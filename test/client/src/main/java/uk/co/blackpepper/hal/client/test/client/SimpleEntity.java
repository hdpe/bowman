package uk.co.blackpepper.hal.client.test.client;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.hal.client.annotation.LinkedResource;
import uk.co.blackpepper.hal.client.annotation.RemoteResource;
import uk.co.blackpepper.hal.client.annotation.ResourceId;

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
