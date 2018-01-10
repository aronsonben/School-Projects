package cmsc420.meeshquest.part2;

import java.util.Comparator;

public class RoadComparator implements Comparator<Road> {

	@Override
	public int compare(Road arg0, Road arg1) {
		if(arg0.getStartCity().getName().compareTo(arg1.getStartCity().getName()) > 0) {
			return -1;
		} else if(arg0.getStartCity().getName().compareTo(arg1.getStartCity().getName()) < 0) {
			return 1;
		} else {
			// Start names are equal 
			if(arg0.getEndCity().getName().compareTo(arg1.getEndCity().getName()) > 0) {
				return -1;
			} else if(arg0.getEndCity().getName().compareTo(arg1.getEndCity().getName()) < 0) {
				return 1;
			} else {
				// End names are equal too
				return 0;
			}
		}
	}

}
