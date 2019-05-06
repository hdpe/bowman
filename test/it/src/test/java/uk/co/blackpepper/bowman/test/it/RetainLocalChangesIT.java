package uk.co.blackpepper.bowman.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.SimpleEntity;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RetainLocalChangesIT extends AbstractIT {

	private Client<SimpleEntity> client;

	@Before
	public void setup() {
		client = clientFactory.create(SimpleEntity.class);
	}

	@Test
	public void retainsLocallySetNullAssociations() {
		SimpleEntity related = new SimpleEntity();
		related.setName("x");

		client.post(related);
		
		SimpleEntity entity = new SimpleEntity();
		entity.setRelated(related);
		
		URI location = client.post(entity);

		SimpleEntity retrieved = client.get(location);
		
		retrieved.setRelated(null);
		
		assertThat(retrieved.getRelated(), is(nullValue()));
	}
}
