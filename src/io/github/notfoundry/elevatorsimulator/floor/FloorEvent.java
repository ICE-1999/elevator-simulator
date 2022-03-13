package io.github.notfoundry.elevatorsimulator.floor;

import java.util.Date;
import java.util.Objects;

/**
 * This is the primary datastructure used by the scheduler to represent a request to move from a source floor to a destination floor at a given time.
 * These events are immutable, meaning that they can safely be shared between multiple threads on a system without risk of partial updates leaving the
 * event in an unstable state.
 * 
 * @author Mark, Lazar, Vis
 *
 */
public final class FloorEvent implements Comparable<FloorEvent> {
	private final long time;
	private final FloorButton button;
	private int direction; //temp until floorbutton is fleshed out
	private final Floor source;
	private final Floor destination;
	private final int ERROR_TYPE;
	
	public FloorEvent(long time, final FloorButton button, final Floor source, final Floor destination, int ERROR_TYPE) {
		this.time = time;
		this.button = button;
		this.source = source;
		this.destination = destination;
		/*
		if(button.compareTo(FloorButton.UP) == 0) {
			this.setDirection(1);
		}
		else {
			this.setDirection(-1);
		}*/
		this.direction = 0;
		this.ERROR_TYPE = ERROR_TYPE;
	}

	/**
	 * @return the time at which the event took place
	 */
	public long getTime() {
		return this.time;
	}
	
	/**
	 * @return the button that was pushed at the floor to request the elevator
	 */
	public FloorButton getButton() {
		return this.button;
	}

	/**
	 * @return the floor from which the event originated
	 */
	public Floor getSource() {
		return this.source;
	}

	/**
	 * @return the floor to which the event is requesting transport to
	 */
	public Floor getDestination() {
		return this.destination;
	}
	
	/**
	 * @return the type of the error
	 */
	public int getERROR_TYPE() 
	{
		return this.ERROR_TYPE;
	}

	@Override
	public String toString() {
		return String.format("%s -> %s at %s", this.getSource(), this.getDestination(), new Date(this.getTime()));
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.destination, this.button, this.source, this.time);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FloorEvent))
			return false;
		final FloorEvent other = (FloorEvent) obj;
		return Objects.equals(this.destination, other.destination)
				&& Objects.equals(this.source, other.source)
				&& Objects.equals(this.button, other.button)
				&& this.time == other.time;
	}

	@Override
	public int compareTo(final FloorEvent o) {
		if (this.time == o.time) return 0;
		if (this.time > o.time) return 1;
		
		return -1;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}
}
