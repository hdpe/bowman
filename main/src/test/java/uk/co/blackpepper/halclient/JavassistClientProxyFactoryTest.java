package uk.co.blackpepper.halclient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

import uk.co.blackpepper.halclient.annotation.LinkedResource;
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

import static java.util.Arrays.asList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavassistClientProxyFactoryTest {
	
	@RemoteResource("/entities")
	public static class Entity {
		
		private URI id;
		
		private Entity linked;
		
		private List<Entity> linkedCollection = new ArrayList<>();
		
		@ResourceId
		public URI getId() {
			return id;
		}
		
		@LinkedResource
		public Entity getLinked() {
			return linked;
		}
		
		@LinkedResource
		public List<Entity> getLinkedCollection() {
			return linkedCollection;
		}
	}
	
	private JavassistClientProxyFactory proxyFactory;
	
	private RestOperations restOperations;
	
	@Before
	public void setup() {
		proxyFactory = new JavassistClientProxyFactory();
		
		restOperations = mock(RestOperations.class);
	}
	
	@Test
	public void createReturnsProxyWithId() {
		Resource<Entity> resource = new Resource<>(new Entity(),
				new Link("http://www.example.com/1", Link.REL_SELF));
		
		Entity proxy = proxyFactory.create(resource,
				Entity.class, mock(RestOperations.class));
		
		assertThat(proxy.getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void createReturnsProxyWithLinkedResource() {
		Resource<Entity> resource = new Resource<>(new Entity(),
				new Link("http://www.example.com/association/linked", "linked"));
		
		when(restOperations.getResource(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(new Resource<>(new Entity(),
						new Link("http://www.example.com/1", Link.REL_SELF)));
		
		Entity proxy = proxyFactory.create(resource, Entity.class, restOperations);
		
		assertThat(proxy.getLinked().getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void createReturnsProxyWithLinkedResources() {
		Resource<Entity> resource = new Resource<>(new Entity(),
				new Link("http://www.example.com/association/linked", "linkedCollection"));
		
		when(restOperations.getResources(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(new Resources<>(asList(new Resource<>(new Entity(),
						new Link("http://www.example.com/1", Link.REL_SELF)))));
		
		Entity proxy = proxyFactory.create(resource, Entity.class, restOperations);
		
		assertThat(proxy.getLinkedCollection().get(0).getId(), is(URI.create("http://www.example.com/1")));
	}
}
