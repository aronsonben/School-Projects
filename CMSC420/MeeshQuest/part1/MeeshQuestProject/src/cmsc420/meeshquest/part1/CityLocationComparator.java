package cmsc420.meeshquest.part1;

import java.util.Comparator;

public class CityLocationComparator implements Comparator<City> {

	public CityLocationComparator() {
	}
	
	@Override
	public int compare(City c1, City c2) {
		double yC1 = c1.getY();
		double yC2 = c2.getY();
		if(yC1 == yC2) {
			
			double xC1 = c1.getX();
			double xC2 = c2.getX();
			if(xC1 == xC2) {
				return 0;
			} else if(xC1 > xC2) {
				return 2;
			} else {
				return -2;
			}
			
		} else if(yC1 > yC2) {
			return 2;
		} else {
			return -2;
		}
	}

}
