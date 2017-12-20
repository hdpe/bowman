package uk.co.blackpepper.bowman.test.it;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.client.HierarchyBaseEntity;
import uk.co.blackpepper.bowman.test.client.HierarchyDerivedEntity1;
import uk.co.blackpepper.bowman.test.client.HierarchyDerivedEntity2;
import uk.co.blackpepper.bowman.test.client.HierarchyPropertyEntity;

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
	
	private Map<URI, Client<?>> createdEntities = new LinkedHashMap<>();
	
	@Before
	public void setUp() {
		baseEntityClient = clientFactory.create(HierarchyBaseEntity.class);
		derivedEntity1Client = clientFactory.create(HierarchyDerivedEntity1.class);
		derivedEntity2Client = clientFactory.create(HierarchyDerivedEntity2.class);
		propertyEntityClient = clientFactory.create(HierarchyPropertyEntity.class);
	}
	
	@After
	public void tearDown() {
		List<Entry<URI, Client<?>>> createdEntities = new ArrayList<>(this.createdEntities.entrySet());
		Collections.reverse(createdEntities);
		
		for (Map.Entry<URI, Client<?>> created : createdEntities) {
			created.getValue().delete(created.getKey());
		}
	}
	
	@Test
	public void testGetAllWithSubtypes() {
		HierarchyDerivedEntity1 entity1 = new HierarchyDerivedEntity1();
		entity1.setEntity1Field("x");
		doPost(derivedEntity1Client, entity1);
		
		HierarchyDerivedEntity2 entity2 = new HierarchyDerivedEntity2();
		entity2.setEntity2Field("y");
		doPost(derivedEntity2Client, entity2);
		
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
		doPost(derivedEntity1Client, entity1);
		
		HierarchyPropertyEntity propertyEntity = new HierarchyPropertyEntity();
		propertyEntity.setLinkedEntity(entity1);
		URI propertyEntityUri = doPost(propertyEntityClient, propertyEntity);
		
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
		doPost(derivedEntity1Client, entity1);
		
		HierarchyDerivedEntity2 entity2 = new HierarchyDerivedEntity2();
		entity2.setEntity2Field("y");
		doPost(derivedEntity2Client, entity2);
		
		HierarchyPropertyEntity propertyEntity = new HierarchyPropertyEntity();
		propertyEntity.setLinkedEntityCollection(asList(entity1, entity2));
		URI propertyEntityUri = doPost(propertyEntityClient, propertyEntity);
		
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
	
	private <T> URI doPost(Client<T> client, T entity) {
		URI location = client.post(entity);
		
		createdEntities.put(location, client);
		
		return location;
	}
}
