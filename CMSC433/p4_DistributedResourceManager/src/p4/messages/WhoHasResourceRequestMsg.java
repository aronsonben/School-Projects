package cmsc433.p4.messages;

import cmsc433.p4.enums.RequestMode;

public class WhoHasResourceRequestMsg {	
	private final String resource_name;
	
	private RequestMode requestMode;
	private AccessRequestMsg accessRequest;
	private AccessReleaseMsg accessRelease;
	private ManagementRequestMsg managementRequest;
	
	public WhoHasResourceRequestMsg (String resource) {
		this.resource_name = resource;
	}
	
	public WhoHasResourceRequestMsg (String resource, AccessRequestMsg request) {
		this.resource_name = resource;
		this.accessRequest = request;
		this.requestMode = RequestMode.ACCESS_REQUEST;
	}
	
	public WhoHasResourceRequestMsg (String resource, AccessReleaseMsg request) {
		this.resource_name = resource;
		this.accessRelease = request;
		this.requestMode = RequestMode.ACCESS_RELEASE;
	}
	
	public WhoHasResourceRequestMsg (String resource, ManagementRequestMsg request) {
		this.resource_name = resource;
		this.managementRequest = request;
		this.requestMode = RequestMode.MANAGEMENT_REQUEST;
	}
	
	
	public String getResourceName () {
		return resource_name;
	}
	
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
	
	@Override 
	public String toString () {
		return "Who has " + resource_name + "?";
	}
}
