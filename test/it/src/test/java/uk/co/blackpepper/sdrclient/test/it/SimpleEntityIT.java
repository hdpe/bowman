package uk.co.blackpepper.sdrclient.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.sdrclient.Client;
import uk.co.blackpepper.sdrclient.ClientFactory;
import uk.co.blackpepper.sdrclient.test.server.model.client.SimpleEntity;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SimpleEntityIT {

	private Client<SimpleEntity> client;

	@Before
	public void setup() {
		client = new ClientFactory(URI.create(System.getProperty("baseUrl"))).create(SimpleEntity.class);
	}

	@Test
	public void canGetEntityName() {
		SimpleEntity sent = new SimpleEntity();
		sent.setName("x");

		URI location = client.post(sent);

		SimpleEntity retrieved = client.get(location);
		assertThat(retrieved.getName(), is("x"));
	}

	@Test
	public void canGetEntityAssociation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("x");
		client.post(related);

		SimpleEntity sent = new SimpleEntity();
		sent.setRelated(related);

		URI location = client.post(sent);

		SimpleEntity retrieved = client.get(location);
		assertThat(retrieved.getRelated().getName(), is("x"));
	}
	
	@Test
	public void canGetNullEntityAssocation() {
		URI location = client.post(new SimpleEntity());

		SimpleEntity retrieved = client.get(location);
		assertThat(retrieved.getRelated(), is(nullValue()));
	}
}
