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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.EntityModel;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class InlineAssociationDeserializerTest {

	private static class SerializeParent {
		
		private List<EntityModel<Child>> children = new ArrayList<>();

		@SuppressWarnings("unused")
		public List<EntityModel<Child>> getChildren() {
			return children;
		}
	}
	
	private static class DeserializeParent {
		
		private List<Child> children = new ArrayList<>();

		@JsonDeserialize(contentUsing = InlineAssociationDeserializer.class)
		public List<Child> getChildren() {
			return children;
		}

		@SuppressWarnings("unused")
		public void setChildren(List<Child> children) {
			this.children = children;
		}
	}
		
	private static class Child {
		
		private String name;
		
		@SuppressWarnings("unused")
		Child() {
		}
		
		Child(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
	
	private ObjectMapper mapper;
	
	private HandlerInstantiator instantiator;

	@Before
	public void setup() {
		RestOperations restOperations = mock(RestOperations.class);
		ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
		
		instantiator = mock(HandlerInstantiator.class);
		
		doReturn(new InlineAssociationDeserializer<>(Object.class, restOperations, proxyFactory))
			.when(instantiator).deserializerInstance(any(), any(), eq(InlineAssociationDeserializer.class));
		
		mapper = new ObjectMapper();
		mapper.setHandlerInstantiator(instantiator);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Test
	public void deserializeReturnsObject() throws Exception {
		SerializeParent out = new SerializeParent();
		out.children.add(EntityModel.of(new Child("x")));
		String json = mapper.writeValueAsString(out);
		
		DeserializeParent parent = mapper.readValue(json, DeserializeParent.class);
		
		assertThat(parent.getChildren().get(0).getName(), is("x"));
	}
}
