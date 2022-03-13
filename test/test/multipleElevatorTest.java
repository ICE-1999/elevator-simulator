package test;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Queue;

import org.junit.Test;

import io.github.notfoundry.elevatorsimulator.scheduler.*;
import io.github.notfoundry.elevatorsimulator.elevator.*;
import io.github.notfoundry.elevatorsimulator.floor.*;

/** This test class will be doing a test run using the existing events.txt file and four elevators
 * 
 * @author Vis
 * */


public class multipleElevatorTest {
	private LocalSchedulerSubsystem sched;
	private ElevatorSubsystem elev, elev2, elev3, elev4;
	private StopReceiver receiver, receiver2, receiver3, receiver4;
	private FloorSubsystem floor;

	@Test
	// Test run of the system using current events.txt file without modification and using multiple elevators
	public void multipleElevatorTestRun() throws Exception, SocketException, UnknownHostException 
	{
		// initializes floor
		DatagramSocket socket = new DatagramSocket();	
		floor = new FloorSubsystem(Path.of("events.txt"));
		floor.init(1234, "localhost");
						
		// initializes sched
	        sched = new LocalSchedulerSubsystem(1234, "localhost");
				
		// checks if sched was initialized properly
		assert(sched.initialize() == true);
				
		// initialize elevs
		elev = new ElevatorSubsystem();
		elev2 = new ElevatorSubsystem();
		elev3 = new ElevatorSubsystem();
		elev4 = new ElevatorSubsystem();
		
		elev.init(1234, "localhost", socket); 
		elev2.init(1234, "localhost", socket); 
		elev3.init(1234, "localhost", socket); 
		elev4.init(1234, "localhost", socket); 
	
		// initialize receivers
		receiver = new StopReceiver(elev);
		receiver2 = new StopReceiver(elev2);
		receiver3 = new StopReceiver(elev3);
		receiver4 = new StopReceiver(elev4);

		receiver.init(socket);
		receiver2.init(socket);
		receiver3.init(socket);
		receiver4.init(socket);
				
		// begin thread execution
		
		Thread schedulerThread = new Thread(sched);
		schedulerThread.start();
				
						
		Thread elevatorThread = new Thread(elev);
		Thread elevatorThread2 = new Thread(elev2);
		Thread elevatorThread3 = new Thread(elev3);
		Thread elevatorThread4 = new Thread(elev4);

		Thread receiverThread = new Thread(receiver);
		Thread receiverThread2 = new Thread(receiver2);
		Thread receiverThread3 = new Thread(receiver3);
		Thread receiverThread4 = new Thread(receiver4);

		Thread floorThread = new Thread(floor);
				
		receiverThread.start();
		receiverThread2.start();
		receiverThread3.start();
		receiverThread4.start();

		elevatorThread.start();
		elevatorThread2.start();
		elevatorThread3.start();
		elevatorThread4.start();

		floorThread.start();
				
		schedulerThread.join();
		floorThread.join();
		
		elevatorThread.join();
		elevatorThread2.join();
		elevatorThread3.join();
		elevatorThread4.join();
		
		receiverThread.join();
		receiverThread2.join();
		receiverThread3.join();
		receiverThread4.join();
	}
}
