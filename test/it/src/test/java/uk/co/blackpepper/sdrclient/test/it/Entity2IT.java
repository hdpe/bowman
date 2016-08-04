package uk.co.blackpepper.sdrclient.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.sdrclient.Client;
import uk.co.blackpepper.sdrclient.ClientFactory;
import uk.co.blackpepper.sdrclient.test.server.model.client.Entity1;
import uk.co.blackpepper.sdrclient.test.server.model.client.Entity2;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class Entity2IT {

	private Client<Entity1> client1;
	
	private Client<Entity2> client2;

	@Before
	public void setup() {
		ClientFactory clientFactory = new ClientFactory(URI.create(System.getProperty("baseUrl")));
		client1 = clientFactory.create(Entity1.class);
		client2 = clientFactory.create(Entity2.class);
	}

	@Test
	public void canGetEntityAssociation() {
		Entity1 related = new Entity1();
		related.setName("x");
		client1.post(related);

		Entity2 sent = new Entity2();
		sent.setRelated(related);

		URI location = client2.post(sent);

		Entity2 retrieved = client2.get(location);
		assertThat(retrieved.getRelated().getName(), is("x"));
	}
}
