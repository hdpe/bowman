package uk.co.blackpepper.bowman.test.it;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.client.HierarchyBaseEntity;
import uk.co.blackpepper.bowman.test.client.HierarchyDerivedEntity1;
import uk.co.blackpepper.bowman.test.client.HierarchyDerivedEntity2;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HierarchyIT extends AbstractIT {

	private Client<HierarchyBaseEntity> baseEntityClient;
	
	private Client<HierarchyDerivedEntity1> derivedEntity1Client;
	
	private Client<HierarchyDerivedEntity2> derivedEntity2Client;
	
	private Map<URI, Client<?>> createdEntities = new LinkedHashMap<>();
	
	@Before
	public void setUp() {
		baseEntityClient = clientFactory.create(HierarchyBaseEntity.class);
		derivedEntity1Client = clientFactory.create(HierarchyDerivedEntity1.class);
		derivedEntity2Client = clientFactory.create(HierarchyDerivedEntity2.class);
	}
	
	@After
	public void tearDown() {
		for (Map.Entry<URI, Client<?>> created : createdEntities.entrySet()) {
			created.getValue().delete(created.getKey());
		}
	}
	
	@Test
	public void testChildren() {
		HierarchyDerivedEntity1 entity1 = new HierarchyDerivedEntity1();
		entity1.setEntity1Field("x");
		doPost(derivedEntity1Client, entity1);
		
		HierarchyDerivedEntity2 entity2 = new HierarchyDerivedEntity2();
		entity2.setEntity2Field("y");
		doPost(derivedEntity2Client, entity2);
		
		Iterable<HierarchyBaseEntity> retrieved = baseEntityClient.getAll();
		
		assertThat(retrieved,
			containsInAnyOrder(hasProperty("entity1Field", is("x")), hasProperty("entity2Field", is("y"))));
	}
	
	private <T> void doPost(Client<T> client, T entity) {
		URI location = client.post(entity);
		
		createdEntities.put(location, client);
	}
}
