package uk.co.blackpepper.bowman.test.client;

import uk.co.blackpepper.bowman.AbstractResource;
import uk.co.blackpepper.bowman.annotation.Children;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@RemoteResource("/hierarchy-base-entities")
@Children({HierarchyDerivedEntity1.class, HierarchyDerivedEntity2.class})
public abstract class HierarchyBaseEntity extends AbstractResource<HierarchyBaseEntity> {
}
