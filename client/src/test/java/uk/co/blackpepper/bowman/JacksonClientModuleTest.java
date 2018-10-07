/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.bowman;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import javassist.util.proxy.MethodHandler;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class JacksonClientModuleTest {

	@RemoteResource("/entities")
	public static class Entity {
				
		private URI id;
		
		private String simple;
		
		private Entity linked;
		
		private List<Entity> linkedCollection = new ArrayList<>();
		
		Entity() {
		}
		
		Entity(String simple) {
			this.simple = simple;
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
		
		public String getSimple() {
			return simple;
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
	
	@Test
	public void unannotatedPropertiesAreSerializedAsNormal() throws Exception {
		String json = mapper.writeValueAsString(new Entity("x"));
		
		assertThat(json, containsString("\"simple\":\"x\""));
	}

	@Test
	public void handlerOnJavassistProxyIsNotSerialized() throws Exception {
		Entity proxy = new Entity("x") {
			public MethodHandler getHandler() {
				return null;
			}
		};
		String json = mapper.writeValueAsString(proxy);

		assertThat(json, not(containsString("\"handler\"")));
	}
}
