package uk.co.blackpepper.bowman.test.client;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.bowman.AbstractResource;
import uk.co.blackpepper.bowman.annotation.Children;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

@RemoteResource("/hierarchy-base-entities")
@Children({HierarchyDerivedEntity1.class, HierarchyDerivedEntity2.class})
public abstract class HierarchyBaseEntity extends AbstractResource<HierarchyBaseEntity> {
	
	private URI id;
	
	@JsonIgnore
	@ResourceId
	public URI getId() {
		return id;
	}
}
