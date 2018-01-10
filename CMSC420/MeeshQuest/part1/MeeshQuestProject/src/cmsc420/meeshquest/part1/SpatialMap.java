package cmsc420.meeshquest.part1;

import java.util.ArrayList;
import java.util.TreeMap;

public class SpatialMap {
	private PRQuadtree prTree;
	private DataDictionary dataDict;
	public final int spatialWidth;
	public final int spatialHeight;
	
	public SpatialMap(PRQuadtree prTree, DataDictionary dataDict, int width, int height) {
		this.prTree = prTree;
		this.dataDict = dataDict;
		this.spatialWidth = width;
		this.spatialHeight = height;
	}
	
	/**
	 * 
	 * @param name
	 * @return 0 -- Successfully inserted City into tree
	 * @return 1,2,3 -- ERRORs: nameNotInDictionary, cityAlreadyMapped, cityOutOfBoundss
	 */
	public int map(String name) {
		City c = dataDict.getCity(name);
		
		if(c == null) {
			// ERROR: nameNotInDictionary -- given City name doesn't exist 
			return 1;
		} else if(dataDict.isMapped.contains(name)) {
			// ERROR: cityAlreadyMapped -- given City has already been mapped to the spatial map
			return 2;
		} else if(outOfBounds(c)) {
			// ERROR: cityOutOfBounds -- given City's coordinates are out of bounds (spatialWidth x spatialHeight)
			return 3;
		} else {
			// No errors, insert City into PRQuadtree
			prTree.insert(c);
			//System.out.println("PRTree after adding "+c.name+": "+prTree.toString());
			dataDict.isMapped.add(name);
			return 0;
		}
	}
	
	public int unmap(String name) {
		City c = dataDict.getCity(name);
		
		if(c == null) {
			// ERROR: nameNotInDictionary -- given City name doesn't exist
			return 1;
		} else if(!dataDict.isMapped.contains(name)) {
			// ERROR: cityNotMapped -- given City is not mapped 
			return 2;
		} else {
			prTree.delete(c);
			dataDict.isMapped.remove(name);
			return 0;
		}
	}
	
	public TreeMap<String, City> range(int x, int y, int radius) {
		TreeMap<String, City> citiesInRange = prTree.range(x, y, radius);
		if(citiesInRange.size() == 0) {
			return null;
		} else {
			return citiesInRange;
		}
	}
	
	// range(), nearest(), ...
	
	
	/* ****************************************** */
	/* ***** Helper functions ******************* */
	/* ****************************************** */
	
	private boolean outOfBounds(City c) {
		if(c.getX() > spatialHeight || c.getY() > spatialWidth) 
			return true;
		else return false;
	}
	
}
