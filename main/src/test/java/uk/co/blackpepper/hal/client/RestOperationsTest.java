package uk.co.blackpepper.hal.client;

import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestOperationsTest {

	private RestOperations restOperations;

	private RestTemplate restTemplate;
	
	private ObjectMapper objectMapper;

	private static class Entity {
		private String field;
		
		public String getField() {
			return field;
		}
	}
	
	@Before
	public void setup() {
		restTemplate = mock(RestTemplate.class);
		objectMapper = new DefaultObjectMapperFactory().create(mock(HandlerInstantiator.class));

		restOperations = new RestOperations(restTemplate, objectMapper);
	}
	
	@Test
	public void getResourceReturnsResource() throws Exception {
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenReturn(createObjectNode("{\"field\":\"value\"}"));
		
		Resource<Entity> resource = restOperations.getResource(URI.create("http://example.com"), Entity.class);
		
		assertThat(resource.getContent().getField(), is("value"));
	}
	
	@Test
	public void getResourcesReturnsResources() throws Exception {
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenReturn(createObjectNode("{\"_embedded\":{\"entities\":[{\"field\":\"value\"}]}}"));
		
		Resources<Resource<Entity>> resources = restOperations.getResources(URI.create("http://example.com"),
			Entity.class);
		
		assertThat(resources.getContent().iterator().next().getContent().getField(), is("value"));
	}

	private ObjectNode createObjectNode(String json) throws IOException, JsonParseException, JsonMappingException {
		return objectMapper.readValue(json, ObjectNode.class);
	}
}
