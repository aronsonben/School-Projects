package cmsc420.meeshquest.part1;

import java.util.Comparator;

public class CityNameComparator implements Comparator<String> {

	public CityNameComparator() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int compare(String arg0, String arg1) {
		return arg1.compareTo(arg0);
	}
	
}
