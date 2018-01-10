package cmsc433.p4.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import akka.actor.ActorRef;
import cmsc433.p4.enums.AccessType;
import cmsc433.p4.enums.ResourceStatus;
import cmsc433.p4.messages.AccessRequestMsg;

public class AccessHandler {
	
	private String resourceName;
	private boolean availableToRead = true;
	private boolean availableToWrite = true;	// use this to let multiple concurrent read accesses but no writes
	private AccessType lockedOnType = null;
	private ResourceStatus status; 
	
	// Exclusive-Write requests
	private ActorRef exclusiveWriter;
	private int numWriteAccesses = 0;			// how many times the exclusiveWriter used Re-entrant locking for its write access
	
	// Concurrent-Read requests
	private HashMap<ActorRef, Integer> readAccessList; 	// each unique actor that has read access -> how many accesses they have
	
	// All requests
	private Queue<Object> blockingRequestQueue;
	
	public AccessHandler(String resourceName) {
		this.resourceName = resourceName;
		exclusiveWriter = null;
		readAccessList = new HashMap<ActorRef, Integer>();
		blockingRequestQueue = new LinkedList<Object>();
		status = ResourceStatus.ENABLED;
	}
	
	/**
	 * Set the actor that has exclusive write access to this resource, if resource is available. Resource then becomes unavailable.
	 * @param requestor - actor that is requesting access
	 * @return false - if resource not available
	 */
	public boolean setExclusiveWrite(ActorRef requestor) {
		if(writeIsAvailable(requestor)) {
			exclusiveWriter = requestor;
			numWriteAccesses++;
			availableToRead = false;
			availableToWrite = false;
			lockedOnType = AccessType.EXCLUSIVE_WRITE;
			//System.out.println("Set exclusive write for " + requestor.path().name() + " in " + resourceName + " access request handler.");
			return true;
		} else {
			return false;
		}
	}
	
	/** Release lock writer had on resource, make it available again */
	public int releaseExclusiveWriter() {
		numWriteAccesses--;
		// If releasing the last or only access for this writer, reset exclusiveWriter, etc.
		if(numWriteAccesses == 0) {
			exclusiveWriter = null;
			availableToRead = true;
			availableToWrite = true;
			lockedOnType = null;
			//System.out.println("Released exclusive write for " + exclusiveWriter.path().name() + " in " + resourceName + " access request handler.");
		}
		return numWriteAccesses;
	}
	
	public void addExclusiveWriter() {
		numWriteAccesses++;
	}
	
	/** If a requestor is asking for concurrent read access, add to list of all actors that can read resource */
	public boolean addConcurrentReader(ActorRef requestor) {
		if(readIsAvailable(requestor)) {
			if(!readAccessList.containsKey(requestor)) {
				readAccessList.put(requestor, 1);
				availableToRead = true;
				availableToWrite = false;
				lockedOnType = AccessType.CONCURRENT_READ;
			} else {
				int requestorAccesses = readAccessList.get(requestor);
				requestorAccesses++;
				readAccessList.put(requestor, requestorAccesses);
			}
			return true;
		} else { 
			return false;
		}
	}
	
	/** Release a concurrent reader's access. If it was the only reader, resource now becomes available */
	public boolean removeConcurrentReader(ActorRef requestor) {
		if(readAccessList.size() > 0 && readAccessList.containsKey(requestor)) {
			int requestorAccessess = readAccessList.get(requestor);
			if(requestorAccessess <= 1) {
				//System.out.println("removing instance of reader: " + requestor.path().name());
				readAccessList.remove(requestor);
				availableToRead = true;
				availableToWrite = true;
				lockedOnType = null;
				if(readAccessList.size()==0) { 
					return true;
				} else {
					return false;
				}
			} else {
				requestorAccessess--;
				readAccessList.put(requestor, requestorAccessess);
				return false;
			}
		} else {
			System.out.println("Didn't remove read access in " + resourceName);
			return false;
		}
	}
	
	/** Return true if resource is available for nonblocking access (meaning no user has exclusive write access) */
	public boolean writeIsAvailable(ActorRef requestor) {
		if(exclusiveWriter == null && 
				( (readAccessList.size() == 0) || ( readAccessList.size() == 1 && readAccessList.containsKey(requestor) ) ) ) {
			return true;
		} else { 
			return false;
		}
	}
	
	public boolean readIsAvailable(ActorRef requestor) {
		if(exclusiveWriter == null || exclusiveWriter.equals(requestor)) {
			return true;
		} else {
			return false;
		}
	}
	
	/** Actor calls this to see if it has exclusiveWrite or concurrentRead access */
	public boolean hasAccess(ActorRef requestor) {
		if( exclusiveWriter == null || readAccessList.size() == 0) {
			return false;
		} else if( exclusiveWriter != null && exclusiveWriter.equals(requestor) ) {
			return true;
		} else if( readAccessList.containsKey(requestor) ) {
			return true;
		} else {
			return false;
		}
	}
	
	/** Return true if there is an exclusiveWriter or there are actors reading concurrently */
	public boolean resourceBlocked() {
		if(exclusiveWriter != null || readAccessList.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public void disable() {
		status = ResourceStatus.DISABLED;
	}
	
	public void enable() {
		status = ResourceStatus.ENABLED;
	}
	
	///// Getters /////
	
	/** Return the actor that currently has exclusive write access. Returns null if no such actor exists. */
	public ActorRef getExclusiveWriteActor() {
		return exclusiveWriter;
	}
	
	/**
	 * Return list of all actors that have read access to this resource. Repetition allowed 
	 *  (in the case that an actor has multiple read accesses).
	 */
	public HashMap<ActorRef, Integer> getReadAccessActors() {
		return readAccessList;
	}
	
	/** Return the AccessType this resource is locked on. Return null if neither. */
	public AccessType getLockedType() {
		return lockedOnType;
	}
	
	public String getResourceName() {
		return resourceName;
	}
	
	public Queue<Object> getBlockingRequests() {
		return blockingRequestQueue;
	}
	
	
	public String toString() {
		return resourceName + " handler has [ ExclusiveWriter = " + exclusiveWriter + "; ConcurrentReaders = " + readAccessList.toString() +
				" ] -- Write Available = " + availableToWrite + "; Read Available = " + availableToRead;
	}
}
