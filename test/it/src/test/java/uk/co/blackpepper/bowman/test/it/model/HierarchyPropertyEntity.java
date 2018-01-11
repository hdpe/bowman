package uk.co.blackpepper.bowman.test.it.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

@RemoteResource("/hierarchy-property-entities")
public class HierarchyPropertyEntity {

	private URI id;
	
	private HierarchyBaseEntity linkedEntity;
	
	private List<HierarchyBaseEntity> linkedEntityCollection = new ArrayList<>();
	
	@ResourceId
	public URI getId() {
		return id;
	}
	
	@LinkedResource
	public HierarchyBaseEntity getLinkedEntity() {
		return linkedEntity;
	}
	
	public void setLinkedEntity(HierarchyBaseEntity linkedEntity) {
		this.linkedEntity = linkedEntity;
	}
	
	@LinkedResource
	public List<HierarchyBaseEntity> getLinkedEntityCollection() {
		return linkedEntityCollection;
	}
	
	public void setLinkedEntityCollection(List<HierarchyBaseEntity> linkedEntityCollection) {
		this.linkedEntityCollection = linkedEntityCollection;
	}
}
