package cmsc433.p4.actors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import cmsc433.p4.enums.*;
import cmsc433.p4.messages.*;
import cmsc433.p4.util.*;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class ResourceManagerActor extends UntypedActor {
	
	private ActorRef logger;					// Actor to send logging messages to
	
	private String rmName = getSelf().path().name();
	
	///// Configuration fields //////
	private ArrayList<Resource> localResources = new ArrayList<Resource>();
	private ArrayList<ActorRef> localUsers = new ArrayList<ActorRef>();
	private ArrayList<ActorRef> remoteManagers = new ArrayList<ActorRef>();
	
	///// General "keeping track of" fields /////
	
	// Table that maps each resource to the ResourceManager that owns it locally
	private HashMap<String, ActorRef> resourceByRM = new HashMap<String, ActorRef>();
	
	
	///// Request forwarding fields /////
	
	// List of resources that are found to be non existant in the system
	private ArrayList<String> nonexistent = new ArrayList<String>();
	
	// Record table of number of RMs that do not have local access to specific resource
	private HashMap<String, Integer> notFoundResponses = new HashMap<String, Integer>();
	
	
	// Table that maps each resource to its Locator object (see ResourceLocataor class for further explanation)
	private HashMap<String, ResourceLocator> resourceLocators = new HashMap<String, ResourceLocator>();
	
	// Table to keep track of requests per resource
	// Table that maps each resource to a queue of requests issued by users requesting access to a given resource
	private HashMap<String, Queue<Object>> requestTracker = new HashMap<String, Queue<Object>>();
	
	
	
	///// Local AccessRequest Handling fields /////
	private HashMap<String, AccessHandler> accessHandlers = new HashMap<String, AccessHandler>();
	
	// Blocking queues keyed by resource name
	//private HashMap<String, Queue<Object>> pendingRequests = new HashMap<String, Queue<Object>>();
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////
	/////////// End field creation ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Props structure-generator for this class.
	 * @return  Props structure
	 */
	static Props props (ActorRef logger) {
		return Props.create(ResourceManagerActor.class, logger);
	}
	
	/**
	 * Factory method for creating resource managers
	 * @param logger			Actor to send logging messages to
	 * @param system			Actor system in which manager will execute
	 * @return					Reference to new manager
	 */
	public static ActorRef makeResourceManager (ActorRef logger, ActorSystem system) {
		ActorRef newManager = system.actorOf(props(logger));
		return newManager;
	}
	
	/**
	 * Sends a message to the Logger Actor
	 * @param msg The message to be sent to the logger
	 */
	public void log (LogMsg msg) {
		//System.out.println(msg);
		logger.tell(msg, getSelf());
	}
	
	/**
	 * Constructor
	 * 
	 * @param logger			Actor to send logging messages to
	 */
	private ResourceManagerActor(ActorRef logger) {
		super();
		this.logger = logger;
		
	}

	// You may want to add data structures for managing local resources and users, storing
	// remote managers, etc.
	
	/* (non-Javadoc)
	 * 
	 * You must provide an implementation of the onReceive method below.
	 * 
	 * @see akka.actor.UntypedActor#onReceive(java.lang.Object)
	 */
	@Override
	public void onReceive(Object msg) throws Exception {
		
		if(msg instanceof AddInitialLocalResourcesRequestMsg) {
			// FROM: Called from Systems.makeSystem when creating new RMs for each node
			// GOAL: Create list of local resources available to this RM
			
			ActorRef sender = getSender();
			AddInitialLocalResourcesRequestMsg rmsg = (AddInitialLocalResourcesRequestMsg) msg;
			
			// Deep copy resource list from msg
			ArrayList<Resource> locResourcesTemp = new ArrayList<Resource>();
			locResourcesTemp = rmsg.getLocalResources();
			localResources.addAll(locResourcesTemp);
			
			for(int i=0; i < localResources.size(); i++) {
				Resource loc = localResources.get(i);
				log(LogMsg.makeLocalResourceCreatedLogMsg(getSelf(), loc.name));
				loc.enable();
				log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), loc.name, ResourceStatus.ENABLED));
				// Add to <Resource, ResourceManager> table
				resourceByRM.put(loc.getName(), null);
				
				// Instantiate the AccessHandler for later, if/when this resource gets AccessRequests
				AccessHandler handler = new AccessHandler(loc.getName());
				accessHandlers.put(loc.getName(), handler);
				
				notFoundResponses.put(loc.getName(), 0);
			}
			
			AddInitialLocalResourcesResponseMsg response = new AddInitialLocalResourcesResponseMsg(rmsg);
			sender.tell(response, getSelf());
		}
		else if(msg instanceof AddLocalUsersRequestMsg) {
			// FROM: Called from Systems.makeSystem when creating User list for each node
			// GOAL: Add local users that exist in Node 
			
			ActorRef sender = getSender();
			AddLocalUsersRequestMsg rmsg = (AddLocalUsersRequestMsg) msg;
			
			ArrayList<ActorRef> locUsersTemp = new ArrayList<ActorRef>();
			locUsersTemp = rmsg.getLocalUsers();
			localUsers.addAll(locUsersTemp);
			
			AddLocalUsersResponseMsg response = new AddLocalUsersResponseMsg(rmsg);
			sender.tell(response, getSelf());
		}
		else if(msg instanceof AddRemoteManagersRequestMsg) {
			// FROM: Called from Systems.makeSystem after each node created
			// GOAL: Update the list of remote managers that current RM has access to
			
			ActorRef sender = getSender();
			AddRemoteManagersRequestMsg rmsg = (AddRemoteManagersRequestMsg) msg;
			
			ArrayList<ActorRef> remoteManagerTemp = new ArrayList<ActorRef>();
			remoteManagerTemp = rmsg.getManagerList();
			
			for(int i=0; i < remoteManagerTemp.size(); i++) {
				ActorRef remoteRM = remoteManagerTemp.get(i);
				if( !remoteRM.equals(getSelf()) ) {
					remoteManagers.add(remoteRM);
				}
			}
			
			AddRemoteManagersResponseMsg response = new AddRemoteManagersResponseMsg(rmsg);
			sender.tell(response, getSelf());
			
		}
		else if(msg instanceof AccessRequestMsg) {
			// FROM: UserActor.sendNextMsgs - will be called by a local User looking to make an Access request
			// GOAL: Issue / handle an Access request from this User
			
			AccessRequestMsg rmsg = (AccessRequestMsg) msg;
			
			//System.out.println(msg.toString() + " from " + getSender().path().name() + " for RM " + getSelf().path().name());
			
			ActorRef sender 				= rmsg.getReplyTo();
			AccessRequest request 			= rmsg.getAccessRequest();
			AccessRequestType requestType 	= request.getType();
			String resourceName 			= request.getResourceName();
			Resource resource;
			
			log(LogMsg.makeAccessRequestReceivedLogMsg(sender, getSelf(), request));
			
			//System.out.println("Msg sent by " + getName(msgSender) + " - Request sent by " + getName(sender));
			
			resource = findLocalResource(resourceName);
			
			// Requested resource does not exist locally - find manager that handles this resource
			if(resource == null) {
				
				ResourceLocator fwdRequestSearch;
				HashMap<ActorRef, Boolean> resourceRMFound;
				Queue<Object> requestList = new LinkedList<Object>();
				
				ActorRef forwardTo = resourceByRM.get(resourceName);
				if(forwardTo == null) {
					// Need to find appropriate remote RM
					//System.out.println(rmName + " about to search for " + resourceName + "...");
					
					if(!requestTracker.containsKey(resourceName)) {
						
						resourceRMFound 	= new HashMap<ActorRef, Boolean>();
						fwdRequestSearch 	= new ResourceLocator(resourceName, resourceRMFound);
						resourceLocators.put(resourceName, fwdRequestSearch);
						
						requestList 		= new LinkedList<Object>();
						requestList.add(rmsg);
						requestTracker.put(resourceName, requestList);
						
						
						WhoHasResourceRequestMsg amsg = new WhoHasResourceRequestMsg(resourceName, rmsg);
						for(ActorRef remoteRM : remoteManagers) {
							fwdRequestSearch.updateLocator(remoteRM, false);
							//System.out.println(rmName + " sending probe to " + getName(remoteRM));
							remoteRM.tell(amsg, getSelf());
						}
						
					} else {
						//System.out.println(rmName + " has already started searching for " + resourceName);
						// The RM is already searching for this the RM that has this Resource, add request to waiting requests
						Queue<Object> requestQueue = requestTracker.get(resourceName);
						requestQueue.add(rmsg);
					}
				} else {
					forwardTo.tell(msg, getSelf());
					log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), forwardTo, request));
				}
				//System.out.println("Ending " + rmsg.toString() + " by " + getName(sender) + " in " + rmName);
			} else {
				// LOCAL processing of AccessRequest
				
				AccessHandler handler = accessHandlers.get(resourceName);
				Queue<Object> blockingQueue = handler.getBlockingRequests();
				Object checkDisableObj = blockingQueue.peek();
				
				if(resource.getStatus() == ResourceStatus.DISABLED || checkDisableObj instanceof ManagementRequestMsg) {
					// Deny access - resource is disabled (or first blocked item is a disable, then its waiting to be disabled)
					//System.out.println(resourceName + " is either disabled or waiting to be disabled");
					
					AccessRequestDeniedMsg denyMsg = new AccessRequestDeniedMsg(rmsg, AccessRequestDenialReason.RESOURCE_DISABLED);
					sender.tell(denyMsg, getSelf());
					log(LogMsg.makeAccessRequestDeniedLogMsg(sender, getSelf(), request, AccessRequestDenialReason.RESOURCE_DISABLED));
					
				} else {
					// TODO: AccessRequestMsg - Handle Local AccessRequests
					
					if(requestType == AccessRequestType.EXCLUSIVE_WRITE_NONBLOCKING) {
						//ActorRef writer = handler.getExclusiveWriteActor();
						// Allow actor to write if resource is available
						if(handler.writeIsAvailable(sender)) {
							//System.out.println("Available " + requestType.name() + " for " + getName(sender) + " in " + rmName);
							handler.setExclusiveWrite(sender);
							AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(rmsg);
							sender.tell(grantMsg, getSelf());
							log(LogMsg.makeAccessRequestGrantedLogMsg(sender, getSelf(), request));
						} else {
							log(LogMsg.makeAccessRequestDeniedLogMsg(sender, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
						}
					}
					else if(requestType == AccessRequestType.CONCURRENT_READ_NONBLOCKING) {
						//ArrayList<ActorRef> readerList = handler.getReadAccessActors();
						// Allow actor to read if resource is available
						//System.out.println(getName(sender) + " trying to concurrent read (nb) " + resourceName);
						if(handler.readIsAvailable(sender)) {
							//System.out.println("Available " + requestType.name() + " for " + getName(sender) + " in " + rmName);
							handler.addConcurrentReader(sender);
							AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(rmsg);
							sender.tell(grantMsg, getSelf());
							//System.out.println("AccessRequestGrantedMsg use in " + grantMsg.toString() + " for " + getName(sender));
							log(LogMsg.makeAccessRequestGrantedLogMsg(sender, getSelf(), request));
							//System.out.println(resourceName + " readers: " + handler.getReadAccessActors().toString());
						} else {
							log(LogMsg.makeAccessRequestDeniedLogMsg(sender, getSelf(), request, AccessRequestDenialReason.RESOURCE_BUSY));
						}
					}
					else if(requestType == AccessRequestType.EXCLUSIVE_WRITE_BLOCKING) {
						//ActorRef writer = handler.getExclusiveWriteActor();
						//System.out.println("New blocking request on " + resourceName + " by " + getName(sender));
						
						if(handler.writeIsAvailable(sender)) {
							//System.out.println("Available " + requestType.name() + " for " + getName(sender) + " in " + rmName);
							handler.setExclusiveWrite(sender);
							AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(rmsg);
							sender.tell(grantMsg, getSelf());
							//System.out.println("Exclusive writer for " + resourceName + " is " + getName(handler.getExclusiveWriteActor()));
							log(LogMsg.makeAccessRequestGrantedLogMsg(sender, getSelf(), request));
						} else {
							//System.out.println("Blocking request needs to wait on " + resourceName + " for " + getName(sender) + " in " + rmName);
							Queue<Object> resourceBlockingQueue = handler.getBlockingRequests();
							resourceBlockingQueue.add(rmsg);
						}
					} else {
						//ArrayList<ActorRef> readerList = handler.getReadAccessActors();
						if(handler.readIsAvailable(sender)) {
							//System.out.println("Available " + requestType.name() + " for " + getName(sender) + " in " + rmName);
							handler.addConcurrentReader(sender);
							sender.tell(rmsg, getSelf());
							AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(rmsg);
							sender.tell(grantMsg, getSelf());
							log(LogMsg.makeAccessRequestGrantedLogMsg(sender, getSelf(), request));
						} else {
							//System.out.println("Blocking request needs to wait on " + resourceName);
							Queue<Object> resourceBlockingQueue = handler.getBlockingRequests();
							resourceBlockingQueue.add(rmsg);
						}
					}
				}
				//System.out.println("Ending " + rmsg.toString() + " by " + getName(sender) + " in " + rmName);
			}
			// end AccessRequestMsg
		}
		else if(msg instanceof ManagementRequestMsg) {
			//System.out.println("Management Request Message by: " + getSender().path().name());
			// FROM: UserActor.sendNextMsgs - will be called by a local User looking to make a Management request
			// GOAL: Issue / handle a Management request from this User
			
			ManagementRequestMsg rmsg = (ManagementRequestMsg) msg;
			
			//ActorRef msgSender 					= getSender();
			ActorRef sender 					= rmsg.getReplyTo();
			ManagementRequest request			= rmsg.getRequest();
			String resourceName 				= request.getResourceName();
			ManagementRequestType requestType 	= request.getType();
			Resource resource					= findLocalResource(resourceName);
			
			log(LogMsg.makeManagementRequestReceivedLogMsg(sender, getSelf(), request));
			
			if(resource == null) {
				ResourceLocator fwdRequestSearch;
				HashMap<ActorRef, Boolean> resourceRMFound;
				Queue<Object> requestList;
				
				ActorRef forwardTo = resourceByRM.get(resourceName);
				if(forwardTo == null) {
					// Need to find appropriate remote RM
					
					if(!requestTracker.containsKey(resourceName)) {
						
						resourceRMFound 	= new HashMap<ActorRef, Boolean>();
						fwdRequestSearch 	= new ResourceLocator(resourceName, resourceRMFound);
						resourceLocators.put(resourceName, fwdRequestSearch);
						
						requestList 		= new LinkedList<Object>();
						requestList.add(rmsg);
						requestTracker.put(resourceName, requestList);
						
						
						WhoHasResourceRequestMsg amsg = new WhoHasResourceRequestMsg(resourceName, rmsg);
						for(ActorRef remoteRM : remoteManagers) {
							fwdRequestSearch.updateLocator(remoteRM, false);
							//System.out.println(rmName + " sending probe to " + getName(remoteRM));
							remoteRM.tell(amsg, getSelf());
						}
						
					} else {
						//System.out.println(rmName + " has already started searching for " + resourceName);
						// The RM is already searching for this the RM that has this Resource, add request to waiting requests
						Queue<Object> requestQueue = requestTracker.get(resourceName);
						requestQueue.add(rmsg);
					}
				}
			} else {
				
				// TODO: management request
				AccessHandler handler = accessHandlers.get(resourceName);
				
				if(requestType == ManagementRequestType.DISABLE) {
					
					// Check if requestor has access to any local resources
					for(Resource r : localResources) {
						handler = accessHandlers.get(r.name);
						if(handler.hasAccess(sender)) {
							ManagementRequestDeniedMsg denyMsg = new ManagementRequestDeniedMsg(rmsg, ManagementRequestDenialReason.ACCESS_HELD_BY_USER);
							sender.tell(denyMsg, getSelf());
							log(LogMsg.makeManagementRequestDeniedLogMsg(sender, getSelf(), request, ManagementRequestDenialReason.ACCESS_HELD_BY_USER));
							break; // needed?
						} 
					}
					
					handler = accessHandlers.get(resourceName);
					Queue<Object> pendingBlockingRequests = handler.getBlockingRequests();
					Object nextRequestObj;

					
					// If resource is being accessed by another actor, do work with blocking queue
					if(handler.resourceBlocked()) {
						
						if(pendingBlockingRequests.size() > 0) {
							
							nextRequestObj = pendingBlockingRequests.peek();
							
							// Check if the first blockingQueue element is a disable request -> if so add this to queue
							if(nextRequestObj instanceof ManagementRequestMsg) {
								// Disable messages are first, add new one to queue
								pendingBlockingRequests.add(rmsg);
							} else {
								// This means there is AT LEAST	one AccessRequestMsg blocking in queue so get rid of them
								
								while(pendingBlockingRequests.size() > 0) {
									 pendingBlockingRequests.poll();
									if(nextRequestObj instanceof AccessRequestMsg) {
										AccessRequestMsg nextRequest = (AccessRequestMsg) nextRequestObj;
										AccessRequest newRequest = nextRequest.getAccessRequest();
										ActorRef replyTo = nextRequest.getReplyTo();
										AccessRequestDeniedMsg denyMsg = new AccessRequestDeniedMsg(nextRequest, AccessRequestDenialReason.RESOURCE_DISABLED);
										replyTo.tell(denyMsg, getSelf());
										log(LogMsg.makeAccessRequestDeniedLogMsg(replyTo, getSelf(), newRequest, AccessRequestDenialReason.RESOURCE_DISABLED));
									}
									// shouldn't ever be anything other than AccessRequestMsg
								}
								pendingBlockingRequests.add(rmsg);
							}
							
						} else {
							// Nothing blocking, put deny request in blocking queue first
							//System.out.println("Resource blocked, add to first in blockingQueue of "+resourceName);
							pendingBlockingRequests.add(rmsg);
						}
						
					} else {
						// resource is available (no actor has write or read access at all) -> disable immediately
						resource.disable();
						handler.disable();
						ManagementRequestGrantedMsg grantMsg = new ManagementRequestGrantedMsg(rmsg);
						sender.tell(grantMsg, getSelf());
						log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resourceName, ResourceStatus.DISABLED));
						log(LogMsg.makeManagementRequestGrantedLogMsg(sender, getSelf(), request));
					}
					
				} else {
					handler = accessHandlers.get(resourceName);
					resource.enable();
					handler.enable();
					ManagementRequestGrantedMsg grantMsg = new ManagementRequestGrantedMsg(rmsg);
					sender.tell(grantMsg, getSelf());
					if(resource.getStatus() == ResourceStatus.DISABLED) {
						log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resourceName, ResourceStatus.ENABLED));
					} 
					log(LogMsg.makeManagementRequestGrantedLogMsg(sender, getSelf(), request));
				}
				
			}
		}
		else if(msg instanceof AccessReleaseMsg) {
			// FROM: UserActor.sendNextMsgs - will be called by a local User looking to make release Access
			// GOAL: Issue / handle a request to release Access for this User on given resource
			
			AccessReleaseMsg rmsg = (AccessReleaseMsg) msg;
			
			//ActorRef msgSender		= getSender();
			ActorRef sender 		= rmsg.getSender();
			AccessRelease release 	= rmsg.getAccessRelease();
			String resourceName		= release.getResourceName();
			AccessType requestType	= release.getType();
			Resource resource		= findLocalResource(resourceName);
			
			log(LogMsg.makeAccessReleaseReceivedLogMsg(sender, getSelf(), release));
			
			// Find resource remotely
			if(resource == null) {
				
				ActorRef forwardTo = resourceByRM.get(resourceName);
				if(forwardTo == null) {
					System.out.println("should it even be here?");
					// Remote RM has not been found for this resource, ignore request
					log(LogMsg.makeAccessReleaseIgnoredLogMsg(sender, getSelf(), release));
				} else {
					forwardTo.tell(rmsg, getSelf());
					log(LogMsg.makeAccessReleaseForwardedLogMsg(getSelf(), forwardTo, release));
				}
				
			} else {
				// TODO: AccessRelease - local processing 
				
				//System.out.println(rmName + " releasing " + resourceName + " for " + getName(sender));
				AccessHandler handler = accessHandlers.get(resourceName);
				
				if(requestType == AccessType.EXCLUSIVE_WRITE) {
					ActorRef writer = handler.getExclusiveWriteActor();
					
					// If the actor holding exclusive write access to this resource != requestor (sender) -> IGNORE
					if(writer == null || !writer.equals(sender) ) {
						//System.out.println(getName(sender) + " is requesting release for a resource ("+resourceName+") that it doesn't" +
						//					" hold access to. (" + getName(writer) + " has access)");
						
						log(LogMsg.makeAccessReleaseIgnoredLogMsg(sender, getSelf(), release));
					} else {
						//System.out.println(getName(sender) + " releasing exclusive write access to " + resourceName);
						
						// Set to true if full removed exclusiveWriter
						boolean fullRelease = false;
						
						// Only send Released log if released last/only access (otherwise there are still more accesses for this writer)
						int accessess = handler.releaseExclusiveWriter();
						//System.out.println(getName(sender) + " has " + accessess + " to " + resourceName);
						
						log(LogMsg.makeAccessReleasedLogMsg(sender, getSelf(), release));
						if(accessess == 0) {
							//System.out.println("Did full release");
							fullRelease = true;
						}
						
						Queue<Object> resourceBlockingRequests = handler.getBlockingRequests();
						Object nextRequestObj;
						
						//System.out.println(resourceBlockingRequests.toString());
						if(fullRelease && resourceBlockingRequests.size() > 0) {	
							//System.out.println(rmsg.toString() + " for " + getName(rmsg.getSender()));
							//System.out.println("doing release for " + resourceName + " with " + rmName + " for " + getName(sender));
							
							nextRequestObj = resourceBlockingRequests.peek();
							
							if(nextRequestObj instanceof AccessRequestMsg) {
								AccessRequestMsg nextRequest 	= (AccessRequestMsg) nextRequestObj;
								AccessRequest newRequest 		= nextRequest.getAccessRequest();
								ActorRef requestor				= nextRequest.getReplyTo();
								String resourceName2 			= newRequest.getResourceName();
								AccessHandler handler2			= accessHandlers.get(resourceName2);
								AccessRequestType reqType 		= newRequest.getType();
								AccessType type = getAccessType(reqType);
								
								// Process next blocked request
								if(type == AccessType.EXCLUSIVE_WRITE) {
									resourceBlockingRequests.poll();
									handler2.setExclusiveWrite(requestor);
									AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(nextRequest);
									//System.out.println("AccessRequestGrantedMsg use in " + grantMsg.toString() + " for " + getName(requestor));
									requestor.tell(grantMsg, getSelf());
									log(LogMsg.makeAccessRequestGrantedLogMsg(requestor, getSelf(), newRequest));
									
									while(resourceBlockingRequests.size() > 0 && getAccessType(reqType) == AccessType.EXCLUSIVE_WRITE) {
										nextRequestObj = resourceBlockingRequests.peek();
										if(nextRequestObj instanceof AccessRequestMsg) {
											resourceBlockingRequests.poll();
											nextRequest 	= (AccessRequestMsg) nextRequestObj;
											requestor 		= nextRequest.getReplyTo();
											reqType = nextRequest.getAccessRequest().getType();
											if(reqType == AccessRequestType.EXCLUSIVE_WRITE_BLOCKING && handler2.hasAccess(requestor)) {
												handler2.addExclusiveWriter();
												grantMsg = new AccessRequestGrantedMsg(nextRequest);
												requestor.tell(grantMsg, getSelf());
												log(LogMsg.makeAccessRequestGrantedLogMsg(requestor, getSelf(), newRequest));
											}
										} 
									}
									//System.out.println(resourceBlockingRequests.toString());
								} else {
									while(type == AccessType.CONCURRENT_READ && resourceBlockingRequests.size() > 0) {
										handler2.addConcurrentReader(requestor);
										
										nextRequestObj = resourceBlockingRequests.poll();
										
										if(nextRequestObj instanceof AccessRequestMsg) {
											nextRequest = (AccessRequestMsg) nextRequestObj;
											newRequest 	 = nextRequest.getAccessRequest();
											requestor 	 = nextRequest.getReplyTo();
											type = getAccessType(newRequest.getType());
											//System.out.println("AccessRequestGrantedMsg use");
											AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(nextRequest);
											requestor.tell(grantMsg, getSelf());
											log(LogMsg.makeAccessRequestGrantedLogMsg(requestor, getSelf(), newRequest));
										} else {
											// Disable request - will only be disables after it
											while(resourceBlockingRequests.size() > 0) {
												nextRequestObj = resourceBlockingRequests.poll();
												ManagementRequestMsg nextMgtRequest 	= (ManagementRequestMsg) nextRequestObj;
												ManagementRequest newMgtRequest 		= nextMgtRequest.getRequest();
												ActorRef managementSender 				= nextMgtRequest.getReplyTo();
												resource.disable();
												log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resourceName, ResourceStatus.DISABLED));
												ManagementRequestGrantedMsg grantMsg	= new ManagementRequestGrantedMsg(nextMgtRequest);
												managementSender.tell(grantMsg, getSelf());
												log(LogMsg.makeManagementRequestGrantedLogMsg(managementSender, getSelf(), newMgtRequest));
											}
										}
									}
								}
							} else {
								// Can only ever be ManagementRequest-Disable otherwise
								// If first one is a disable request - disable the resource and handle the rest in queue (if there are some)
								while(resourceBlockingRequests.size() > 0) {
									nextRequestObj = resourceBlockingRequests.poll();
									ManagementRequestMsg nextRequest 	= (ManagementRequestMsg) nextRequestObj;
									ManagementRequest newRequest 		= nextRequest.getRequest();
									ActorRef managementSender 			= nextRequest.getReplyTo();
									resource.disable();
									log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resourceName, ResourceStatus.DISABLED));
									ManagementRequestGrantedMsg grantMsg= new ManagementRequestGrantedMsg(nextRequest);
									managementSender.tell(grantMsg, getSelf());
									log(LogMsg.makeManagementRequestGrantedLogMsg(managementSender, getSelf(), newRequest));
								}
							}
						}
					}
				} else {
					HashMap<ActorRef, Integer> readActors = handler.getReadAccessActors();
					
					// If requesting actor doesn't have any read access on the resource, ignore it
					if(readActors.size() == 0 || !readActors.containsKey(sender)) {
						//System.out.println(getName(sender) + " is requesting release for a resource ("+resourceName+") that it doesn't" +
						//		" hold access to.");
						log(LogMsg.makeAccessReleaseIgnoredLogMsg(sender, getSelf(), release));
					} else {
						//System.out.println(getName(sender) + " releasing a concurrent read access to " + resourceName);
						
						// Set to true if fully removed all readers
						boolean fullRelease = false;
						
						// Only send Release log if removeConcurrentReader
						if(handler.removeConcurrentReader(sender)) {
							//System.out.println("did full release");
							fullRelease = true;
						}
						
						log(LogMsg.makeAccessReleasedLogMsg(sender, getSelf(), release));
						//System.out.println(getName(sender) + " released access of " + resourceName + ": fullRelease? " + fullRelease+" in "+rmName);
						Queue<Object> resourceBlockingRequests = handler.getBlockingRequests();
						Object nextRequestObj;
						
						if(fullRelease && resourceBlockingRequests.size() > 0) {
							
							nextRequestObj = resourceBlockingRequests.peek();
							
							if(nextRequestObj instanceof AccessRequestMsg) {
								nextRequestObj = resourceBlockingRequests.poll();
								AccessRequestMsg nextRequest = (AccessRequestMsg) nextRequestObj;
								AccessRequest newRequest 		= nextRequest.getAccessRequest();
								ActorRef requestor				= nextRequest.getReplyTo();
								String resourceName2 			= newRequest.getResourceName();
								AccessHandler handler2			= accessHandlers.get(resourceName2);
								AccessRequestType reqType 		= newRequest.getType();
								AccessType type = getAccessType(reqType);
								
								// Process next blocked request 
								if(type == AccessType.EXCLUSIVE_WRITE) {
									handler2.setExclusiveWrite(requestor);
									//System.out.println("AccessRequestGrantedMsg use");
									AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(nextRequest);
									requestor.tell(grantMsg, getSelf());
									log(LogMsg.makeAccessRequestGrantedLogMsg(requestor, getSelf(), newRequest));
									
									while(resourceBlockingRequests.size() > 0 && getAccessType(reqType) == AccessType.EXCLUSIVE_WRITE) {
										nextRequestObj = resourceBlockingRequests.peek();
										if(nextRequestObj instanceof AccessRequestMsg) {
											resourceBlockingRequests.poll();
											nextRequest 	= (AccessRequestMsg) nextRequestObj;
											requestor 		= nextRequest.getReplyTo();
											reqType = nextRequest.getAccessRequest().getType();
											if(reqType == AccessRequestType.EXCLUSIVE_WRITE_BLOCKING && handler2.hasAccess(requestor)) {
												handler2.addExclusiveWriter();
												grantMsg = new AccessRequestGrantedMsg(nextRequest);
												requestor.tell(grantMsg, getSelf());
												log(LogMsg.makeAccessRequestGrantedLogMsg(requestor, getSelf(), newRequest));
											}
										} 
									}
									//System.out.println(resourceBlockingRequests.toString());
								} else {
									while(type == AccessType.CONCURRENT_READ && resourceBlockingRequests.size() > 0) {
										handler2.addConcurrentReader(requestor);
										
										nextRequestObj = resourceBlockingRequests.poll();
										
										if(nextRequestObj instanceof AccessRequestMsg) {
											nextRequest = (AccessRequestMsg) nextRequestObj;
											newRequest 	 = nextRequest.getAccessRequest();
											requestor 	 = nextRequest.getReplyTo();
											type = getAccessType(newRequest.getType());
											//System.out.println("AccessRequestGrantedMsg use");
											AccessRequestGrantedMsg grantMsg = new AccessRequestGrantedMsg(nextRequest);
											requestor.tell(grantMsg, getSelf());
											log(LogMsg.makeAccessRequestGrantedLogMsg(requestor, getSelf(), newRequest));
											//System.out.println(resourceBlockingRequests.toString());
										} else {
											// Disable request - will only be disables after it
											while(resourceBlockingRequests.size() > 0) {
												nextRequestObj = resourceBlockingRequests.poll();
												ManagementRequestMsg nextMgtRequest 	= (ManagementRequestMsg) nextRequestObj;
												ManagementRequest newMgtRequest 		= nextMgtRequest.getRequest();
												ActorRef managementSender 				= nextMgtRequest.getReplyTo();
												resource.disable();
												log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resourceName, ResourceStatus.DISABLED));
												ManagementRequestGrantedMsg grantMsg	= new ManagementRequestGrantedMsg(nextMgtRequest);
												managementSender.tell(grantMsg, getSelf());
												log(LogMsg.makeManagementRequestGrantedLogMsg(managementSender, getSelf(), newMgtRequest));
											}
										}
									}
								}
							} else {
								// Can only ever be ManagementRequest-Disable otherwise
								// If first one is a disable request - disable the resource and handle the rest in queue (if there are some)
								while(resourceBlockingRequests.size() > 0) {
									nextRequestObj = resourceBlockingRequests.poll();
									ManagementRequestMsg nextRequest 	= (ManagementRequestMsg) nextRequestObj;
									ManagementRequest newRequest 		= nextRequest.getRequest();
									ActorRef managementSender 			= nextRequest.getReplyTo();
									resource.disable();
									log(LogMsg.makeResourceStatusChangedLogMsg(getSelf(), resourceName, ResourceStatus.DISABLED));
									ManagementRequestGrantedMsg grantMsg= new ManagementRequestGrantedMsg(nextRequest);
									managementSender.tell(grantMsg, getSelf());
									log(LogMsg.makeManagementRequestGrantedLogMsg(managementSender, getSelf(), newRequest));
								}
							}
						} 
					}
				}
			}
		}
		else if(msg instanceof WhoHasResourceRequestMsg) {
			// FROM: A ResourceManagerActor that is looking to see who has a resource
			// GOAL: Check if resource exists in this RM, tell sender if it has it
			

			WhoHasResourceRequestMsg rmsg = (WhoHasResourceRequestMsg) msg;
			ActorRef sender		= getSender();
			
			//System.out.println(rmName + " receieved WhoHasResourceRequest from " + getName(sender) + " looking for " + rmsg.getResourceName());
			
			boolean hasResource = false;
			String resourceName = rmsg.getResourceName();
			Resource resource 	= findLocalResource(resourceName);

			if(resource != null) {
				hasResource = true;
			}
			
			WhoHasResourceResponseMsg response0 = new WhoHasResourceResponseMsg(resourceName, hasResource, getSelf());
			sender.tell(response0, getSelf());

		}
		else if(msg instanceof WhoHasResourceResponseMsg) {
			// FROM: A ResourceManagerActor that has found the resource this RM was looking for during request forwarding
			// GOAL: Update search for resource and forward request appropriately 
			
			WhoHasResourceResponseMsg rmsg 			= (WhoHasResourceResponseMsg) msg;
			ActorRef sender 						= rmsg.getSender();
			String resourceName 					= rmsg.getResourceName();
			ResourceLocator locator 				= resourceLocators.get(resourceName);
			Queue<Object> pendingRequests 			= requestTracker.get(resourceName);
			
			//System.out.println(rmName + " received WhoHasResourceResponse from " + getName(sender) + "...");
		    //System.out.println(getName(sender) + " says " + rmsg.toString());
			
			// TODO: WhoHasResourceResponseMsg
			if(rmsg.getResult()) {

				// Resource has been found at "sender" RM - can now fwd request
				locator.updateLocator(sender, true);
				
				resourceByRM.put(resourceName, sender);
				log(LogMsg.makeRemoteResourceDiscoveredLogMsg(getSelf(), sender, resourceName));
				
				while(!pendingRequests.isEmpty()) {
					Object reqMsg = pendingRequests.poll();
					if(reqMsg instanceof AccessRequestMsg) {
						AccessRequestMsg castMsg = (AccessRequestMsg) reqMsg;
						sender.tell(castMsg, getSelf());
						log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), sender, castMsg.getAccessRequest()));
					}
					else if(reqMsg instanceof AccessReleaseMsg) {
						AccessReleaseMsg castMsg = (AccessReleaseMsg) reqMsg;
						sender.tell(castMsg, getSelf());
						log(LogMsg.makeAccessReleaseForwardedLogMsg(getSelf(), sender, castMsg.getAccessRelease()));
					}
					else {
						ManagementRequestMsg castMsg = (ManagementRequestMsg) reqMsg;
						sender.tell(castMsg, getSelf());
						log(LogMsg.makeManagementRequestForwardedLogMsg(getSelf(), sender, castMsg.getRequest()));
					}
				}
			} else {
				System.out.println(rmName + " does not have " + resourceName);
				
				int notFounds = notFoundResponses.get(resourceName);
				notFounds++;
				notFoundResponses.put(resourceName, notFounds);
				
				if(notFounds == remoteManagers.size()) {
					// Resource not found in any RM. Add to nonexistent and deny all requests waiting on this resource
					nonexistent.add(resourceName);
					
					while(!pendingRequests.isEmpty()) {
						Object reqMsg = pendingRequests.poll();
						if(reqMsg instanceof AccessRequestMsg) {
							AccessRequestMsg castMsg = (AccessRequestMsg) reqMsg;
							ActorRef respondTo = castMsg.getReplyTo();
							AccessRequestDeniedMsg denyMsg = new AccessRequestDeniedMsg(castMsg, AccessRequestDenialReason.RESOURCE_NOT_FOUND);
							respondTo.tell(denyMsg, getSelf());
							log(LogMsg.makeAccessRequestDeniedLogMsg(respondTo, getSelf(), castMsg.getAccessRequest(), AccessRequestDenialReason.RESOURCE_NOT_FOUND));
						}
						else if(reqMsg instanceof AccessReleaseMsg) {
							AccessReleaseMsg castMsg = (AccessReleaseMsg) reqMsg;
							ActorRef respondTo = castMsg.getSender();
							log(LogMsg.makeAccessReleaseIgnoredLogMsg(respondTo, getSelf(), castMsg.getAccessRelease()));
						}
						else {
							ManagementRequestMsg castMsg = (ManagementRequestMsg) reqMsg;
							ActorRef respondTo = castMsg.getReplyTo();
							ManagementRequestDeniedMsg denyMsg = new ManagementRequestDeniedMsg(castMsg, ManagementRequestDenialReason.RESOURCE_NOT_FOUND);
							respondTo.tell(denyMsg, getSelf());
							log(LogMsg.makeManagementRequestDeniedLogMsg(respondTo, getSelf(), castMsg.getRequest(), ManagementRequestDenialReason.RESOURCE_NOT_FOUND));
						}
					}
					
				}
				
			}
		} else {
			System.out.println("ERROR 101");
		}
		// else
		
	}
	
	
	
	///////////////////// Helper methods ///////////////////
	
	/* Method to check if current RM has local access to given resource */
	private Resource findLocalResource(String resourceToFind) {
		Resource found = null;
		for(int i=0; i < localResources.size(); i++) {
			if(localResources.get(i).name.equals(resourceToFind)) {
				found = localResources.get(i);
				return found;
			}
		}
		return found;
	}
	
	/* Method to easily return an Actor's name */
	private String getName(ActorRef actor) {
		return actor.path().name();
	}
	
	private AccessType getAccessType(AccessRequestType requestType) {
		if(requestType == AccessRequestType.CONCURRENT_READ_BLOCKING || requestType == AccessRequestType.CONCURRENT_READ_NONBLOCKING) {
			return AccessType.CONCURRENT_READ;
		} else {
			return AccessType.EXCLUSIVE_WRITE;
		}
	}
	
}

/* AccessRequestMsg - FORWARDING
 * // Request forwarding fields
				ResourceLocator fwdRequestSearch;
				HashMap<ActorRef, Boolean> resourceRMFound;
				Queue<AccessRequestMsg> accReqList;
				
				// Check if RM that handles this resource is known
				ActorRef forwardTo = resourceByRM.get(resourceName);
				if(forwardTo == null) {
					// Currently do not know which RM has local access to this resource
					
					//System.out.println(rmName + " does not know which remote RM has " + resourceName);
					
					// Check if resource is already being searched for:
					if(!accessRequestTracker.containsKey(resourceName)) {	
						//System.out.println("YO!: Resource already being searched for by " + getName(msgSender) + " in " + rmName );
						
						// It is not, so proceed to ask each RM if they have access to this resource
						resourceRMFound 	= new HashMap<ActorRef, Boolean>();
						fwdRequestSearch 	= new ResourceLocator(resourceName, resourceRMFound);
						accReqList 			= new LinkedList<AccessRequestMsg>();
						accReqList.add(rmsg);
						accessRequestTracker.put(resourceName, accReqList);
						resourceLocators.put(resourceName, fwdRequestSearch);
						
						WhoHasResourceRequestMsg amsg = new WhoHasResourceRequestMsg(resourceName);
						for(ActorRef remoteRM : remoteManagers) {
							fwdRequestSearch.updateLocator(remoteRM, false);
							//System.out.println("Asking " + remoteRM.path().name() + " to check for " + resourceName + " locally");
							remoteRM.tell(amsg, getSelf());
						}
						
						//System.out.println("Finishing " + rmName);
					} else {
						//System.out.println(rmName + " has already started searching for " + resourceName);
						
						// The RM is already searching for this the RM that has this Resource, add request to waiting requests
						Queue<AccessRequestMsg> requestQueue = accessRequestTracker.get(resourceName);
						requestQueue.add(rmsg);
					}
					
				} else {
					// This RM already knows which RM has local access to this resource -> fwd process
					forwardTo.tell(msg, getSelf());
					log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), forwardTo, request));
				}
				
				
// WhoHasResourceResponseMsg idea1			
			WhoHasResourceResponseMsg rmsg 			= (WhoHasResourceResponseMsg) msg;
			ActorRef sender 						= rmsg.getSender();
			String resourceName 					= rmsg.getResourceName();
			ResourceLocator locator 				= resourceLocators.get(resourceName);
			Queue<AccessRequestMsg> pendingAccRequests	 	= accessRequestTracker.get(resourceName);
			Queue<ManagementRequestMsg> pendingMgmtRequests = managementRequestTracker.get(resourceName);
			
			//System.out.println(rmsg.toString() + " (" + getName(sender) + ")");
			
			
			if(rmsg.getResult()) {				
				// Resource has been found at "sender" RM - can now fwd request
				locator.updateLocator(sender, true);
				
				// Update resource-RM table & forward pending requests
				resourceByRM.put(resourceName, sender);
				log(LogMsg.makeRemoteResourceDiscoveredLogMsg(getSelf(), sender, resourceName));
				
				
				// Go ahead and foward the access requests to all waiting actors
				while(!pendingRequests.isEmpty()) {
					AccessRequestMsg arMsg = pendingRequests.poll();
					//System.out.println(rmName + " sending pending AccessRequestMsg to " + getName(sender) );
					sender.tell(arMsg, getSelf());
					log(LogMsg.makeAccessRequestForwardedLogMsg(getSelf(), sender, arMsg.getAccessRequest()));
				}
				
			} else {
				// Resource not found in this RM
				locator.updateLocator(sender, false);
				
				// If ResourceLocator has checked every RM and it has not found one, send deny message - else let it continue
				if(locator.getCheckedCount() == remoteManagers.size()) {
					if(locator.getOwnCount()==0) {
						while(!pendingRequests.isEmpty()) {
							AccessRequestMsg arMsg = pendingRequests.poll();
							ActorRef respondTo = arMsg.getReplyTo();
							AccessRequestDeniedMsg denyMsg = new AccessRequestDeniedMsg(arMsg, AccessRequestDenialReason.RESOURCE_NOT_FOUND);
							respondTo.tell(denyMsg, getSelf());
						}
					} else {
						//System.out.println("Finished locator for " + resourceName);
					}
				}
			}
//////////////////////////////////////////
	// Exclusive-write-blocking stuff
						//System.out.println("Requesting EXCLUSIVE_WRITE_BLOCKING access with " + getName(sender) + " for " + resourceName);
						//if(writer != null) {System.out.println(getName(writer) + " currently has write access to " + resourceName);}
						// If access is not allowed, put request into pending request queue
						
						// If there doesn't exist a waitQueue for this resource in this RM, create one. Else add it to existing one.
						if(!pendingRequests.containsKey(resourceName)) {
							Queue<Object> waitQueue = new LinkedList<Object>();
							pendingRequests.put(resourceName, waitQueue);
							System.out.println("created new wait queue for " + resourceName);
						} else {
							Queue<Object> waitQueue = pendingRequests.get(resourceName);
							waitQueue.add(rmsg);
							System.out.println("added to existing queue for " + resourceName);
						}
						
						//System.out.println(requestType.name() + " NOT available for " + getName(sender) + " in " + rmName +
						//					". Added request to local pending request Queue.");
////////////////////////// old access release//////////////////////////////
						// If there are blocking requests waiting on this resource: (otherwise let msg end)
						if(pendingRequests.containsKey(resourceName)) {
							Queue<Object> waitQueue = pendingRequests.get(resourceName);
							if(waitQueue.size() > 0) {
								Object waitingRequest = waitQueue.poll();
								
								if(waitingRequest instanceof AccessRequestMsg) {
									
									
								}
								else if(waitingRequest instanceof ManagementRequestMsg) {
									
									ManagementRequestMsg newMgmtMsg = (ManagementRequestMsg) waitingRequest;
								}
								else {
									// Cannot get a AccessReleaseRequest since those will always be ignored beforehand 
									System.out.println("ERROR: Ignore message - wrong msg type in AccessRelease");
								}
							}
						}
///////////////////////////////////////////

				
				
				
				
				
				
				locator.updateLocator(sender, false);
				
				// If ResourceLocator has checked every RM and it has not found one, send deny message - else let it continue
				if(locator.getCheckedCount() == remoteManagers.size()) {
					if(locator.getOwnCount()==0) {
						while(!pendingRequests.isEmpty()) {
							Object reqMsg = pendingRequests.poll();
							if(reqMsg instanceof AccessRequestMsg) {
								AccessRequestMsg castMsg = (AccessRequestMsg) reqMsg;
								ActorRef respondTo = castMsg.getReplyTo();
								AccessRequestDeniedMsg denyMsg = new AccessRequestDeniedMsg(castMsg, AccessRequestDenialReason.RESOURCE_NOT_FOUND);
								respondTo.tell(denyMsg, getSelf());
								log(LogMsg.makeAccessRequestDeniedLogMsg(respondTo, getSelf(), castMsg.getAccessRequest(), AccessRequestDenialReason.RESOURCE_NOT_FOUND));
							}
							else if(reqMsg instanceof AccessReleaseMsg) {
								AccessReleaseMsg castMsg = (AccessReleaseMsg) reqMsg;
								ActorRef respondTo = castMsg.getSender();
								log(LogMsg.makeAccessReleaseIgnoredLogMsg(respondTo, getSelf(), castMsg.getAccessRelease()));
							}
							else {
								ManagementRequestMsg castMsg = (ManagementRequestMsg) reqMsg;
								ActorRef respondTo = castMsg.getReplyTo();
								ManagementRequestDeniedMsg denyMsg = new ManagementRequestDeniedMsg(castMsg, ManagementRequestDenialReason.RESOURCE_NOT_FOUND);
								respondTo.tell(denyMsg, getSelf());
								log(LogMsg.makeManagementRequestDeniedLogMsg(respondTo, getSelf(), castMsg.getRequest(), ManagementRequestDenialReason.RESOURCE_NOT_FOUND));
							}
						}
					} else {
						System.out.println("Finished locating " + resourceName);
					}
				}
/////////////////////////////////////////////
 * 			/////////////WhoHasResourceRequest/////////////////////
			Object request = rmsg.getRequestMsg();
			if(request instanceof AccessRequestMsg) {
				AccessRequestMsg accRequest = (AccessRequestMsg) request;
				
				WhoHasResourceResponseMsg response = new WhoHasResourceResponseMsg(resourceName, hasResource, accRequest, getSelf());
				sender.tell(response, getSelf());
			}
			else if(request instanceof AccessReleaseMsg) {
				AccessReleaseMsg accRelease = (AccessReleaseMsg) request;
				
				WhoHasResourceResponseMsg response = new WhoHasResourceResponseMsg(resourceName, hasResource, accRelease, getSelf());
				sender.tell(response, getSelf());
			}
			else if(request instanceof ManagementRequestMsg) {
				ManagementRequestMsg mgmtRequest = (ManagementRequestMsg) request;
				
				WhoHasResourceResponseMsg response = new WhoHasResourceResponseMsg(resourceName, hasResource, mgmtRequest, getSelf());
				sender.tell(response, getSelf());
			}
			else {
				System.out.println("This shouldn't really be here!!!");
				WhoHasResourceResponseMsg response0 = new WhoHasResourceResponseMsg(resourceName, hasResource, getSelf());
				sender.tell(response0, getSelf());
			}
			//////////////////////////////////////////////
			if(mode == RequestMode.ACCESS_REQUEST) {
				AccessRequestMsg request = (AccessRequestMsg) msgObj;
				sender = request.getReplyTo();
			} else if(mode == RequestMode.MANAGEMENT_REQUEST) {
				ManagementRequestMsg request = (ManagementRequestMsg) msgObj;
				sender = request.getReplyTo();
			} else {
				System.out.println("ERROR: In WhoHasResourceRequestMsg");
			}
 */























