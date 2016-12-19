package uk.co.blackpepper.halclient.test.client;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.halclient.annotation.LinkedResource;
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

@RemoteResource("/bidi-children")
public class BidiChildEntity {

	private URI id;
	
	private BidiParentEntity parent;
	
	private String name;
	
	@ResourceId
	@JsonIgnore
	public URI getId() {
		return id;
	}

	@LinkedResource
	public BidiParentEntity getParent() {
		return parent;
	}

	public void setParent(BidiParentEntity parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
