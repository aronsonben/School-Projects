package cmsc420.geometry ;


import java.awt.geom.Point2D;
import java.util.Comparator;

/**
 * Compares two cities based on location of x and y coordinates. 
 * 
 * Added by Ben Aronson on 7/20/17:
 * First compares remote coordinates.
 * 
 * First compares the x values of each {@link City}. If the x values are 
 *  the same, then the y values of each City are compared.
 * 
 * @author Ben Zoller
 * @editor Ruofei Du
 * @version 1.0, 23 Jan 2007
 */
public class CityLocationComparator implements Comparator<City> {

	public int compare(final City one, final City two) {
		// compare remote coords first
		Point2D remote1 = one.getRemotePt();
		Point2D remote2 = two.getRemotePt();
		if(remote1.getX() < remote2.getX()) {
			return -1;
		} else if(remote1.getX() > remote2.getY()) {
			return 1;
		} else {
			if (one.getY() < two.getY()) {
				return -1;
			} else if (one.getY() > two.getY()) {
				return 1;
			} else {
				/* one.getY() == two.getY() */
				if (one.getX() < two.getX()) {
					return -1;
				} else if (one.getX() > two.getX()) {
					return 1;
				} else {
					/* one.getX() == two.getX() */
					return 0;
				}
			}
		}
		
		
	}
}