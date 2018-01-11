package uk.co.blackpepper.bowman.test.it.model;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;
import uk.co.blackpepper.bowman.annotation.ResourceTypeInfo;

@RemoteResource("/hierarchy-base-entities")
@ResourceTypeInfo(subtypes = {HierarchyDerivedEntity1.class, HierarchyDerivedEntity2.class})
public abstract class HierarchyBaseEntity {
	
	private URI id;
	
	@JsonIgnore
	@ResourceId
	public URI getId() {
		return id;
	}
}
