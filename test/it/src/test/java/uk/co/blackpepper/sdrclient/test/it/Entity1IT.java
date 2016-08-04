package uk.co.blackpepper.sdrclient.test.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import uk.co.blackpepper.sdrclient.Client;
import uk.co.blackpepper.sdrclient.ClientFactory;
import uk.co.blackpepper.sdrclient.test.server.model.client.Entity1;

import java.net.URI;
import java.util.Collections;

public class Entity1IT {

	private Client<Entity1> client;

	@Before
	public void setup() {
		client = new ClientFactory(URI.create(System.getProperty("baseUrl")))
		        .create(Entity1.class);
	}
	
  @Test
  public void canGetEntityName() {
    Entity1 sent = new Entity1();
    sent.setName("x");
    
    URI location = client.post(sent);
    
    Entity1 retrieved = client.get(location);
    assertThat(retrieved.getName(), is("x"));
  }
  
  @Test
  public void canGetEntityAssociation() {
	  Entity1 related = new Entity1();
	  related.setName("x");
	  client.post(related);
	  
	  Entity1 sent = new Entity1();
	  sent.setRelated(related);
	  
	  URI location = client.post(sent);
	  
	  Entity1 retrieved = client.get(location);
	  assertThat(retrieved.getRelated().getName(), is("x"));
  }
  
  static class Entity1Resource extends Resource<Entity1> {

	public Entity1Resource() {
		this(new Entity1());
	}
	  
	public Entity1Resource(Entity1 content, Iterable<Link> links) {
		super(content, links);
	}

	public Entity1Resource(Entity1 content, Link... links) {
		super(content, links);
	}
	  
  }
}
