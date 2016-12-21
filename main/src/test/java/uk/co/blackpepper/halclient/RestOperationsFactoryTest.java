package uk.co.blackpepper.halclient;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
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
	
	private RestOperationsFactory factory;

	@Before
	public void setup() {
		restTemplateFactory = mock(RestTemplateFactory.class);
		mapperFactory = mock(ObjectMapperFactory.class);
		proxyFactory = mock(ClientProxyFactory.class);

		factory = new RestOperationsFactory(new Configuration()
				.setRestTemplateFactory(restTemplateFactory)
				.setObjectMapperFactory(mapperFactory)
				.setProxyFactory(proxyFactory));
	}
	
	@Test
	public void createReturnsRestOperations() {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		
		when(mapperFactory.create(any(HandlerInstantiator.class))).thenReturn(mapper);
		when(restTemplateFactory.create(mapper)).thenReturn(restTemplate);
		
		RestOperations restOperations = factory.create();
		
		assertThat(restOperations, is(aRestOperationsMatching(restTemplate, mapper)));
	}
	
	@Test
	public void createInstantiatesObjectMapperWithEmbeddedChildDeserializerAwareHandlerInstantiator() {
		ObjectMapper mapper = new ObjectMapper();
		RestTemplate restTemplate = new RestTemplate();
		
		when(mapperFactory.create(any(HandlerInstantiator.class))).thenReturn(mapper);
		when(restTemplateFactory.create(mapper)).thenReturn(restTemplate);
		
		factory.create();
	
		ArgumentCaptor<HandlerInstantiator> handlerInstantiator = ArgumentCaptor.forClass(HandlerInstantiator.class);
		verify(mapperFactory).create(handlerInstantiator.capture());

		assertThat(handlerInstantiator.getValue().deserializerInstance(null, null, EmbeddedChildDeserializer.class),
				is(anEmbeddedChildDeserializerMatching(aRestOperationsMatching(restTemplate, mapper), proxyFactory)));
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
	private static Matcher<JsonDeserializer> anEmbeddedChildDeserializerMatching(
			final Matcher<RestOperations> restOperations, final ClientProxyFactory proxyFactory) {
		return new TypeSafeMatcher<JsonDeserializer>() {

			@Override
			public boolean matchesSafely(JsonDeserializer item) {
				if (!(item instanceof EmbeddedChildDeserializer)) {
					return false;
				}
				
				EmbeddedChildDeserializer other = (EmbeddedChildDeserializer) item;
				
				return restOperations.matches(other.getRestOperations())
						&& proxyFactory == other.getProxyFactory();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("instanceof ").appendValue(EmbeddedChildDeserializer.class)
					.appendText(", restOperations ").appendValue(restOperations)
					.appendText(", proxyFactory ").appendValue(proxyFactory);
			}
		};
	}
}
