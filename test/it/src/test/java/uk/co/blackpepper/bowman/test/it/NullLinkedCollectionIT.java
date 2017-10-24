package uk.co.blackpepper.bowman.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.client.NullLinkedCollectionEntity;
import uk.co.blackpepper.bowman.test.client.SimpleEntity;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NullLinkedCollectionIT extends AbstractIT {
	
	private Client<NullLinkedCollectionEntity> client;

	private Client<SimpleEntity> simpleEntityClient;
	
	@Before
	public void setUp() {
		client = clientFactory.create(NullLinkedCollectionEntity.class);
		simpleEntityClient = clientFactory.create(SimpleEntity.class);
	}
	
	@Test
	public void canGetInitiallyNullLinkedCollection() {
		SimpleEntity linked = new SimpleEntity();
		URI linkedLocation = simpleEntityClient.post(linked);
		
		NullLinkedCollectionEntity entity = new NullLinkedCollectionEntity();
		entity.setLinked(Sets.newHashSet(linked));
		client.post(entity);
		
		NullLinkedCollectionEntity retrieved = client.get(entity.getId());
		
		assertThat(retrieved.getLinked(), contains(hasProperty("id", is(linkedLocation))));
	}
}
