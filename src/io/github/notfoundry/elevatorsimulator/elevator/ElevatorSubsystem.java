package io.github.notfoundry.elevatorsimulator.elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This class represents the elevator subsystem, receiving events from the elevator scheduler
 * and notifying it when an event has been completed.
 * 
 * @author Mark, Lazar, Vis
 */
public class ElevatorSubsystem implements Door,Runnable {
	
	public static final int BOTTOM_FLOOR = 1;
	public static final int TOP_FLOOR = 22;
	public static final int ELEVATOR_FLOOR_MOVE_DELAY = 200;
	public static final int ELEVATOR_UNLOAD_DELAY = 1000;
	public static final int REGISTER_SIZE = 7; //size of byte array sent to scheduler for registration
	public static final long doorActionMax = 5000; // Time taken for a door action (in ms) - Opening or Closing a door
	private boolean initialState; // keeps track of whether Elevator is in initial state or not (currFloor = 1, destFloor = 1, direction = 0, errorType = 0, state = DoorState.Close)
	private int currentFloor;
	private int destFloor;
	private int errorType; // 0 - no error, 1 - door error, 2 - floor timer fault
	private DatagramSocket socket;
	private InetAddress schedulerIP;
	private int schedulerPort;
	private int direction; //1 is up, 0 is stationary, -1 is down
	private List<Trip> stops;
	private DoorState state; // State of the door
	private DoorState prevState; // Previous state of door, used to keep track of when door was in error state
	private String msg; // Will be used to store description of elevator state
	
	// Default constructor for Elevator
	public ElevatorSubsystem() {
		this.currentFloor = BOTTOM_FLOOR;
		this.destFloor = BOTTOM_FLOOR;
		this.direction = 0;
		this.errorType = 0;
		this.initialState = true;
		this.state = DoorState.CLOSE;
		this.prevState = DoorState.CLOSE;
		this.stops = new LinkedList<>();
		this.msg = "";
	}
	
	public void init(final int port, final String IP, final DatagramSocket socket) throws SocketException, UnknownHostException {
		this.socket = socket;
		this.schedulerIP = InetAddress.getByName(IP);
		this.schedulerPort = port;
		
		System.out.println("initialized to talk to " + IP + ":" + port);
		System.out.println("This elevator is at "+currentFloor+" going to "+destFloor);
	}
	
	public synchronized void addStop(int start, int dest, int direction, int errorType) {
		this.stops.add(new Trip(start,dest,direction, errorType));
		kick();
		}
	
	/**
	 * checks if we've reached a boundary floor
	 * (largest or smallest floor among current trips)
	 * @param arrived
	 * @return true if boundary was reached
	 */
	private boolean checkBoundary(int arrived) {
		int max = BOTTOM_FLOOR;
		int min = TOP_FLOOR + 1;
		boolean ret = false;
		Iterator<Trip> it = this.stops.iterator();
		while (it.hasNext()) {
			Trip currentTrip = it.next();
			if (currentTrip.getStart() < min) {
				min = currentTrip.getStart();
			}
			if (currentTrip.getStart() > max) {
				max = currentTrip.getStart();
			}
		}
		if (arrived == max && direction == 1) {
			// we've reached max up!
			ret = true;
			this.direction = -1;
		}
		if (arrived == min && direction == -1) {
			ret = true;
			this.direction = 1;
		}
		return ret;
	}
	/**
	 * @param arrived
	 * @return true if stop is on the list, false if not.
	 */
	public synchronized boolean removeStop(int arrived) {
		System.out.println("Arrived at "+arrived);
		boolean rc = false;
		Iterator<Trip> it = stops.iterator();
		while (it.hasNext()) {
			Trip currentTrip = it.next();
						
			if(checkBoundary(arrived) || (currentTrip.getStart() == arrived && currentTrip.getDirection() == this.direction)) {
				rc = true;
				if(currentTrip.getDestination() == null) {
					it.remove();
				}
				else{
					currentTrip.setStart(currentTrip.getDestination()); //set start to destination
					currentTrip.setDestination(null); //set destination to null, (the elevator is now on the way to new 'start')
				}
			}
			else {
				System.out.println("Ignoring stop "+currentTrip.getStart() + " " + currentTrip.getDestination() + " " + currentTrip.getDirection());
			}
		}
		if(this.stops.isEmpty()) {
			this.direction = 0;
		}
		return rc;
	}
	
	//Returns the currentFloor
	public int getCurrentFloor() {
		return this.currentFloor;
	}
	
	//Sets the currentFloor to a specified value
	public void setCurrentFloor(int curr) {
		this.currentFloor = curr;
	}
	
	//Returns the destFloor
	public int getDestinationFloor() {
		return this.destFloor;
	}
	
	//Sets the destFloor to a specified value
	public void setDestinationFloor(int dest) {
		this.destFloor = dest;
	}
	
	//Returns the direction
	public int getDirection() {
		return this.direction;
	}
	
	//Sets the direction to a specified value
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	// Returns the type of error (0 = no error, 1 = door error, 2 = floor timer fault)
	public int getErrorType() 
	{
		return this.errorType;
	}
	
	// Sets the type of error to a specified value
	public void setErrorType(int errorType) 
	{
		this.errorType = errorType;
	}
	
	@Override
	// toString method which will describe the current state of the elevator, current floor, direction, destination floor, errorType and state of door
	public String toString() 
	{
		String direction = "";
		if (this.direction == 1) direction = "UP";
		if (this.direction == -1) direction = "DOWN";
		
		String error = "No error";
		if (this.errorType == 1) error = "Door Stuck Error";
		if (this.errorType == 2) error = "Floor Timer Fault";
		
		msg = "This elevator is at floor: " + currentFloor + " going " + direction + " to floor: " + destFloor;
		msg += "\nThe current state of the door is: " + state;
		
		if (this.errorType != 0) msg += "\n It is expected that this elevator will experience the error: " + error;
		else msg += "\nThe elevator will not expereince any errors.";
		
		return msg;
	}
	

	/**
	 * elevator travels from current floor to destination floor
	 * and sends updates to the scheduler when it passes each floor.
	 */
	public void travel() {
			
		//only enters if currentFloor is in stops.getStart()
		if (removeStop(currentFloor))
		{
			openDoor();
		}
		
		msg = toString();
		
		// while traveling, send update to scheduler at every floor
		byte[] dataToScheduler = new byte[7];
		dataToScheduler[0] = 0;		//elevator message
		dataToScheduler[1] = 1;		//update
		if (state == DoorState.CLOSE) dataToScheduler[2] = 0; // Door is closed
		if (state == DoorState.OPEN) dataToScheduler[2] = 1; // Door is open
		dataToScheduler[3] = (byte) this.errorType;     // error type, if any
		dataToScheduler[4] = (byte) this.currentFloor; 	//currentFloor
		dataToScheduler[6] = (byte) this.direction; 	//direction
		
		if (state == DoorState.CLOSE) System.out.println("At floor "+this.currentFloor+" going "+this.direction);
		
		//find max or min floor in stops<>
		if(!this.stops.isEmpty()) {
			int maxOrMin = maxOrMinStop(this.direction);
			if(maxOrMin == 0) { // reached top or bottom
				dataToScheduler[5] = (byte) this.currentFloor; //treated as destination
				this.direction = -this.direction;
			}
			else {
				dataToScheduler[5] = (byte) maxOrMin ; // max/min stop to be treated as destination for elevator finding algorithm
			}
		}
		else {
			dataToScheduler[5] = (byte) this.currentFloor;
			dataToScheduler[6] = 0;
			this.direction = 0;
			this.destFloor = this.currentFloor;
		}
		
		DatagramPacket send = new DatagramPacket(dataToScheduler, dataToScheduler.length, schedulerIP, schedulerPort);
		try {
			if(this.state != DoorState.ERROR) 
			{
			socket.send(send);
			System.out.println("Elevator sent update to scheduler");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		//update sent
		
		//increment floor
		if (this.currentFloor < TOP_FLOOR + 1) 
		{
			if (getState() == DoorState.ERROR) 
			{
				while(true) {} // do nothing
			}
			if (state == DoorState.CLOSE && currentFloor + direction >= 1 && currentFloor + direction <= TOP_FLOOR ) this.currentFloor += this.direction;
		}
	}
	
	private synchronized int maxOrMinStop(int dir) {
		int minimum = BOTTOM_FLOOR;
		int maximum = TOP_FLOOR;
		Iterator<Trip> it = stops.iterator();
		while (it.hasNext()) {
			Trip currentTrip = it.next();
			if(currentTrip.getDirection() == direction) {
				//finding maximum
				if(currentTrip.getStart() > maximum) {
					maximum = currentTrip.getStart();
				}
				if(currentTrip.getDestination() != null && currentTrip.getDestination() > maximum) {
					maximum = currentTrip.getDestination();
				}
				//finding minimum
				if(currentTrip.getStart() < minimum) {
					minimum = currentTrip.getStart();
				}
				if(currentTrip.getDestination() != null && currentTrip.getDestination() < minimum) {
					minimum = currentTrip.getDestination();
				}
			}
		}
		if(direction == 1) {
			return maximum;
		}
		if(direction == -1) {
			return minimum;
		}
		return 0;
	}

	/**
	 * Runs the elevator subsystem, which receives a destination from the scheduler
	 * and sends an elevator to the destination.
	 */
	@Override
	public void run() {
		//register elevator
		System.out.println("registering elevator");
		byte[] register = new byte[REGISTER_SIZE];
		register[0] = 0;
		register[1] = 0;
		register[4] = 1;
		DatagramPacket sendReg = new DatagramPacket(register, register.length, schedulerIP, schedulerPort);
		try {
			socket.send(sendReg);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		while (true) {
			while(!stops.isEmpty()) {
				if (direction == 0) {
					Trip firstStop = stops.iterator().next();
					if (firstStop.getStart() < currentFloor) {
						this.direction = -1;
						initialState = false;
					} else if (firstStop.getStart() > currentFloor) {
						this.direction = 1;
						initialState = false;
					} else {
						this.direction = firstStop.getDirection();
					}
					
					if (state != DoorState.ERROR) 
					{
						System.out.println("First stop at " + firstStop.getStart() +
							" setting direction to " + this.direction);
						setDestinationFloor(firstStop.getStart());
						setErrorType(firstStop.getErrorType());
					}
				} 					
				
				travel();
			
			} // while stops aren't empty
			try {
				System.out.println("No stops available, waiting...");
				waitForKick();
				System.out.println("Got the kick");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	private synchronized void waitForKick() throws InterruptedException {
		System.out.println("This elevator is stationary");
		this.direction = 0; //stationary
		wait();
	}
	
	private synchronized void kick() {
		System.out.println("Kick received");
		notifyAll();
		
		// If there are still stops remaining and if the elevator door was in error state previously and door state is currently closed, call emergencyHelp() to resume travel
		if (stops.size() >= 1 && prevState == DoorState.ERROR && state == DoorState.CLOSE) resumeTravel();
	}
	
	// If there any other stops after handling error, resume travel
	private void resumeTravel() 
	{		
		System.out.println ("New stops, resuming travel...");
		while (stops.size() >= 1) 
		{
			setDestinationFloor(stops.get(0).getStart());
			setErrorType(stops.get(0).getErrorType());
			if (currentFloor > destFloor) direction = -1;
			if (currentFloor < destFloor) direction = 1;
			
			while(currentFloor >= destFloor || currentFloor <= destFloor) 
			{
				System.out.println("At floor "+this.currentFloor+" going "+this.direction);
				if (currentFloor != destFloor) 
				{
				this.removeStop(currentFloor);
				currentFloor += direction;
				}
				
				if (currentFloor == destFloor) 
				{
					travel();
					break;
				}
			}
		} // no more stops
				
		if (state != DoorState.ERROR) 
		{
		setDirection(0);
		System.out.println("No stops available...");
		System.out.println("Elevator is idle");
		}
	}
		
	@Override
	// Returns the state of the door
	public DoorState getState() {
		return this.state;
	}
	
	@Override
	// Sets the state of the door to a specified DoorState value
	// DoorState values: OPEN, CLOSED, ERROR
	public void setState(DoorState newState) 
	{
		this.state = newState;
	}

	@Override
	// Open the door at the corresponding floor and  notify Scheduler if there is an error
	public void openDoor() 	
	{
		setState(DoorState.OPEN);
		System.out.println ("The door at floor " + currentFloor + " has been opened");
		long OpenStateTime = System.currentTimeMillis(); // Time when the door is opened
				
		System.out.println("The door will remain open for " + doorActionMax/1000 + " seconds");
		
		// Door Stuck Error - Keep the door open for 1 more second at the error floor
		if (!initialState && errorType == 1 && currentFloor == destFloor) 
		{
			while(System.currentTimeMillis() - OpenStateTime != 1000 + doorActionMax);
		}  
		
		// Open door normally, if initial state or if the errorType is not 1 and not initialState
		else if (initialState || !initialState && errorType != 1 && currentFloor == destFloor) while (System.currentTimeMillis() - OpenStateTime != doorActionMax) {} // Open the door in doorActionMax duration
		
		
		long newTime = System.currentTimeMillis(); // Time after the door has been opened for doorActionMax duration
		long totalTime = newTime - OpenStateTime; // Should be equal to doorActionMax (5000 ms) if not, an error occurred
		
		// Checks if door opening time exceeded doorActionMax
		if (totalTime > doorActionMax) 
		{
			System.out.println("The door seems to have been stuck open and took too long to start closing....");
			setState(DoorState.ERROR);
			this.prevState = DoorState.ERROR; // Keeps track of door previously being in error state, since state will get out of error after handling error
			notifyScheduler(state, errorType);
		}
		else if (totalTime == doorActionMax) closeDoor();
	}
	
	@Override
	// Close the door at the corresponding floor and notify Scheduler if there is an error
		public void closeDoor() 
	{
		System.out.println ("The door at floor " + this.currentFloor + " will close in " + doorActionMax/1000 + " seconds");
		long currTime = System.currentTimeMillis();
		
		//Floor timer fault - delay the door close by 1 more second
		if (!initialState && errorType == 2 && currentFloor == destFloor) 
		{ 
		while (System.currentTimeMillis() - currTime != 1000 + doorActionMax){}
		}
		
		// Close door normally, if initial state or if the errorType is not 2 and not initialState
		else if (initialState || !initialState && errorType != 2 && currentFloor == destFloor) while (System.currentTimeMillis() - currTime != doorActionMax) {} // Open the door in doorActionMax duration
		
		setState(DoorState.CLOSE);
		
		System.out.println ("The door at floor " + currentFloor + " has been closed");
		
		long CloseStateTime = System.currentTimeMillis(); // Time after the door has finished closing
		long totalTime = CloseStateTime - currTime; // Should be equal to doorActionMax (5000 ms), if not, an error occured
		
		// Checks if door closing exceeded time for doorActionMax
		if (totalTime > doorActionMax) 
		{
			System.out.println("The door took too much time in order to be closed, delaying the trip to next floor....");
			setState(DoorState.ERROR);
			this.prevState = DoorState.ERROR; // Keeps track of door previously being in error state, since state will get out of error after handling error
			notifyScheduler(state, errorType);
		}
	}

	@Override
	/* Handle any errors that the door experiences (i.e. staying open longer 
	than it should) by retrieving the schedulerData from StopReceiver
	*/
	public void handleError(int error, byte[] schedulerData) 
	{	
		System.out.println("Attempting to handle error....");
		
		// Handle door stuck error
		if (schedulerData[2] == 0) {
		System.out.println("Door at floor " + currentFloor + " received request from Scheduler: " + "Close Doors");
		setErrorType(0);
		closeDoor();
		System.out.println("The door stuck error at floor " + currentFloor + " has been resolved.");
		
		}
			
		// Handle floor timer fault error
		if (schedulerData[2] == 1) 
		{
			System.out.println("Door at floor " + currentFloor + " received request from Scheduler: " + "Open Doors");
			setErrorType(0);
			openDoor();
			System.out.println("The elevator at " + currentFloor + " will now be terminated");
			System.exit(0);
		}
	}
	
	@Override
	// Notify the Scheduler of the current error that the elevator is experiencing
		public void notifyScheduler(DoorState state, int errorType) 
		{
			byte[] dataToScheduler = new byte[7];
			dataToScheduler[0] = 0;
			dataToScheduler[1] = 1;
			dataToScheduler[2] = 2; // State of Door is in ERROR
			dataToScheduler[3] = (byte) errorType; // type of error
			dataToScheduler[4] = (byte) currentFloor;
			dataToScheduler[5] = (byte) currentFloor;
			
			try {
				DatagramPacket schedulerMsg = new DatagramPacket(dataToScheduler, dataToScheduler.length, schedulerIP, schedulerPort);
				socket.send(schedulerMsg);
				System.out.println("Door status at floor " + this.currentFloor + " has been sent to Scheduler");
				//notifyAll();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
}