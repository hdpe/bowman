package uk.co.blackpepper.halclient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.co.blackpepper.halclient.annotation.LinkedResource;
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class JacksonClientModuleTest {

	@RemoteResource("/entities")
	public static class Entity {
				
		private URI id;
		
		private Entity linked;
		
		private List<Entity> linkedCollection = new ArrayList<>();
		
		Entity() {
		}
		
		Entity(URI id) {
			this.id = id;
		}
		
		Entity(Entity linked) {
			this.linked = linked;
		}
		
		@ResourceId
		@JsonIgnore
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
	
	private ObjectMapper mapper;
	
	@Before
	public void setup() {
		mapper = new ObjectMapper();
		
		mapper.registerModule(new JacksonClientModule());
	}
	
	@Test
	public void aLinkedResourceIsSerializedAsAUri() throws Exception {
		String json = mapper.writeValueAsString(new Entity(new Entity(URI.create("http://www.example.com/1"))));
		
		assertThat(json, containsString("\"linked\":\"http://www.example.com/1\""));
	}
	
	@Test
	public void linkedResourcesAreSerializedAsAUriArray() throws Exception {
		Entity entity = new Entity();
		entity.getLinkedCollection().add(new Entity(URI.create("http://www.example.com/1")));
		
		String json = mapper.writeValueAsString(entity);
		
		assertThat(json, containsString("\"linkedCollection\":[\"http://www.example.com/1\"]"));
	}
}
