package cmsc420.meeshquest.part2;

import cmsc420.meeshquest.p1canonical.*;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import cmsc420.geom.Geometry2D;

public class Road extends Line2D.Float implements Geometry2D, Comparable<Road> {
	
	/** Tag only used when the start and end cities were swapped */
	public boolean SWAPPED = false;
	
	/* Start and end points for this Road */
	private final City start, end;
	
	/* Name will be "startCity+endCity" */
	private final String name;
	
	public Road(City start, City end) {
		this.start = start;
		this.end = end;
		this.name = start.getName()+end.getName();
		
		/* Start and end coordinates will always be the 
		 * coordinates of the start and end Cities */
		setLine(start.getX(), start.getY(), end.getX(), end.getY());
	}
	
	public Road(City start, City end, boolean swap) {
		this.start = start;
		this.end = end;
		this.name = start.getName()+end.getName();
		SWAPPED = swap;
		
		/* Start and end coordinates will always be the 
		 * coordinates of the start and end Cities */
		setLine(start.getX(), start.getY(), end.getX(), end.getY());
	}
	
	/** Return City at start point of road */
	public City getStartCity() {
		return start;
	}
	
	/** Return City at endpoint of road */
	public City getEndCity() {
		return end;
	}
	
	/** Return name of the Road (startCity.name +endCity.name) */
	public String getName() {
		return name;
	}
	
	/** Return length of Road from startCity to endCity */
	public double getLength() {
		return start.toPoint2D().distance(end.toPoint2D());
	}
	
	/** Return SEGMENT flag according to Geometry2D interface */
	@Override
	public int getType() {
		return 1;
	}

	public String toString() {
		return "Edge " + this.getName();
	}
	
	public int hashCode() {
		return start.hashCode() + end.hashCode();
	}
	
	/** 
	 * this is equivalent to 'r' if start city is same as other start or other end
	 * or vice versa
	 */
	public boolean equals(Road r) {
		if( (r.getStartCity().equals(this.getStartCity()) || r.getStartCity().equals(this.getEndCity())) && 
			(r.getEndCity().equals(this.getStartCity()) || r.getEndCity().equals(this.getEndCity()) )) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(Road o) {
		if(this.getStartCity().getName().compareTo(o.getStartCity().getName()) > 0) {
			return -1;
		} else if(this.getStartCity().getName().compareTo(o.getStartCity().getName()) < 0) {
			return 1;
		} else {
			// Start names are equal 
			if(this.getEndCity().getName().compareTo(o.getEndCity().getName()) > 0) {
				return -1;
			} else if(this.getEndCity().getName().compareTo(o.getEndCity().getName()) < 0) {
				return 1;
			} else {
				// End names are equal too
				return 0;
			}
		}
	}
	
}
