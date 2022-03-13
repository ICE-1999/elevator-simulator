package io.github.notfoundry.elevatorsimulator.scheduler;
/**
 * Main class for running scheduler thread
 * 
 * @author Lazar
 *
 */
public class SchedulerMain {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		LocalSchedulerSubsystem scheduler = new LocalSchedulerSubsystem(1234, "localhost");
		if (!scheduler.initialize()) {
			System.err.println("Scheduler failed to initialize");
			return;
		}
		Thread schedulerThread = new Thread(scheduler);
		
		schedulerThread.start();
		schedulerThread.join();
	}

}
