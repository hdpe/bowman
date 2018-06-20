package uk.co.blackpepper.bowman;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.co.blackpepper.bowman.JacksonClientModule.ResourceMixin;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ResourceDeserializerTest {
	
	private interface DeclaredType {
		// no members
	}
	
	private static class ResolvedType implements DeclaredType {
		
		private String field;

		public String getField() {
			return field;
		}
	}
	
	private abstract static class ResolvedAbstractClassType implements DeclaredType {
		
		ResolvedAbstractClassType() {
			// explicit constructor required for private class
		}
		
		abstract String findField();
	}
	
	private interface ResolvedInterfaceType extends DeclaredType {
		
		String findField();
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
			.when(instantiator).deserializerInstance(any(), any(), eq(ResourceDeserializer.class));
				
		mapper = new ObjectMapper();
		mapper.setHandlerInstantiator(instantiator);
		mapper.registerModule(new Jackson2HalModule());
		mapper.registerModule(new TestModule());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		doReturn(Void.class).when(typeResolver).resolveType(any(), any(), any());
	}
	
	@Test
	public void deserializeResolvesType() throws Exception {
		mapper.readValue("{\"_links\":{\"self\":{\"href\":\"http://x.com/1\"}}}",
			new TypeReference<Resource<DeclaredType>>() { /* generic type reference */ });
		
		verify(typeResolver).resolveType(DeclaredType.class,
			new Links(new Link("http://x.com/1", Link.REL_SELF)), configuration);
	}
	
	@Test
	public void deserializeReturnsObjectOfResolvedType() throws Exception {
		doReturn(ResolvedType.class).when(typeResolver).resolveType(any(), any(), any());
		
		Resource<DeclaredType> resource = mapper.readValue("{\"field\":\"x\"}",
			new TypeReference<Resource<DeclaredType>>() { /* generic type reference */ });
		
		assertThat("class", resource.getContent().getClass(), Matchers.<Class<?>>equalTo(ResolvedType.class));
		assertThat("field", ((ResolvedType) resource.getContent()).getField(), is("x"));
	}
	
	@Test
	public void deserializeReturnsObjectOfResolvedInterfaceType() throws Exception {
		doReturn(ResolvedInterfaceType.class).when(typeResolver).resolveType(any(), any(), any());
		
		Resource<DeclaredType> resource = mapper.readValue("{}",
			new TypeReference<Resource<DeclaredType>>() { /* generic type reference */ });
		
		assertThat(ResolvedInterfaceType.class.isAssignableFrom(resource.getContent().getClass()), is(true));
	}

	@Test
	public void deserializeReturnsObjectOfResolvedAbstractClassType() throws Exception {
		doReturn(ResolvedAbstractClassType.class).when(typeResolver).resolveType(any(), any(), any());
		
		Resource<DeclaredType> resource = mapper.readValue("{}",
			new TypeReference<Resource<DeclaredType>>() { /* generic type reference */ });
		
		assertThat(ResolvedAbstractClassType.class.isAssignableFrom(resource.getContent().getClass()), is(true));
	}
}
