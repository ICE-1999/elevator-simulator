package io.github.notfoundry.elevatorsimulator.floor.loader;

/**
 * An exception base for handling errors with the elevator events that will be handled by the system.
 * 
 * @author Mark
 */
public class EventLoadException extends RuntimeException {
	private static final long serialVersionUID = 6082449656032361744L;

	public EventLoadException(final Exception e) {
		super(e);
	}
}
