package cmsc420.geometry;

public class Metropole extends City {

	/** 
	 * Same idea as City objects except the coordinates being passed in are 
	 * 	remote coordinates instead of local. Use radius of 1 as default
	 * 	metropole radius.
	 */
	public Metropole(String name, int x, int y, String color) {
		super(name, x, y, 1, color);
	}
	
	/** 
	 * Turns this Metropole object into a City object
	 */
	public City toCity() {
		return new City(this);
	}

}
