package io.github.notfoundry.elevatorsimulator.elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import io.github.notfoundry.elevatorsimulator.elevator.Door.DoorState;

/**
 * This class represents the StopReceiver which receives stops from the Scheduler
 * and sends them to the elevator. If it receives an error handling request,
 * it calls the handleError() method of the Elevator using the schedulerData
 * and the error the Elevator was experiencing is resolved.
 * 
 * 
 * @author Lazar, Vis
 */

public class StopReceiver implements Runnable {
	private ElevatorSubsystem elev;
	private DatagramSocket socket;
	
	public static final int PACKET_SIZE = 4;
	
	public StopReceiver(ElevatorSubsystem elevator) {
		this.elev = elevator;
	}
	
	public void init(DatagramSocket socket) throws SocketException {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			//Receive newStart and destination from Scheduler
			byte[] receiveArray = new byte[PACKET_SIZE];
			DatagramPacket received = new DatagramPacket(receiveArray, PACKET_SIZE);
			
			try {
				socket.receive(received);
				
				// Checks if StopReceiver received something other than a Stop from Scheduler
				if (received.getData()[0] == 0 && received.getData()[1] == 1) 
				{
					// If so, handle error using the received Data
					this.elev.handleError(received.getData()[3], receiveArray);
				} 
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			// Make sure that the Receiver is not misinterpreting error handling requests as stops
			if (received.getData()[0] != 0) this.elev.addStop(received.getData()[1], received.getData()[0], received.getData()[2], received.getData()[3]); //start, destination, direction, error
			}
		}
	}