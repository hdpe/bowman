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

import java.util.List;
import java.util.function.BiFunction;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.PotentialAssignment;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import static java.util.Arrays.asList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Theories.class)
public class RestOperationsFactoryTest {

	private RestTemplateFactory restTemplateFactory;

	private ObjectMapperFactory mapperFactory;
	
	private ClientProxyFactory proxyFactory;
	
	private ClientHttpRequestFactory clientHttpRequestFactory;
	
	private RestOperationsFactory factory;
	
	private Configuration configuration;
	
	@Before
	public void setup() {
		restTemplateFactory = mock(RestTemplateFactory.class);
		mapperFactory = mock(ObjectMapperFactory.class);
		proxyFactory = mock(ClientProxyFactory.class);

		clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
		
		configuration = Configuration.builder()
				.setRestTemplateConfigurer(null)
				.setClientHttpRequestFactory(clientHttpRequestFactory)
				.build();
		
		factory = new RestOperationsFactory(configuration, proxyFactory, mapperFactory, restTemplateFactory);
		
		when(mapperFactory.create(any())).thenReturn(new ObjectMapper());
		when(restTemplateFactory.create(any(), any())).thenReturn(new RestTemplate());
	}
	
	@Test
	public void createReturnsRestOperations() {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		
		when(mapperFactory.create(any())).thenReturn(mapper);
		when(restTemplateFactory.create(clientHttpRequestFactory, mapper)).thenReturn(restTemplate);
		
		RestOperations restOperations = factory.create();
		
		assertThat(restOperations, is(aRestOperationsMatching(is(restTemplate), is(mapper))));
	}
	
	@Test
	public void createInstantiatesObjectMapperWithInlineAssociationDeserializerAwareHandlerInstantiator() {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		
		when(mapperFactory.create(any())).thenReturn(mapper);
		when(restTemplateFactory.create(any(), any())).thenReturn(restTemplate);
		
		factory.create();
	
		ArgumentCaptor<HandlerInstantiator> handlerInstantiator = ArgumentCaptor.forClass(HandlerInstantiator.class);
		verify(mapperFactory).create(handlerInstantiator.capture());
		
		JsonDeserializer<?> result = handlerInstantiator.getValue()
			.deserializerInstance(null, null, InlineAssociationDeserializer.class);
		
		assertThat(result, is(anInlineAssociationDeserializerMatching(
			aRestOperationsMatching(is(restTemplate), is(mapper)), is(proxyFactory))));
	}
	
	@Test
	public void createInstantiatesObjectMapperWithResourceDeserializerAwareHandlerInstantiator() {
		factory.create();
		
		ArgumentCaptor<HandlerInstantiator> handlerInstantiator = ArgumentCaptor.forClass(HandlerInstantiator.class);
		verify(mapperFactory).create(handlerInstantiator.capture());
		
		JsonDeserializer<?> result = handlerInstantiator.getValue()
			.deserializerInstance(null, null, ResourceDeserializer.class);
		
		assertThat(result, is(aResourceDeserializerMatching(instanceOf(DefaultTypeResolver.class),
			is(configuration))));
	}
	
	@Theory
	public void createInstantiatesObjectMapperWithNonLibraryHandlerAwareHandlerInstantiator(
		@ParametersSuppliedBy(NonLibraryHandlerTestParams.class) HandlerInstantiatorTestParams params) {

		factory.create();
		
		ArgumentCaptor<HandlerInstantiator> handlerInstantiator = ArgumentCaptor.forClass(HandlerInstantiator.class);
		verify(mapperFactory).create(handlerInstantiator.capture());
		
		Object result = params.instantiationMethod.apply(handlerInstantiator.getValue(), params.clazz);
		
		assertThat(result, instanceOf(params.clazz));
	}
	
	@Test
	public void createInvokesConfigurerOnRestTemplateIfPresent() {
		RestTemplateConfigurer restTemplateConfigurer = mock(RestTemplateConfigurer.class);
		Configuration configuration = Configuration.builder()
				.setRestTemplateConfigurer(restTemplateConfigurer)
				.build();
		
		RestTemplate restTemplate = new RestTemplate();
		when(restTemplateFactory.create(any(), any())).thenReturn(restTemplate);
		
		new RestOperationsFactory(configuration, proxyFactory, mapperFactory, restTemplateFactory)
			.create();
		
		verify(restTemplateConfigurer).configure(restTemplate);
	}
	
	@Test
	public void createInvokesConfigurerOnObjectMapperIfPresent() {
		ObjectMapperConfigurer objectMapperConfigurer = mock(ObjectMapperConfigurer.class);
		Configuration configuration = Configuration.builder()
			.setObjectMapperConfigurer(objectMapperConfigurer)
			.build();
		
		ObjectMapper objectMapper = new ObjectMapper();
		when(mapperFactory.create(any())).thenReturn(objectMapper);
		
		new RestOperationsFactory(configuration, proxyFactory, mapperFactory, restTemplateFactory)
			.create();
		
		verify(objectMapperConfigurer).configure(objectMapper);
	}

	private static Matcher<RestOperations> aRestOperationsMatching(Matcher<RestTemplate> restTemplate,
			Matcher<ObjectMapper> mapper) {
		return new TypeSafeMatcher<RestOperations>() {

			@Override
			public boolean matchesSafely(RestOperations other) {
				return restTemplate.matches(other.getRestTemplate())
						&& mapper.matches(other.getObjectMapper());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("restTemplate ").appendValue(restTemplate)
					.appendText(", objectMapper ").appendValue(mapper);
			}
		};
	}

	private static Matcher<JsonDeserializer> anInlineAssociationDeserializerMatching(
			Matcher<RestOperations> restOperations, Matcher<ClientProxyFactory> proxyFactory) {
		return new TypeSafeMatcher<JsonDeserializer>() {

			@Override
			public boolean matchesSafely(JsonDeserializer item) {
				if (!(item instanceof InlineAssociationDeserializer)) {
					return false;
				}
				
				InlineAssociationDeserializer other = (InlineAssociationDeserializer) item;
				
				return restOperations.matches(other.getRestOperations())
						&& proxyFactory.matches(other.getProxyFactory());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("instanceof ").appendValue(InlineAssociationDeserializer.class)
					.appendText(", restOperations ").appendValue(restOperations)
					.appendText(", proxyFactory ").appendValue(proxyFactory);
			}
		};
	}
	
	private static Matcher<JsonDeserializer> aResourceDeserializerMatching(
			Matcher<TypeResolver> typeResolver, Matcher<Configuration> configuration) {
		return new TypeSafeMatcher<JsonDeserializer>() {
			
			@Override
			protected boolean matchesSafely(JsonDeserializer item) {
				if (!(item instanceof ResourceDeserializer)) {
					return false;
				}
				
				ResourceDeserializer other = (ResourceDeserializer) item;
				
				return typeResolver.matches(other.getTypeResolver())
					&& configuration.matches(other.getConfiguration());
			}
			
			@Override
			public void describeTo(Description description) {
				description.appendText("instanceof ").appendValue(ResourceDeserializer.class)
					.appendText(", typeResolver ").appendValue(typeResolver)
					.appendText(", configuration ").appendValue(configuration);
			}
		};
	}
	
	private static class HandlerInstantiatorTestParams {
		
		private Class<?> clazz;
		
		private BiFunction<HandlerInstantiator, Class<?>, Object> instantiationMethod;
		
		public HandlerInstantiatorTestParams(Class<?> clazz,
			BiFunction<HandlerInstantiator, Class<?>, Object> instantiationMethod) {
			
			this.clazz = clazz;
			this.instantiationMethod = instantiationMethod;
		}
	}
	
	public static class NonLibraryHandlerTestParams extends ParameterSupplier {
		
		public NonLibraryHandlerTestParams() {
		}
		
		@Override
		public List<PotentialAssignment> getValueSources(ParameterSignature sig) {
			return asList(
				PotentialAssignment.forValue(
					"deserializerInstance",
					new HandlerInstantiatorTestParams(DummyJsonDeserializer.class,
						(instantiator, clazz) -> instantiator.deserializerInstance(null, null, clazz))
				),
				
				PotentialAssignment.forValue(
					"keyDeserializerInstance",
					new HandlerInstantiatorTestParams(DummyKeyDeserializer.class,
						(instantiator, clazz) -> instantiator.keyDeserializerInstance(null, null, clazz))
				),
				
				PotentialAssignment.forValue(
					"serializerInstance",
					new HandlerInstantiatorTestParams(DummySerializer.class,
						(instantiator, clazz) -> instantiator.serializerInstance(null, null, clazz))
				),
				
				PotentialAssignment.forValue(
					"typeResolverBuilderInstance",
					new HandlerInstantiatorTestParams(DummyTypeResolverBuilder.class,
						(instantiator, clazz) -> instantiator.typeResolverBuilderInstance(null, null, clazz))
				),
				
				PotentialAssignment.forValue(
					"typeIdResolverInstance",
					new HandlerInstantiatorTestParams(DummyTypeIdResolver.class,
						(instantiator, clazz) -> instantiator.typeIdResolverInstance(null, null, clazz))
				)
			);
		}
	}
	
	private static class DummyJsonDeserializer extends JsonDeserializer<Object> {
		
		@Override
		public Object deserialize(JsonParser p, DeserializationContext ctxt) {
			return null;
		}
	}
	
	private static class DummyKeyDeserializer extends KeyDeserializer {
		
		@Override
		public Object deserializeKey(String key, DeserializationContext ctxt) {
			return null;
		}
	}
	
	private static class DummySerializer extends JsonSerializer<Object> {
		
		@Override
		public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {
		}
	}
	
	private static class DummyTypeResolverBuilder extends StdTypeResolverBuilder {
	}
	
	private static class DummyTypeIdResolver extends MinimalClassNameIdResolver {
		
		protected DummyTypeIdResolver() {
			super(SimpleType.constructUnsafe(Object.class), TypeFactory.defaultInstance());
		}
	}
}
