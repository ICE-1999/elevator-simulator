package io.github.notfoundry.elevatorsimulator.scheduler;

import io.github.notfoundry.elevatorsimulator.floor.FloorEvent;

/**
 * This interface defines the basic functionality of the scheduler subsystem, which is responsible
 * for accepting floor change events and notifying the appropriate elevators of which floors to move to.
 * The scheduler should also be notified of each floor change by the elevator, to allow its internal
 * state to remain consistent with the actual state of the world.
 * 
 * @author Mark
 *
 */
public interface SchedulerSubsystem extends Runnable {
	void onFloorEvent(final FloorEvent e);
	
	FloorEvent getFloorEvent();
	
	void onFloorEventEnd(final FloorEvent e);
	
	void onFinish();
	
	boolean isFinished();
}
