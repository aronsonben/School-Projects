package cmsc420.meeshquest.p1canonical;

/**
 * Thrown if a city attempted to be mapped is outside the bounds of the
 * spatial map.
 */
public class CityOutOfBoundsException extends Throwable {
	private static final long serialVersionUID = -6878077114302943595L;

	public CityOutOfBoundsException() {
	}

	public CityOutOfBoundsException(String message) {
		super(message);
	}
}
