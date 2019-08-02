package uk.co.blackpepper.bowman.test.it;

import java.net.URI;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.OptionalLinksEntity;
import uk.co.blackpepper.bowman.test.it.model.OptionalLinksQueryEntity;
import uk.co.blackpepper.bowman.test.it.model.SimpleEntity;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class OptionalLinkIT extends AbstractIT {
	
	private Client<OptionalLinksEntity> client;
	
	private Client<OptionalLinksQueryEntity> queryClient;
	
	private Client<SimpleEntity> simpleEntityClient;

	@Before
	public void setup() {
		client = clientFactory.create(OptionalLinksEntity.class);
		queryClient = clientFactory.create(OptionalLinksQueryEntity.class);
		simpleEntityClient = clientFactory.create(SimpleEntity.class);
	}

	@Test
	public void canGetOptionalItem() {
		OptionalLinksEntity sent = new OptionalLinksEntity();
		client.post(sent);
		
		OptionalLinksQueryEntity retrieved = queryClient.get(getQueryUri(sent));
		
		assertThat(retrieved.getOptionalLinkItem(), is(nullValue()));
	}
	
	@Test
	public void canGetOptionalCollection() {
		OptionalLinksEntity sent = new OptionalLinksEntity();
		client.post(sent);
		
		OptionalLinksQueryEntity retrieved = queryClient.get(getQueryUri(sent));
		
		assertThat(retrieved.getOptionalLinkCollection(), is(empty()));
	}
	
	@Test
	public void optionalItemCanHaveValue() {
		SimpleEntity related = new SimpleEntity();
		related.setName("x");
		simpleEntityClient.post(related);
		
		OptionalLinksEntity sent = new OptionalLinksEntity();
		sent.setOptionalLinkItem(related);
		URI location = client.post(sent);
		
		OptionalLinksEntity retrieved = client.get(location);
		
		assertThat(retrieved.getOptionalLinkItem().getName(), is("x"));
	}
	
	@Test
	public void optionalCollectionCanHaveValues() {
		SimpleEntity related = new SimpleEntity();
		related.setName("x");
		simpleEntityClient.post(related);
		
		OptionalLinksEntity sent = new OptionalLinksEntity();
		sent.setOptionalLinkCollection(Collections.singletonList(related));
		URI location = client.post(sent);
		
		OptionalLinksEntity retrieved = client.get(location);
		
		assertThat(retrieved.getOptionalLinkCollection().stream().map(SimpleEntity::getName).toArray(),
			is(arrayContaining("x")));
	}
	
	private static URI getQueryUri(OptionalLinksEntity entity) {
		return URI.create(entity.getId().toString()
			.replace("optional-links-entities", "optional-links-entities-query"));
	}
}
