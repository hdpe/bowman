package uk.co.blackpepper.sdrclient.test.client;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.ResourceId;

public class EmbeddedBidiChildEntity {

	private URI id;
	
	private EmbeddedBidiParentEntity parent;
	
	private String name;

	private SimpleEntity related;
	
	@ResourceId
	@JsonIgnore
	public URI getId() {
		return id;
	}

	@LinkedResource
	public EmbeddedBidiParentEntity getParent() {
		return parent;
	}

	public void setParent(EmbeddedBidiParentEntity parent) {
		this.parent = parent;
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
