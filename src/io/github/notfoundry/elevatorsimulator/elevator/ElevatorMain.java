package io.github.notfoundry.elevatorsimulator.elevator;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Main class for running an instance of ElevatorSubsystem
 * 
 * @author Lazar
 *
 */
public class ElevatorMain {
	
	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
		
		DatagramSocket socket = new DatagramSocket();
		
		ElevatorSubsystem elevator = new ElevatorSubsystem();
		elevator.init(1234, "localhost", socket); //subject to change
		StopReceiver receiver = new StopReceiver(elevator);
		receiver.init(socket);
		
		Thread elevatorThread = new Thread(elevator);
		Thread receiverThread = new Thread(receiver);
		
		receiverThread.start();
		elevatorThread.start();
		
		elevatorThread.join();
		receiverThread.join();

	}

}
