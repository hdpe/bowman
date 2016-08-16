package uk.co.blackpepper.hal.client.test.client;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.hal.client.annotation.LinkedResource;
import uk.co.blackpepper.hal.client.annotation.RemoteResource;
import uk.co.blackpepper.hal.client.annotation.ResourceId;

@RemoteResource("/bidi-parents")
public class BidiParentEntity {

	private URI entityId;
	
	private String name;
	
	private Set<BidiChildEntity> children = new LinkedHashSet<BidiChildEntity>();
	
	@ResourceId
	@JsonIgnore
	public URI getEntityId() {
		return entityId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@LinkedResource
	public Set<BidiChildEntity> getChildren() {
		return children;
	}
}
