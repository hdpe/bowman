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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RestOperationsFactoryTest {

	private RestTemplateFactory restTemplateFactory;

	private ObjectMapperFactory mapperFactory;
	
	private ClientProxyFactory proxyFactory;
	
	private ClientHttpRequestFactory clientHttpRequestFactory;
	
	private RestOperationsFactory factory;

	@Before
	public void setup() {
		restTemplateFactory = mock(RestTemplateFactory.class);
		mapperFactory = mock(ObjectMapperFactory.class);
		proxyFactory = mock(ClientProxyFactory.class);

		clientHttpRequestFactory = mock(ClientHttpRequestFactory.class);
		
		Configuration configuration = Configuration.builder()
				.setRestTemplateConfigurer(null)
				.setClientHttpRequestFactory(clientHttpRequestFactory)
				.build();
		
		factory = new RestOperationsFactory(configuration, proxyFactory, mapperFactory, restTemplateFactory);
	}
	
	@Test
	public void createReturnsRestOperations() {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		
		when(mapperFactory.create(any(HandlerInstantiator.class))).thenReturn(mapper);
		when(restTemplateFactory.create(clientHttpRequestFactory, mapper)).thenReturn(restTemplate);
		
		RestOperations restOperations = factory.create();
		
		assertThat(restOperations, is(aRestOperationsMatching(restTemplate, mapper)));
	}
	
	@Test
	public void createInstantiatesObjectMapperWithInlineAssociationDeserializerAwareHandlerInstantiator() {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		
		when(mapperFactory.create(any(HandlerInstantiator.class))).thenReturn(mapper);
		when(restTemplateFactory.create(any(ClientHttpRequestFactory.class), any(ObjectMapper.class)))
			.thenReturn(restTemplate);
		
		factory.create();
	
		ArgumentCaptor<HandlerInstantiator> handlerInstantiator = ArgumentCaptor.forClass(HandlerInstantiator.class);
		verify(mapperFactory).create(handlerInstantiator.capture());

		assertThat(handlerInstantiator.getValue().deserializerInstance(null, null, InlineAssociationDeserializer.class),
				is(anInlineAssociationDeserializerMatching(aRestOperationsMatching(restTemplate, mapper),
						proxyFactory)));
	}
	
	@Test
	public void createInvokesConfigurerOnRestTemplateIfPresent() {
		RestTemplateConfigurer restTemplateConfigurer = mock(RestTemplateConfigurer.class);
		Configuration configuration = Configuration.builder()
				.setRestTemplateConfigurer(restTemplateConfigurer)
				.build();
		
		RestTemplate restTemplate = new RestTemplate();
		when(restTemplateFactory.create(any(ClientHttpRequestFactory.class), any(ObjectMapper.class)))
			.thenReturn(restTemplate);
		
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
		when(mapperFactory.create(any(HandlerInstantiator.class)))
			.thenReturn(objectMapper);
		
		new RestOperationsFactory(configuration, proxyFactory, mapperFactory, restTemplateFactory)
			.create();
		
		verify(objectMapperConfigurer).configure(objectMapper);
	}

	private static Matcher<RestOperations> aRestOperationsMatching(final RestTemplate restTemplate,
			final ObjectMapper mapper) {
		return new TypeSafeMatcher<RestOperations>() {

			@Override
			public boolean matchesSafely(RestOperations other) {
				return restTemplate == other.getRestTemplate()
						&& mapper == other.getObjectMapper();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("restTemplate ").appendValue(restTemplate)
					.appendText(", objectMapper ").appendValue(mapper);
			}
		};
	}

	@SuppressWarnings("rawtypes")
	private static Matcher<JsonDeserializer> anInlineAssociationDeserializerMatching(
			final Matcher<RestOperations> restOperations, final ClientProxyFactory proxyFactory) {
		return new TypeSafeMatcher<JsonDeserializer>() {

			@Override
			public boolean matchesSafely(JsonDeserializer item) {
				if (!(item instanceof InlineAssociationDeserializer)) {
					return false;
				}
				
				InlineAssociationDeserializer other = (InlineAssociationDeserializer) item;
				
				return restOperations.matches(other.getRestOperations())
						&& proxyFactory == other.getProxyFactory();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("instanceof ").appendValue(InlineAssociationDeserializer.class)
					.appendText(", restOperations ").appendValue(restOperations)
					.appendText(", proxyFactory ").appendValue(proxyFactory);
			}
		};
	}
}
