package uk.co.blackpepper.bowman;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.co.blackpepper.bowman.JacksonClientModule.ResourceMixin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResourceDeserializerTest {
	
	private static class DeclaredType {
		// no members
	}
	
	private static class ResolvedType extends DeclaredType {
		
		private String field;

		public String getField() {
			return field;
		}

		public void setField(String field) {
			this.field = field;
		}
	}
	
	@SuppressWarnings("serial")
	private static class TestModule extends SimpleModule {
		TestModule() {
			setMixInAnnotation(Resource.class, ResourceMixin.class);
		}
	}
	
	private ObjectMapper mapper;
	
	private HandlerInstantiator instantiator;

	private TypeResolver typeResolver;
	
	private Configuration configuration;
	
	@Before
	public void setup() {
		typeResolver = mock(TypeResolver.class);
		configuration = Configuration.build();
		
		instantiator = mock(HandlerInstantiator.class);
		
		doReturn(new ResourceDeserializer(Object.class, typeResolver, configuration))
			.when(instantiator).deserializerInstance(any(DeserializationConfig.class),
					any(Annotated.class), eq(ResourceDeserializer.class));
				
		mapper = new ObjectMapper();
		mapper.setHandlerInstantiator(instantiator);
		mapper.registerModule(new Jackson2HalModule());
		mapper.registerModule(new TestModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	@Test
	public void deserializeReturnsObjectOfResolvedType() throws Exception {
		doReturn(ResolvedType.class).when(typeResolver).resolveType(DeclaredType.class,
			new Links(new Link("http://x.com/1", Link.REL_SELF)), configuration);
		
		Resource<DeclaredType> resource = mapper.readValue(
			"{\"field\":\"x\",\"_links\":{\"self\":{\"href\":\"http://x.com/1\"}}}",
			new TypeReference<Resource<DeclaredType>>() { });
		
		assertThat("class", resource.getContent().getClass(), Matchers.<Class<?>>equalTo(ResolvedType.class));
		assertThat("field", ((ResolvedType) resource.getContent()).getField(), is("x"));
	}
}
