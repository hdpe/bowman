package uk.co.blackpepper.halclient.test.client;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import uk.co.blackpepper.halclient.EmbeddedChildDeserializer;
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

@RemoteResource("/embedded-bidi-parents")
public class EmbeddedBidiParentEntity {

	private URI id;
	
	private String name;

	private Set<EmbeddedBidiChildEntity> children = new LinkedHashSet<EmbeddedBidiChildEntity>();
	
	private EmbeddedBidiChildEntity child;
	
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

	@JsonDeserialize(using = EmbeddedChildDeserializer.class)
	public EmbeddedBidiChildEntity getChild() {
		return child;
	}

	public void setChild(EmbeddedBidiChildEntity child) {
		this.child = child;
	}

	@JsonDeserialize(contentUsing = EmbeddedChildDeserializer.class)
	public Set<EmbeddedBidiChildEntity> getChildren() {
		return children;
	}
}
