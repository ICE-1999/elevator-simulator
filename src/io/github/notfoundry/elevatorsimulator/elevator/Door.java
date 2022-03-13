package io.github.notfoundry.elevatorsimulator.elevator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import io.github.notfoundry.elevatorsimulator.floor.Floor;
import io.github.notfoundry.elevatorsimulator.floor.FloorMain;

/**
 * This class handles the opening and closing of the Elevator door. Notifying the
 * Scheduler of any change to the state of the Elevator door, and in the event of
 * an error, letting the Scheduler know and take care of the error.
 * 
 * @author Vis
 *
 */
public interface Door {
	
	// All the possible states of a door.
	public enum DoorState
	{
		OPEN, CLOSE, ERROR
	}
	
	String toString();
	DoorState getState();
	void setState(DoorState newState);
	void openDoor();
	void closeDoor();
	void handleError(int error, byte [] schedulerData);
	void notifyScheduler(DoorState state, int errorType);
	}