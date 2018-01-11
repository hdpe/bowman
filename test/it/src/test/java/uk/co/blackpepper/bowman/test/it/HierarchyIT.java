package uk.co.blackpepper.bowman.test.it;

import java.net.URI;
import java.util.Arrays;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.HierarchyBaseEntity;
import uk.co.blackpepper.bowman.test.it.model.HierarchyDerivedEntity1;
import uk.co.blackpepper.bowman.test.it.model.HierarchyDerivedEntity2;
import uk.co.blackpepper.bowman.test.it.model.HierarchyPropertyEntity;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HierarchyIT extends AbstractIT {

	private Client<HierarchyBaseEntity> baseEntityClient;
	
	private Client<HierarchyDerivedEntity1> derivedEntity1Client;
	
	private Client<HierarchyDerivedEntity2> derivedEntity2Client;
	
	private Client<HierarchyPropertyEntity> propertyEntityClient;
	
	@Before
	public void setUp() {
		baseEntityClient = clientFactory.create(HierarchyBaseEntity.class);
		derivedEntity1Client = clientFactory.create(HierarchyDerivedEntity1.class);
		derivedEntity2Client = clientFactory.create(HierarchyDerivedEntity2.class);
		propertyEntityClient = clientFactory.create(HierarchyPropertyEntity.class);
	}
	
	@Test
	public void testGetAllWithSubtypes() {
		HierarchyDerivedEntity1 entity1 = new HierarchyDerivedEntity1();
		entity1.setEntity1Field("x");
		derivedEntity1Client.post(entity1);
		
		HierarchyDerivedEntity2 entity2 = new HierarchyDerivedEntity2();
		entity2.setEntity2Field("y");
		derivedEntity2Client.post(entity2);
		
		Iterable<HierarchyBaseEntity> retrieved = baseEntityClient.getAll();
		
		assertThat(retrieved, containsInAnyOrder(Arrays.<Matcher<? super HierarchyBaseEntity>>asList(
			Matchers.<HierarchyBaseEntity>allOf(
				instanceOf(HierarchyDerivedEntity1.class),
				hasProperty("entity1Field", is("x"))
			),
			Matchers.<HierarchyBaseEntity>allOf(
				instanceOf(HierarchyDerivedEntity2.class),
				hasProperty("entity2Field", is("y"))
			)
		)));
	}
	
	@Test
	public void testLinkedEntityWithSubtypes() {
		HierarchyDerivedEntity1 entity1 = new HierarchyDerivedEntity1();
		entity1.setEntity1Field("x");
		derivedEntity1Client.post(entity1);
		
		HierarchyPropertyEntity propertyEntity = new HierarchyPropertyEntity();
		propertyEntity.setLinkedEntity(entity1);
		URI propertyEntityUri = propertyEntityClient.post(propertyEntity);
		
		HierarchyPropertyEntity retrieved = propertyEntityClient.get(propertyEntityUri);
		
		assertThat(retrieved.getLinkedEntity(), Matchers.<HierarchyBaseEntity>allOf(
			instanceOf(HierarchyDerivedEntity1.class),
			hasProperty("entity1Field", is("x"))
		));
	}

	@Test
	public void testLinkedEntityCollectionWithSubtypes() {
		HierarchyDerivedEntity1 entity1 = new HierarchyDerivedEntity1();
		entity1.setEntity1Field("x");
		derivedEntity1Client.post(entity1);
		
		HierarchyDerivedEntity2 entity2 = new HierarchyDerivedEntity2();
		entity2.setEntity2Field("y");
		derivedEntity2Client.post(entity2);
		
		HierarchyPropertyEntity propertyEntity = new HierarchyPropertyEntity();
		propertyEntity.setLinkedEntityCollection(asList(entity1, entity2));
		URI propertyEntityUri = propertyEntityClient.post(propertyEntity);
		
		HierarchyPropertyEntity retrieved = propertyEntityClient.get(propertyEntityUri);
		
		assertThat(retrieved.getLinkedEntityCollection(),
			containsInAnyOrder(Arrays.<Matcher<? super HierarchyBaseEntity>>asList(
				Matchers.<HierarchyBaseEntity>allOf(
					instanceOf(HierarchyDerivedEntity1.class),
					hasProperty("entity1Field", is("x"))
				),
				Matchers.<HierarchyBaseEntity>allOf(
					instanceOf(HierarchyDerivedEntity2.class),
					hasProperty("entity2Field", is("y"))
				)
			)));
	}
}
