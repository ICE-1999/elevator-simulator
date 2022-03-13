package io.github.notfoundry.elevatorsimulator.floor.loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.PriorityQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.notfoundry.elevatorsimulator.floor.Floor;
import io.github.notfoundry.elevatorsimulator.floor.FloorButton;
import io.github.notfoundry.elevatorsimulator.floor.FloorEvent;

/**
 * This class is responsible for loading the events for a given floor from a text file into a machine-readable format
 * capable of being processed by the elevator scheduler.
 * 
 * The format of each line of the input text file is
 * 
 * $TIME $BUTTON $SOURCE $DESTINATION
 * 
 * where these variables have the formats
 *  - $TIME: A string with the format HH:mm:ss:SSS
 *  - $BUTTON: UP or DOWN
 *  - $SOURCE: An integer for the source floor number
 *  - $DESTINATION: An integer for the destination floor number
 * 
 * @author Mark
 *
 */
public class FloorEventLoader {
	private static final String EVENT_DATA_DELIMITER = " ";
	
	private final Path path;
	
	public FloorEventLoader(final Path path) {
		this.path = path;
	}
	
	public PriorityQueue<FloorEvent> load() throws EventLoadException {
		try (final Stream<String> lines = Files.lines(this.path)) {
			return lines
					.map(FloorEventLoader::parseFloorEvent)
					.peek(FloorEventLoader::printLoadedEvent)
					.collect(Collectors.toCollection(PriorityQueue::new));
		} catch (final IOException e) {
			throw new EventLoadException(e);
		}
	}
	
	private static FloorEvent parseFloorEvent(final String data) {
		final String[] components = data.split(EVENT_DATA_DELIMITER);
		
		long now = System.currentTimeMillis();
		final long timeOffset = Long.parseLong(components[0]) * 1000;
		final FloorButton floorButton = FloorButton.valueOf(components[1]);
		final Floor source = new Floor(Integer.valueOf(components[2]));
		final Floor destination = new Floor(Integer.valueOf(components[3]));
		int ERROR_TYPE = 0;
		if(components.length > 4) {
			ERROR_TYPE = Integer.valueOf(components[4]);
		}
		
		return new FloorEvent(now + timeOffset, floorButton, source, destination, ERROR_TYPE);
	}
	
	private static void printLoadedEvent(final FloorEvent e) {
		System.out.printf("Loaded event from %s -> %s at %s\n", e.getSource(), e.getDestination(), new Date(e.getTime()));
	}
}
