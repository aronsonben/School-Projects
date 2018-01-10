package cmsc433.p4.util;

import java.util.HashMap;

import akka.actor.ActorRef;

/**
 * -Class to help with searching each remote ResourceManagerActor to determine which (if any) have 
 * 		local access to a given resource (resourceName).
 * -Keeps track of each RM in a table with a Boolean value, where the value is true if the RM has
 * 		local access to the resource.
 * -The ownCount starts at 0, will be increased when a RM with local access is found. Otherwise stays at 0.
 * @author Ben Aronson
 */
public class ResourceLocator {
	
	private String resourceName;
	private HashMap<ActorRef, Boolean> resourceFound;
	private int ownCount = 0;
	private int checkedCount = 0;

	public ResourceLocator(String resourceName, HashMap<ActorRef, Boolean> resourceFound) {
		this.resourceName = resourceName;
		this.resourceFound = resourceFound;
	}
	
	public void updateLocator(ActorRef remoteManager, boolean result) {
		if(result) {
			ownCount++;
		} 
		resourceFound.put(remoteManager, result);
		checkedCount++;
	}
	
	public void increaseOwnCount() {
		ownCount++;
	}
	
	// Getters //
	
	public int getOwnCount() {
		return ownCount;
	}
	
	public int getCheckedCount() {
		return checkedCount;
	}
	public String getResourceName() {
		return resourceName;
	}
	
	public HashMap<ActorRef, Boolean> getResourceFoundTable() {
		return resourceFound;
	}
	
}
