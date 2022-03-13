package io.github.notfoundry.elevatorsimulator.scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

//import io.github.notfoundry.elevatorsimulator.elevator.ElevatorSubsystem;
//import io.github.notfoundry.elevatorsimulator.floor.Floor;
import io.github.notfoundry.elevatorsimulator.floor.FloorEvent;

/**
 * This is an implementation of the SchedulerSubsystem designed to run as a thread on a local system.
 * 
 * @author Mark, Lazar, Vis
 */
public class LocalSchedulerSubsystem implements SchedulerSubsystem {
	
	private final int ELEV_PACKET_SIZE = 4;
	
	private final int port;
	private final String IP;
	private DatagramSocket socket;
	private State state;
	private ArrayList<ElevatorState> regElevators = new ArrayList<>();
	// TODO
	// create elevator state class (check)
	// have a list of those (check)
	// create the floor class (already exists)
	// 1 or list?? TBD
	
	private FloorEvent event;
	
	public LocalSchedulerSubsystem(int port, String IP) {
		this.port = port;
		this.IP = IP;
		this.state = State.SENDING;
	}
	
	public boolean initialize() throws Exception {
		socket = new DatagramSocket(port, InetAddress.getByName(IP));
		System.out.println("initialized on " + IP + ":" + port);
		return true; //for now
	}

	/**
	 * This method is used to notify the scheduler of an incoming floor change event, which will prompt it to
	 * attempt to notify and blocked elevators that new events are available.
	 */
	@Override
	public synchronized void onFloorEvent(final FloorEvent event) {
		System.out.printf("Scheduler: received event %s\n", event);
		
		while (this.state == State.RECEIVING) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.state = State.RECEIVING;
		this.event = event;
		
		System.out.printf("Scheduler: notifying elevators of event %s\n", event);

		this.notifyAll();
	}
	
	/**
	 * This method is used to poll the scheduler for the most recent incoming floor change event, which can be used to
	 * dispatch the event to an appropriate elevator.
	 */
	@Override
	public synchronized FloorEvent getFloorEvent() {
		while (this.state == State.SENDING) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		this.state = State.SENDING;
		
		final FloorEvent event = this.event;
		this.event = null;
		
		return event;
	}
	
	/**
	 * This method is used to notify the scheduler that an elevator has finished handling the given floor event,
	 * and is ready to receive new movement instructions.
	 */
	@Override
	public synchronized void onFloorEventEnd(final FloorEvent event) {
		System.out.printf("Scheduler: received reply to event %s\n", event);
		//check if floor finished
		//this.onFinish();
	}
	
	/**
	 * This method notifies the scheduler that there are no more events to process. In this real world,
	 * this may not actually happen, but for the purposes of testing this is exposed to users.
	 */
	@Override
	public void onFinish() {
		System.out.println("Scheduler: finished");
		this.state = State.FINISHED;
	}
	
	/**
	 * @return whether the scheduler is still receiving events to publish
	 */
	@Override
	public boolean isFinished() {
		return this.state == State.FINISHED;
	}
	
	/**
	 * This represents the state of the scheduler at any given time. Transitions between these states are handled internally in response to
	 * incoming events or requests for the most recent event data.
	 */
	private static enum State {
		SENDING, RECEIVING, FINISHED
	}

	
	/**
	 * Receives packets from floors or elevators, and sends packet for the next elevator action.
	 * PACKET FORMAT:
	 * Byte 0: source [0 = elevator, 1 = floor]
	 * Byte 1: type [0 = registration, 1 = request/movement notification]
	 * Byte 2: the state of the Elevator door [0 = closed, 1 = opened, 2 = error]
	 * Byte 3: if there is an error at a given floor [0 = no error, 1 = door error, 2 = floor error]
	 * Byte 4: current floor number: int
	 * Byte 5: destination floor number: int (floor receive)
	 * Byte 6: direction [-1 = down, 1 = up]
	 * 
	 * Handles floor requests, elevator registrations, and elevator updates
	 */
	@Override
	public void run() {
		while(true) {
			try {
				System.out.println("Waiting for packets...");
				byte[] receiveArray = new byte[512];
				DatagramPacket received = new DatagramPacket(receiveArray, 512);
				socket.receive(received);
				System.out.println("Received packet from " + received.getAddress() + ":" + received.getPort());
				byte[] receivedData = received.getData();
				//do stuff with the received data
				if(receivedData[0] == 0) {
					handleElevator(received);
				}
				else if(receivedData[0] == 1) {
					//check if elevator exists??
					handleFloor(received);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//this.onFinish();
	}

	private void handleElevator(DatagramPacket received) throws IOException {
		//2nd byte registration
		byte[] receivedData = received.getData();
		int floorNum = receivedData[4];
		if(receivedData[1] == 0) {
			//register elevator
			regElevators.add(new ElevatorState(floorNum, received.getSocketAddress()));
			//done registration
			return;
		}
		//received update
		if(receivedData[1] == 1) {
			// update contains current floor, destination floor
			// look in elevator list and find the elevator and change its current floor number
			boolean changed = false;
			for(ElevatorState e : regElevators) {
				if(e.getAddress().equals(received.getSocketAddress())) {
					e.setDestination(receivedData[5]);
					e.setCurrent(receivedData[4]);
					e.setErrorType(receivedData[3]); // used to be setDirection
					
					if (e.getCurrent() < e.getDestination()) e.setDirection(1);
					if (e.getCurrent() > e.getDestination()) e.setDirection(-1);
					changed = true;
				}
			}
			if(!changed) {
				System.out.println("Elevator not registered");
				return;
			}
			//done
		}
		
		// If the door is in a error state (DoorState.ERROR)
		if (receivedData[2] == 2) 
		{
			System.out.println("The scheduler acknowledges the error at floor " + floorNum);
			
			// Handles door error by sending a request back to the Elevator telling it to close its door
			if (receivedData[3] == 1) 
			{
				System.out.println("Attempting to resolve stuck door at " + floorNum);
				for (ElevatorState e: regElevators) 
				{
					if(e.getAddress().equals(received.getSocketAddress())) 
					{
						byte[] dataToElevator = new byte[7];
						dataToElevator[0] = 0;
						dataToElevator[1] = 1;
						dataToElevator[2] = 0; // Tell the Elevator to close its door
						dataToElevator[3] = 0; // Therefore, resolving the error 
						dataToElevator[4] = (byte) floorNum;
						dataToElevator[5] = (byte) floorNum;
						
						DatagramPacket sendToElevator = new DatagramPacket(dataToElevator, dataToElevator.length, e.getAddress());
						try {
							socket.send(sendToElevator);
							System.out.println("Scheduler sent request to Elevator at " + floorNum + " to fix stuck door");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
			
			// Handles floor timer fault
			if (receivedData[3] == 2) 
			{
				System.out.println("Attempting to resolve floor timer fault at " + floorNum);
				
				ArrayList <ElevatorState> copyCurrList = regElevators;
				
				for (ElevatorState e: regElevators) 
				{
					if(e.getAddress().equals(received.getSocketAddress())) 
					{
						byte[] dataToElevator = new byte[7];
						dataToElevator[0] = 0;
						dataToElevator[1] = 1;
						dataToElevator[2] = 1; // Tell the Elevator to open its door
						dataToElevator[3] = 2; 
						dataToElevator[4] = (byte) floorNum;
						dataToElevator[5] = (byte) floorNum;
						
						DatagramPacket sendToElevator = new DatagramPacket(dataToElevator, dataToElevator.length, e.getAddress());
					
						try {
							socket.send(sendToElevator);
							System.out.println("Scheduler sent request to Elevator at " + floorNum + " to open doors");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
						copyCurrList.remove(e);
						
						System.out.println("The scheduler has removed Elevator" + e.getAddress() + " from the list of operating Elevators");
						System.out.println("Floor timer fault at floor " + floorNum + " has been handled");
						break;
					}
				}
				
				regElevators = copyCurrList;
				
				// If there are no more elevators in the list of elevators, terminate scheduler
				if (regElevators.size() == 0) 
				{
					System.out.println("No elevators are in service, all have been terminated or shut down");
					System.out.println("Terminating Scheduler....");
					System.exit(0);
				}
			}

			
		}
		
		//received update
		if(receivedData[1] == 1) {
			// update contains current floor, destination floor, direction
			// look in elevator list and find the elevator and change its current floor number
			boolean changed = false;
			for(ElevatorState e : regElevators) {
				if(e.getAddress().equals(received.getSocketAddress())) {
					e.setCurrent(receivedData[4]);   // 4th byte is currentFloor
					e.setErrorType(receivedData[3]); // 3rd byte is errorType
					e.setDirection(receivedData[6]); // 6th byte is direction
					if(receivedData[6] == 1 || receivedData[6] == -1) {
						e.setDestination(receivedData[5]); 
					}
					else {
						e.setDestination(receivedData[5]);
					}
					changed = true;
				}
			}
			if(!changed) {
				System.out.println("Elevator not registered");
				return;
			}
			
			//done
		}
	}
	
	private void handleFloor(DatagramPacket received) throws Exception {
		
		byte[] receivedData = received.getData();
		//2nd byte request
		if(receivedData[1] == 1) {
			ElevatorState bestElevator;

			if(!regElevators.isEmpty()) {
				//send elevator that is nearest to its destination to requested destination.
				bestElevator = findBestElevator(receivedData[4]); //find the elevator closest to the waiting passenger
				bestElevator.setDestination(receivedData[5]);
			}
			else {
				throw new Exception("No registered elevators to move");
			}
			//send destination to best elevator
			byte[] elevData = new byte[ELEV_PACKET_SIZE];
			elevData[0] = receivedData[5]; // destination
			elevData[1] = receivedData[4]; // "current". Which the elevator shall treat as a destination, until it reaches it
			elevData[2] = receivedData[6]; // button Direction
			elevData[3] = receivedData[3]; // error type, if any
			// send packet to elevatorSubsystem with the new starting and destination floors
			DatagramPacket sendToElevator = new DatagramPacket(elevData, elevData.length, bestElevator.getAddress());
			socket.send(sendToElevator);
		}
		else {
			//error
			throw new Exception("Floor request invalid type: byte[1] != 1");
		}
	}
	
	/**
	 * Finds fastest elevator that can arrive at request location.
	 * Fastest = smallest( abs(currentDestination - current) + abs(currentDestination - newDestination) )
	 * 
	 * The above calculation adds the number of floors to current destination to the number of floors that
	 * the elevator would need to go from that destination to the new destination. The smallest of these 
	 * values is the fastest elevator.
	 * 
	 * This algorithm only works if the elevators don't get interrupted during travel. This is accounted
	 * for by searching first for an elevator that is on the way to newStart.
	 * 
	 * @param newStart the floor at which the button was pressed.
	 */
	public ElevatorState findBestElevator(int newStart) {
		ElevatorState bestSoFar = null;
		int best = 23;
		if(regElevators.size() == 1) {
			return regElevators.get(0);
		}
		int curr;
		System.out.println("searching best elevator for destination "+newStart);
		for(ElevatorState e : regElevators) {
			
			System.out.println("Examining "+e.toString());
			
			// Makes sure the elevator being chosen as best elevator will not be terminated upon handling its error
			if (e.getErrorType() != 2) 
			{
				
			if(e.getDirection() == 0) {
				curr = Math.abs(e.getCurrent() - newStart);
			}
			else {
				curr = Math.abs(e.getDestination() - e.getCurrent()) + Math.abs(e.getDestination() - newStart);
			}
			if(curr < best) {
				bestSoFar = e;
				System.out.println(bestSoFar.toString());
				System.out.println("current "+curr+" is better than " + best);
				best = curr;
			}
			if (curr > best) {
				System.out.println("current "+curr+" is not better than " + best);
			}
			
			}
		}
		return bestSoFar;
	}
	
	
}