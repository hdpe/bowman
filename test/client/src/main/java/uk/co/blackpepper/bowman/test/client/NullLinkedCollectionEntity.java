package uk.co.blackpepper.bowman.test.client;

import java.net.URI;
import java.util.Set;

import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

@RemoteResource("/null-linked-collections")
public class NullLinkedCollectionEntity {

	private URI id;

	private Set<SimpleEntity> linked;

	@ResourceId
	public URI getId() {
		return id;
	}

	@LinkedResource
	public Set<SimpleEntity> getLinked() {
		return linked;
	}

	public void setLinked(Set<SimpleEntity> linked) {
		this.linked = linked;
	}
}
