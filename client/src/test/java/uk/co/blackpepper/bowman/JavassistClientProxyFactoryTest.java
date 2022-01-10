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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;

import javassist.util.proxy.ProxyFactory;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JavassistClientProxyFactoryTest {
	
	@RemoteResource("/entities")
	public static class Entity {
		
		private URI id;
		
		private Entity linked;
		
		private boolean active;
		
		private List<Entity> linkedCollection = new ArrayList<>();
		
		private List<Entity> nullLinkedCollection;
		
		@ResourceId
		public URI getId() {
			return id;
		}
		
		@LinkedResource
		public Entity linked() {
			return linked;
		}
		
		@LinkedResource(rel = "a:b")
		public Entity getLinkedWithCustomRel() {
			return linked;
		}
		
		@LinkedResource
		public List<Entity> getLinkedCollection() {
			return linkedCollection;
		}
		
		@LinkedResource
		public List<Entity> getNullLinkedCollection() {
			return nullLinkedCollection;
		}

		public void setNullLinkedCollection(List<Entity> nullLinkedCollection) {
			this.nullLinkedCollection = nullLinkedCollection;
		}

		public boolean isActive() {
			return active;
		}
		
		public void setActive(boolean active) {
			this.active = active;
		}
	}
	
	public static class UnconstructableEntity {
		
		public UnconstructableEntity(Object argument) {
			// unconstructable due to missing no-args constructor
		}
	}
	
	public abstract static class AbstractClassTypeResource {
	}
	
	public interface InterfaceTypeResource {
		
		@LinkedResource
		List<Entity> linked();
	}
	
	private JavassistClientProxyFactory proxyFactory;
	
	private RestOperations restOperations;
	
	private ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
	}
	
	@Before
	public void setup() {
		proxyFactory = new JavassistClientProxyFactory();
		
		restOperations = mock(RestOperations.class);
	}
	
	@Test
	public void createReturnsProxyWithId() {
		EntityModel<Entity> resource = EntityModel.of(new Entity(),
				Link.of("http://www.example.com/1", IanaLinkRelations.SELF));
		
		Entity proxy = proxyFactory.create(resource, mock(RestOperations.class));
		
		assertThat(proxy.getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void createReturnsProxyWithLinkedResource() {
		EntityModel<Entity> resource = EntityModel.of(new Entity(),
				Link.of("http://www.example.com/association/linked", "linked"));
		
		when(restOperations.getResource(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(EntityModel.of(new Entity(),
						Link.of("http://www.example.com/1", IanaLinkRelations.SELF)));
		
		Entity proxy = proxyFactory.create(resource, restOperations);
		
		assertThat(proxy.linked().getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void createReturnsProxyWithLinkedResourceWithCustomRel() {
		EntityModel<Entity> resource = EntityModel.of(new Entity(),
				Link.of("http://www.example.com/association/linked", "a:b"));
		
		when(restOperations.getResource(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(EntityModel.of(new Entity(),
						Link.of("http://www.example.com/1", IanaLinkRelations.SELF)));
		
		Entity proxy = proxyFactory.create(resource, restOperations);
		
		assertThat(proxy.getLinkedWithCustomRel().getId(), is(URI.create("http://www.example.com/1")));
	}

	@Test
	public void createWithLinkedResourceLinkNotPresentReturnsProxyThrowingException() {
		Entity entity = proxyFactory.create(EntityModel.of(new Entity()), restOperations);
		
		thrown.expect(NoSuchLinkException.class);
		thrown.expect(hasProperty("linkName", is("linked")));
		
		entity.linked();
	}
	
	@Test
	public void createWithLinkedResourceTargetNotPresentReturnsProxyReturningNull() {
		EntityModel<Entity> resource = EntityModel.of(new Entity(),
			Link.of("http://www.example.com/association/linked", "linked"));
	
		when(restOperations.getResource(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(null);
		
		Entity proxy = proxyFactory.create(resource, restOperations);
		
		assertThat(proxy.linked(), is(nullValue()));
	}
	
	@Test
	public void createReturnsProxyWithLinkedResources() {
		EntityModel<Entity> resource = EntityModel.of(new Entity(),
				Link.of("http://www.example.com/association/linked", "linkedCollection"));
		
		when(restOperations.getResources(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(CollectionModel.of(asList(EntityModel.of(new Entity(),
						Link.of("http://www.example.com/1", IanaLinkRelations.SELF)))));
		
		Entity proxy = proxyFactory.create(resource, restOperations);
		
		assertThat(proxy.getLinkedCollection().get(0).getId(), is(URI.create("http://www.example.com/1")));
	}

	@Test
	public void createWithNullLinkedCollectionReturnsProxyWithLinkedResources() {
		EntityModel<Entity> resource = EntityModel.of(new Entity(),
				Link.of("http://www.example.com/association/linked", "nullLinkedCollection"));
		
		when(restOperations.getResources(URI.create("http://www.example.com/association/linked"),
				Entity.class)).thenReturn(CollectionModel.of(asList(EntityModel.of(new Entity(),
						Link.of("http://www.example.com/1", IanaLinkRelations.SELF)))));
		
		Entity proxy = proxyFactory.create(resource, restOperations);
		
		assertThat(proxy.getNullLinkedCollection().get(0).getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void createReturnsProxyWithActive() {
		Entity entity = new Entity();
		entity.setActive(true);
		
		EntityModel<Entity> resource = EntityModel.of(entity,
			Link.of("http://www.example.com/1", IanaLinkRelations.SELF));
		
		Entity proxy = proxyFactory.create(resource, mock(RestOperations.class));
		
		assertThat(proxy.getId(), is(URI.create("http://www.example.com/1")));
		assertThat(proxy.isActive(), is(true));
	}
	
	@Test
	public void createReturnsProxyWithSettingValuesPossible() {
		Entity entity = new Entity();
		entity.setActive(true);
		
		EntityModel<Entity> resource = EntityModel.of(entity,
			Link.of("http://www.example.com/1", IanaLinkRelations.SELF));
		
		Entity proxy = proxyFactory.create(resource, mock(RestOperations.class));
		
		proxy.setActive(false);
		
		assertThat(proxy.isActive(), is(false));
	}
	
	@Test
	public void createWithResourceWithProxiedAbstractClassContentReturnsProxy() throws Exception {
		AbstractClassTypeResource content = instantiateProxyOfAbstractClassType(AbstractClassTypeResource.class);
		
		AbstractClassTypeResource resource = proxyFactory
			.create(EntityModel.of(content), mock(RestOperations.class));
		
		assertThat(resource, is(allOf(isA(AbstractClassTypeResource.class), not(sameInstance(content)))));
	}
	
	@Test
	public void createWithResourceWithProxiedInterfaceContentReturnsProxy() throws Exception {
		InterfaceTypeResource content = instantiateProxyOfInterfaceType(InterfaceTypeResource.class);
		
		InterfaceTypeResource resource = proxyFactory.create(EntityModel.of(content), mock(RestOperations.class));
		
		assertThat(resource, is(allOf(isA(InterfaceTypeResource.class), not(sameInstance(content)))));
	}
	
	@Test
	public void createWithResourceWithProxiedInterfaceContentReturnsProxyWithLinkedResources() throws Exception {
		InterfaceTypeResource content = instantiateProxyOfInterfaceType(InterfaceTypeResource.class);
		
		when(restOperations.getResources(URI.create("http://www.example.com/association/linked"),
			Entity.class)).thenReturn(CollectionModel.of(asList(EntityModel.of(new Entity(),
			Link.of("http://www.example.com/1", IanaLinkRelations.SELF)))));
		
		InterfaceTypeResource resource = proxyFactory.create(EntityModel.of(content,
			Link.of("http://www.example.com/association/linked", "linked")), restOperations);
		
		assertThat(resource.linked().get(0).getId(), is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void createWithUnconstructableEntityResourceThrowsException() {
		thrown.expect(ClientProxyException.class);
		thrown.expectMessage("couldn't create proxy instance of " + UnconstructableEntity.class);
		
		proxyFactory.create(EntityModel.of(new UnconstructableEntity(new Object())), mock(RestOperations.class));
	}
	
	private static <T> T instantiateProxyOfAbstractClassType(Class<T> type) throws Exception {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(type);
		return instantiate(factory.createClass());
	}
	
	private static <T> T instantiateProxyOfInterfaceType(Class<T> type) throws Exception {
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(new Class[] {type});
		return instantiate(factory.createClass());
	}
	
	private static <T> T instantiate(Class clazz) throws Exception {
		@SuppressWarnings("unchecked")
		T result = (T) clazz.newInstance();
		return result;
	}
}
