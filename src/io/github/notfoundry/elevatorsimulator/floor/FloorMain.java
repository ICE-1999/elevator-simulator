package io.github.notfoundry.elevatorsimulator.floor;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;

/**
 * Main class that runs the floorsubsystem thread
 * 
 * sends and yeets
 * 
 * @author Lazar
 */
public class FloorMain {

	public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
		// TODO Auto-generated method stub
		FloorSubsystem floor = new FloorSubsystem(Path.of("events.txt"));
		floor.init(1234, "localhost");
		
		Thread floorThread = new Thread(floor);
		
		floorThread.start();
		floorThread.join();
	}

}
