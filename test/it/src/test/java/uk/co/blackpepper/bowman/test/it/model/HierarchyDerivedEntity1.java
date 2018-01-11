package uk.co.blackpepper.bowman.test.it.model;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

@RemoteResource("/hierarchy-derived-entity-ones")
public class HierarchyDerivedEntity1 extends HierarchyBaseEntity {

	private String entity1Field;
	
	public String getEntity1Field() {
		return entity1Field;
	}
	
	public void setEntity1Field(String entity1Field) {
		this.entity1Field = entity1Field;
	}
}
