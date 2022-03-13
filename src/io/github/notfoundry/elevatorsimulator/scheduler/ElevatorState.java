package io.github.notfoundry.elevatorsimulator.scheduler;

import java.net.SocketAddress;

/**
 * Used by scheduler to decide what to tell the elevator. 
 * 
 * @author Lazar, Vis
 *
 */
public class ElevatorState {
	private int current;
	private int destination;
	private int direction;
	private int errorType;
	private SocketAddress address;
	
	public ElevatorState(int current, SocketAddress address) {
		this.setCurrent(current);
		//this.setDestination(current);
		this.address = address;
		this.setDirection(1);
	}

	public int getCurrent() {
		return current;
	}

	public void setCurrent(int current) {
		this.current = current;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getErrorType () 
	{
		return errorType;
	}
	
	public void setErrorType(int errorType) 
	{
		this.errorType = errorType;
	}
	
	public SocketAddress getAddress() {
		return address;
	}
	
	@Override
	public String toString() {
		return "{current: " + current + 
				", destination: " + destination + 
				", address: " + address.toString() + 
				", errorType: " + errorType + "}";
	}
	
}