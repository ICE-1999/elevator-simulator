package io.github.notfoundry.elevatorsimulator.elevator;

public class Trip {
	private int start;
	private Integer destination;
	private int direction; //1 up, -1 down
	private int errorType;
	
	public Trip (int start, Integer destination, int direction, int errorType) {
		this.setStart(start);
		this.setDestination(destination);
		this.setDirection(direction);
		this.setErrorType(errorType);
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public Integer getDestination() {
		return destination;
	}

	public void setDestination(Integer destination) {
		this.destination = destination;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}
	
 	public void setErrorType (int errorType) 
 	{
 		this.errorType = errorType;
 	}
 	
 	public int getErrorType () 
 	{
 		return errorType;
 	}
 	

 	public void printTrip() { 
 		System.out.println("-------PRINTING TRIP---------");
 		System.out.println("Trip start: "+this.getStart());
		System.out.println("Trip destination: "+this.getDestination());
		System.out.println("Trip Direction: "+this.direction);
		System.out.println("Trip Error: "+this.errorType);

 	}
 	
}