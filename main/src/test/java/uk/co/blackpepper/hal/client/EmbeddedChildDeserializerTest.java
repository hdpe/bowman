package uk.co.blackpepper.hal.client;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Resource;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class EmbeddedChildDeserializerTest {

	private static class SerializeParent {
		
		private List<Resource<Child>> children = new ArrayList<Resource<Child>>();

		@SuppressWarnings("unused")
		public List<Resource<Child>> getChildren() {
			return children;
		}
	}
	
	private static class DeserializeParent {
		
		private List<Child> children = new ArrayList<Child>();

		@JsonDeserialize(contentUsing = EmbeddedChildDeserializer.class)
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
		
		doReturn(new EmbeddedChildDeserializer(restOperations, proxyFactory))
			.when(instantiator).deserializerInstance(any(DeserializationConfig.class),
					any(Annotated.class), eq(EmbeddedChildDeserializer.class));
				
		mapper = new ObjectMapper();
		mapper.setHandlerInstantiator(instantiator);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Test
	public void deserializeReturnsObject() throws Exception {
		SerializeParent out = new SerializeParent();
		out.children.add(new Resource<Child>(new Child("x")));
		String json = mapper.writeValueAsString(out);
		
		DeserializeParent parent = mapper.readValue(json, DeserializeParent.class);
		
		assertThat(parent.getChildren().get(0).getName(), is("x"));
	}
}
