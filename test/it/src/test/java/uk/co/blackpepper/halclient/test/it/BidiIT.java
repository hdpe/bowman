package uk.co.blackpepper.halclient.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.halclient.Client;
import uk.co.blackpepper.halclient.test.client.BidiChildEntity;
import uk.co.blackpepper.halclient.test.client.BidiParentEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BidiIT extends AbstractIT {

	private Client<BidiParentEntity> parentClient;
	
	private Client<BidiChildEntity> childClient;

	@Before
	public void setup() {
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
	
	@Test
	public void canGetChildrenAssocation() {
		BidiParentEntity parent = new BidiParentEntity();
		URI location = parentClient.post(parent);
		
		BidiChildEntity child = new BidiChildEntity();
		child.setName("x");
		child.setParent(parent);
		childClient.post(child);
		
		BidiParentEntity retrieved = parentClient.get(location);
		assertThat(retrieved.getChildren().iterator().next().getName(), is("x"));
	}
}
