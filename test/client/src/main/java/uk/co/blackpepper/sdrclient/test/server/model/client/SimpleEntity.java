package uk.co.blackpepper.sdrclient.test.server.model.client;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;
import uk.co.blackpepper.sdrclient.annotation.ResourceId;

@RemoteResource("/simple-entities")
public class SimpleEntity {

	private URI id;

	private String name;
	
	private SimpleEntity related;
	
	@ResourceId
	@JsonIgnore
	public URI getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@LinkedResource
	public SimpleEntity getRelated() {
		return related;
	}

	public void setRelated(SimpleEntity related) {
		this.related = related;
	}
}
