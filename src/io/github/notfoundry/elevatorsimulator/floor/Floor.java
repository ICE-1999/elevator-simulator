package io.github.notfoundry.elevatorsimulator.floor;

import java.util.Objects;

/**
 * This datastructure represents a floor in the elevator system. Strictly speaking this is just a wrapper around
 * an integer at the moment, but it allows floors to be strongly typed to prevent coding errors. Floors are comparable
 * based on their floor number, allowing a partial ordering to be maintained.
 * 
 * @author Mark
 */
public final class Floor implements Comparable<Floor> {
	private final int floorNum;
	
	public Floor(final int floorNum) {
		this.floorNum = floorNum;
	}

	/**
	 * @return the floor number represented by this floor
	 */
	public int getFloor() {
		return floorNum;
	}

	@Override
	public int hashCode() {
		return Objects.hash(floorNum);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Floor))
			return false;
		final Floor other = (Floor) obj;
		return floorNum == other.floorNum;
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.floorNum);
	}

	@Override
	public int compareTo(final Floor o) {
		return Integer.compare(this.floorNum, o.floorNum);
	}
}
