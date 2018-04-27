package uk.co.blackpepper.bowman.test.it;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.SimpleEntity;
import uk.co.blackpepper.bowman.test.it.model.SimpleEntitySearch;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SimpleEntitySearchIT extends AbstractIT {
	
	private Client<SimpleEntity> entities;
	
	private Client<SimpleEntitySearch> search;
	
	@Before
	public void setup() {
		entities = clientFactory.create(SimpleEntity.class);
		search = clientFactory.create(SimpleEntitySearch.class);
	}
	
	@Test
	public void getByInterfaceTemplateLinkReturnsEntity() {
		SimpleEntity entity = new SimpleEntity();
		entity.setName("x");
		entities.post(entity);
		
		SimpleEntity found = search.get().findByName("x");
		
		assertThat(found.getName(), is("x"));
	}
	
	@Test
	public void getByInterfaceTemplateLinkReturnsEntityWithProxiedProperties() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		entities.post(related);
		
		SimpleEntity entity = new SimpleEntity();
		entity.setName("x");
		entity.setRelated(related);
		entities.post(entity);
		
		SimpleEntity found = search.get().findByName("x").getRelated();
		
		assertThat(found.getName(), is("related"));
	}
}
