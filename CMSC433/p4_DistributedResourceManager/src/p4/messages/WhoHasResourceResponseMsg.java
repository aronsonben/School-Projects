package cmsc433.p4.messages;

import akka.actor.ActorRef;
import cmsc433.p4.enums.RequestMode;

public class WhoHasResourceResponseMsg {
	private final String resource_name;
	private final boolean result;
	private final ActorRef sender; // The actor who sends this response message.
	
	/*
	private RequestMode requestMode;
	private AccessRequestMsg accessRequest;
	private AccessReleaseMsg accessRelease;
	private ManagementRequestMsg managementRequest;
	*/
	
	public WhoHasResourceResponseMsg (String resource_name, boolean result, ActorRef sender) {
		this.resource_name = resource_name;
		this.result = result;
		this.sender = sender;
	}
	
	public WhoHasResourceResponseMsg (WhoHasResourceRequestMsg request, boolean result, ActorRef sender) {
		this.resource_name = request.getResourceName();
		this.result = result;
		this.sender = sender;
	}
	
	/*
	public WhoHasResourceResponseMsg (String resource_name, boolean result, AccessRequestMsg request, ActorRef sender) {
		this.resource_name = resource_name;
		this.result = result;
		this.sender = sender;
		this.accessRequest = request;
		this.requestMode = RequestMode.ACCESS_REQUEST;
	}
	
	public WhoHasResourceResponseMsg (String resource_name, boolean result, AccessReleaseMsg request ,ActorRef sender) {
		this.resource_name = resource_name;
		this.result = result;
		this.sender = sender;
		this.accessRelease = request;
		this.requestMode = RequestMode.ACCESS_RELEASE;
	}
	
	public WhoHasResourceResponseMsg (String resource_name, boolean result, ManagementRequestMsg request ,ActorRef sender) {
		this.resource_name = resource_name;
		this.result = result;
		this.sender = sender;
		this.managementRequest = request;
		this.requestMode = RequestMode.MANAGEMENT_REQUEST;
	}
	*/
	
	public String getResourceName () {
		return resource_name;
	}
	
	public boolean getResult () {
		return result;
	}
	
	public ActorRef getSender () {
		return sender;
	}
	
	/*
	public RequestMode getRequestMode() {
		return requestMode;
	}
	
	public Object getRequestMsg() {
		if(requestMode == RequestMode.ACCESS_REQUEST) {
			return accessRequest;
		}
		else if(requestMode == RequestMode.ACCESS_RELEASE) {
			return accessRelease;
		}
		else if(requestMode == RequestMode.MANAGEMENT_REQUEST) {
			return managementRequest;
		} else {
			//System.out.println("Error: resultMode is null at this point");
			return null;
		}
	}
	*/
	
	@Override public String toString () {
		return "I" + (result ? " have " : " do not have ") + resource_name;
	}
}
