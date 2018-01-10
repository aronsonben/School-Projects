package cmsc420.meeshquest.part1;

import java.awt.geom.Point2D;

public class City extends Point2D.Float {
	public final String name;
	public final String color;
	public final float radius;
	
	public City(String name, float x, float y, float radius, String color) {
		this.name = name;
		this.color = color;
		this.radius = radius;
		setLocation(x, y);
	}

	public String getCoordString() {
		return "(" + getX() + ", " + getY() + ")";
	}
	
	public String toString() {
		return "[" + name + " ("+getX()+", "+getY()+")]";
	}
}