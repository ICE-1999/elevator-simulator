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

/** This test class will be testing error injection and handling 
 * 
 * @author Vis
 * */

public class errorHandlingTest {

	private LocalSchedulerSubsystem sched;
	private ElevatorSubsystem elev;
	private StopReceiver receiver;
	private FloorSubsystem floor;

	@Test
	// Test to check injection of error Type 1 from events.txt to FloorSubsystem
	public void injectErrorType1 () throws SocketException 
	{
		DatagramSocket socket = new DatagramSocket();
		
		floor = new FloorSubsystem(Path.of("events.txt"));
		
		Iterator <FloorEvent> it = floor.getEvents().iterator();

		// Remove all the FloorEvents in queue of events, that do not have an errorType of 1
		FloorEvent event = floor.getEvents().peek();
		while (floor.getEvents().size() > 0) {
			if (floor.getEvents().peek().getERROR_TYPE() == 1) 
			{
				event = floor.getEvents().peek();
			}
			floor.getEvents().remove();
		}
		floor.getEvents().add(event);

		// Checks to see if the events other than those with errorType 1 were removed, in this case should be equal to 1
		assert(floor.getEvents().size() == 1);
		
		// Checks to see if the head of the queue / the only FloorEvent has an errorType of 1
		assert(floor.getEvents().element().getERROR_TYPE() == 1);		
	}
	
	@Test
	// Test to check injection of error Type 2 from events.txt to FloorSubsystem
	public void injectErrorType2 () throws SocketException 
	{
		DatagramSocket socket = new DatagramSocket();
		
		floor = new FloorSubsystem(Path.of("events.txt"));
		Iterator <FloorEvent> it = floor.getEvents().iterator();
		
		// Remove all the FloorEvents in queue of events, that do not have an errorType of 2
		FloorEvent event = floor.getEvents().peek();
		while (floor.getEvents().size() > 0) {
			if (floor.getEvents().peek().getERROR_TYPE() == 2) 
			{
				event = floor.getEvents().peek();
			}
			floor.getEvents().remove();
		}
		floor.getEvents().add(event);

		// Checks to see if the events other than those with errorType 1 were removed, in this case should be equal to 1
		assert(floor.getEvents().size() == 1);
		
		// Checks to see if the head of the queue / the only FloorEvent has an errorType of 2
		assert(floor.getEvents().element().getERROR_TYPE() == 2);		
	}
	
	@Test
	// Handles error 1 - door stuck error
	public void handleError1 () throws Exception, SocketException, UnknownHostException 
	{
		// initializes floor
		DatagramSocket socket = new DatagramSocket();	
		floor = new FloorSubsystem(Path.of("events.txt"));
		
		// Remove all the FloorEvents in queue of events, that do not have an errorType of 1
		FloorEvent event = floor.getEvents().peek();
		while (floor.getEvents().size() > 0) {
			if (floor.getEvents().peek().getERROR_TYPE() == 1) 
			{
				event = floor.getEvents().peek();
			}
			floor.getEvents().remove();
		}
		
		floor.getEvents().add(event);
		floor.init(1234, "localhost");

		// Checks to see if the events other than those with errorType 1 were removed, in this case should be equal to 1
		assert(floor.getEvents().size() == 1);

		// Checks to see if the head of the queue / the only FloorEvent has an errorType of 1
		assert(floor.getEvents().peek().getERROR_TYPE() == 1);
				
		// initializes sched
		sched = new LocalSchedulerSubsystem(1234, "localhost");
		
		// checks if sched was initialized properly
		assert(sched.initialize() == true);
		
		// initialize elev
		elev = new ElevatorSubsystem();
		elev.init(1234, "localhost", socket); 
				
		// initialize receiver
		receiver = new StopReceiver(elev);
		receiver.init(socket);
				
		// begin thread execution
		Thread schedulerThread = new Thread(sched);
		schedulerThread.start();
		
				
		Thread elevatorThread = new Thread(elev);
		Thread receiverThread = new Thread(receiver);
		
		Thread floorThread = new Thread(floor);
		
		receiverThread.start();
		elevatorThread.start();
		
		floorThread.start();
		
		schedulerThread.join();
		floorThread.join();
	}

	@Test
	// Handles error 2 - floor timer fault
	public void handleError2 () throws Exception, SocketException 
	{
		// initializes floor
		DatagramSocket socket = new DatagramSocket();	
		floor = new FloorSubsystem(Path.of("events.txt"));
		
		// Remove all the FloorEvents in queue of events, that do not have an errorType of 1
		FloorEvent event = floor.getEvents().peek();
		while (floor.getEvents().size() > 0) {
			if (floor.getEvents().peek().getERROR_TYPE() == 2) 
			{
				event = floor.getEvents().peek();
			}
			floor.getEvents().remove();
		}
		
		floor.getEvents().add(event);
		floor.init(1234, "localhost");

		// Checks to see if the events other than those with errorType 1 were removed, in this case should be equal to 1
		assert(floor.getEvents().size() == 1);

		// Checks to see if the head of the queue / the only FloorEvent has an errorType of 1
		assert(floor.getEvents().peek().getERROR_TYPE() == 2);
		{
		// initializes sched
		sched = new LocalSchedulerSubsystem(1234, "localhost");
		
		// checks if sched was initialized properly
		assert(sched.initialize() == true);
		
		// initialize elev
		elev = new ElevatorSubsystem();
		elev.init(1234, "localhost", socket); 
				
		// initialize receiver
		receiver = new StopReceiver(elev);
		receiver.init(socket);
				
		// begin thread execution
		Thread schedulerThread = new Thread(sched);
		schedulerThread.start();
		
		Thread elevatorThread = new Thread(elev);
		Thread receiverThread = new Thread(receiver);
		
		Thread floorThread = new Thread(floor);
		
		receiverThread.start();
		elevatorThread.start();
		
		floorThread.start();
		
		schedulerThread.join();
		floorThread.join();
		elevatorThread.join();
		receiverThread.join();
		}
	}

}
