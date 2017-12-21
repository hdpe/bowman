package uk.co.blackpepper.bowman.test.client;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

@RemoteResource("/hierarchyDerivedEntity2s")
public class HierarchyDerivedEntity2 extends HierarchyBaseEntity {

	private String entity2Field;
	
	public String getEntity2Field() {
		return entity2Field;
	}
	
	public void setEntity2Field(String entity2Field) {
		this.entity2Field = entity2Field;
	}
}
