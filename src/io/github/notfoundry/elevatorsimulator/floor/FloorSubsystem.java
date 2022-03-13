package io.github.notfoundry.elevatorsimulator.floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.Queue;

import io.github.notfoundry.elevatorsimulator.floor.loader.FloorEventLoader;

/**
 * This subsystem is responsible for dispatching events to the elevator scheduler, informing it of each
 * request for elevator transport on the given floor.
 * 
 * @author Mark, Vis, lazar
 */
public class FloorSubsystem implements Runnable {
	private static final long EVENT_SIMULATION_DELAY_MS = 1000;
	
	private DatagramSocket socket;
	private InetAddress schedulerIP;
	private int schedulerPort;
	
	private final Queue<FloorEvent> events;
	
	public FloorSubsystem(final Path eventsPath) {
		this.events = new FloorEventLoader(eventsPath).load();
	}
	
	public void init(final int port, final String IP) throws SocketException, UnknownHostException {
		this.socket = new DatagramSocket();
		this.schedulerIP = InetAddress.getByName(IP);
		this.schedulerPort = port;
		System.out.println("initialized to talk to " + IP + ":" + port);
	}
	
	/**
	 * Starts the floor subsystem, and passes each of the events to the elevator scheduler
	 * in priority order based on the timestamp of the event, before notifying the scheduler
	 * that no more events are available.
	 */
	@Override
	public void run() {
		
		System.out.println("floor: starting");
		while (!this.events.isEmpty()) {
			final FloorEvent event = this.events.poll();
			long sleepTime = event.getTime() - System.currentTimeMillis();
			if (sleepTime < 0) sleepTime = EVENT_SIMULATION_DELAY_MS;
			try {
				System.out.println("Waiting for "+sleepTime+"ms to send next event");
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				throw new RuntimeException(e);
			}
			
			System.out.printf("Floor: sending event %s\n", event);
			byte[] dataToScheduler = new byte[7];
			dataToScheduler[0] = 1;
			dataToScheduler[1] = 1;
			dataToScheduler[3] = (byte) event.getERROR_TYPE();
			dataToScheduler[4] = (byte) event.getSource().getFloor();
			dataToScheduler[5] = (byte) event.getDestination().getFloor();
			dataToScheduler[6] = (byte) event.getDirection();
			DatagramPacket sendToScheduler = new DatagramPacket(dataToScheduler, dataToScheduler.length, schedulerIP, schedulerPort);
			try {
				socket.send(sendToScheduler);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Sent packet from " + socket.getLocalAddress() +":"+ socket.getLocalPort() +" to " + schedulerIP +":"+ schedulerPort);
		}
		System.out.println("Floor: finished");
		
	}

	// Returns the queue of events
	public Queue<FloorEvent> getEvents()
	{
		return this.events;
	}
}

