package cmsc420.meeshquest.part2;

public class RoadAlreadyMappedException extends Throwable {
	private static final long serialVersionUID = 5726326619703566538L;

	public RoadAlreadyMappedException() {
	}

	public RoadAlreadyMappedException(String message) {
		super(message);
	}
}
