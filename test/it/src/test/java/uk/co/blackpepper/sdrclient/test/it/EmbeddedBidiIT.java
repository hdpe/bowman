package uk.co.blackpepper.sdrclient.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.sdrclient.Client;
import uk.co.blackpepper.sdrclient.ClientFactory;
import uk.co.blackpepper.sdrclient.test.server.model.client.EmbeddedBidiChildEntity;
import uk.co.blackpepper.sdrclient.test.server.model.client.EmbeddedBidiParentEntity;
import uk.co.blackpepper.sdrclient.test.server.model.client.SimpleEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EmbeddedBidiIT {

	private Client<EmbeddedBidiParentEntity> client;
	
	private Client<SimpleEntity> simpleEntityClient;
	
	@Before
	public void setup() {
		ClientFactory clientFactory = new ClientFactory(URI.create(System.getProperty("baseUrl")));
		client = clientFactory.create(EmbeddedBidiParentEntity.class);
		simpleEntityClient = clientFactory.create(SimpleEntity.class);
	}
	
	@Test
	public void canGetChildAssocation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		simpleEntityClient.post(related);
		
		EmbeddedBidiParentEntity parent = new EmbeddedBidiParentEntity();
		
		EmbeddedBidiChildEntity child = new EmbeddedBidiChildEntity();
		child.setName("x");
		child.setRelated(related);
//		child.setParent(parent);

		parent.setChild(child);
		
		URI location = client.post(parent);
		
		EmbeddedBidiParentEntity retrieved = client.get(location);
		
		EmbeddedBidiChildEntity retrievedItem = retrieved.getChild();
		assertThat(retrievedItem.getName(), is("x"));
		assertThat(retrievedItem.getRelated().getName(), is("related"));
	}
	
	@Test
	public void canGetChildrenAssocation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		simpleEntityClient.post(related);
		
		EmbeddedBidiParentEntity parent = new EmbeddedBidiParentEntity();
		
		EmbeddedBidiChildEntity child = new EmbeddedBidiChildEntity();
		child.setName("x");
		child.setRelated(related);
//		child.setParent(parent);

		parent.getChildren().add(child);
		
		URI location = client.post(parent);
		
		EmbeddedBidiParentEntity retrieved = client.get(location);
		
		EmbeddedBidiChildEntity retrievedItem = retrieved.getChildren().iterator().next();
		assertThat(retrievedItem.getName(), is("x"));
		assertThat(retrievedItem.getRelated().getName(), is("related"));
	}
}
