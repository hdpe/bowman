package uk.co.blackpepper.sdrclient.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.sdrclient.Client;
import uk.co.blackpepper.sdrclient.ClientFactory;
import uk.co.blackpepper.sdrclient.test.server.model.client.BidiChildEntity;
import uk.co.blackpepper.sdrclient.test.server.model.client.BidiParentEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BidiIT {

	private Client<BidiParentEntity> parentClient;
	
	private Client<BidiChildEntity> childClient;

	@Before
	public void setup() {
		ClientFactory clientFactory = new ClientFactory(URI.create(System.getProperty("baseUrl")));
		parentClient = clientFactory.create(BidiParentEntity.class);
		childClient = clientFactory.create(BidiChildEntity.class);
	}

	@Test
	public void canGetParentAssociation() {
		BidiParentEntity parent = new BidiParentEntity();
		parent.setName("x");
		parentClient.post(parent);

		BidiChildEntity sent = new BidiChildEntity();
		sent.setParent(parent);

		URI location = childClient.post(sent);

		BidiChildEntity retrieved = childClient.get(location);
		assertThat(retrieved.getParent().getName(), is("x"));
	}
}
