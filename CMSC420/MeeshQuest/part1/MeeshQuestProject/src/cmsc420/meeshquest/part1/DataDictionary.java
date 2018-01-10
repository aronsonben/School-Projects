package cmsc420.meeshquest.part1;

import java.util.ArrayList;
import java.util.TreeMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DataDictionary {
	private TreeMap<String, City> nameMap;
	private TreeMap<City, String> locationMap;
	//private TreeMap<String, Boolean> isMapped;
	public ArrayList<String> isMapped;
	
	public DataDictionary(TreeMap<String, City> nameMap, TreeMap<City, String> locMap, ArrayList<String> isMapped) {
		this.nameMap = nameMap;
		this.locationMap = locMap;
		this.isMapped = isMapped;
	}
	
	/**
	 * Function to create a city and add it to the data dictionary if success, return error otherwise
	 * @return 0 - successfully added new City object to data dictionary
	 * @return 1 - ERROR: "duplicateCityCoordinates"
	 * @return 2 - ERROR: "duplicateCityName"
	 */
	public int createCity(String name, int x, int y, int radius, String color) {
		City newCity = new City(name, x, y, radius, color);
		
		if(checkLocation(newCity)) {
			// ERROR: duplicateCityCoordinates - if locationMap already has City at coordinates (x,y)
			return 1;
		}
		else if(nameMap.containsKey(name)) {
			// ERROR: duplicateCityName
			return 2;
		}
		else {
			// No error: Add new City to TreeMaps
			nameMap.put(name, newCity);
			locationMap.put(newCity, name);
			return 0;
		}
	}
	
	
	public void listCities(Document results, Element listTag, String sortBy) {
		if(sortBy.equals("coordinate")) {
			
			for(City c : locationMap.keySet()) {
				Element city = results.createElement("city");
				Double x = new Double(c.getX());
				Double y = new Double(c.getY());
				Double radius = new Double(c.radius);
				city.setAttribute("name", c.name);
				city.setAttribute("x", String.valueOf(x.intValue()));
				city.setAttribute("y", String.valueOf(y.intValue()));
				city.setAttribute("color", c.color);
				city.setAttribute("radius", String.valueOf(radius.intValue()));
				listTag.appendChild(city);
			}
			
		} else {	// sortBy is "name"
			
			for(String name : nameMap.keySet()) {
				City c = nameMap.get(name);
				Element city = results.createElement("city");
				Double x = new Double(c.getX());
				Double y = new Double(c.getY());
				Double radius = new Double(c.radius);
				city.setAttribute("name", c.name);
				city.setAttribute("x", String.valueOf(x.intValue()));
				city.setAttribute("y", String.valueOf(y.intValue()));
				city.setAttribute("color", c.color);
				city.setAttribute("radius", String.valueOf(radius.intValue()));
				listTag.appendChild(city);
			}
		}
	}
	
	public void clearAll() {
		nameMap.clear();
		locationMap.clear();
		isMapped.clear(); 
		//System.out.println("Cleared all "+nameMap.toString()+" - "+locationMap.toString()+" - "+isMapped.toString());
	}
	
	public void delete(City c) {
		nameMap.remove(c.name);
		locationMap.remove(c);
		//System.out.println("Deleted "+c.name+". Now: "+nameMap.toString()+" and "+locationMap.toString());
	}
	
	public String toString() { 
		return nameMap.toString() + " - " + locationMap.toString() + " - " + isMapped.toString();
	}
	
	
	/* ********************************************* */
	/* ******* Private auxiliary functions ********* */
	/* ********************************************* */
	
	/**Function to check if a city already exists in this location
	 * @return true - if a city does exist in given location
	 */
	private boolean checkLocation(City newCity) {
		for(City c : locationMap.keySet()) {
			if(newCity.equals(c)) {
				return true;
			}
		}
		return false;
	}
	
	
	/* ********************************************* */
	/* ******* Public   helper	 functions ********* */
	/* ********************************************* */
	
	/** Function called in "listCities" during XML Parsing to make 
	 * sure cities exist in dataDictionary before creating output.
	 * @return true if nameMap.isEmpty && locationMap.isEmpty (just to double check)
	 */
	public boolean dictEmpty() {
		if(nameMap.isEmpty() && locationMap.isEmpty())
			return true;
		else return false;
	}
	
	/** Return given City if it exists in the dataDictionary, null otherwise */
	public City getCity(String name) {
		if(nameMap.containsKey(name))
			return nameMap.get(name);
		else return null;
		
	}
	
	
}
