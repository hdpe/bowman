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

import java.io.IOException;
import java.net.URI;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.node.ObjectNode;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.I_AM_A_TEAPOT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class RestOperationsTest {

	private RestOperations restOperations;

	private RestTemplate restTemplate;
	
	private ObjectMapper objectMapper;
	
	private ExpectedException thrown = ExpectedException.none();

	@RemoteResource("/entities")
	private static class Entity {
		private String field;
		
		public String getField() {
			return field;
		}
	}
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
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
                .thenReturn(createObjectNode("{\"field\":\"value\",\"_links\" : {\"self\" : {"
                        + "  \"href\" : \"http://example.com/Service/entities/id\"}}}"));
		
		Resource<Entity> resource = restOperations.getResource(URI.create("http://example.com"), Entity.class);
		
		assertThat(resource.getContent().getField(), is("value"));
	}
	
	@Test
	public void getResourceOnNotFoundHttpClientExceptionReturnsNull() throws Exception {
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenThrow(new HttpClientErrorException(NOT_FOUND));
		
		Resource<Entity> resource = restOperations.getResource(URI.create("http://example.com"), Entity.class);
		
		assertThat(resource, is(nullValue()));
	}
	
	@Test
	public void getResourceOnOtherHttpClientExceptionThrowsException() throws Exception {
		HttpClientErrorException exception = new HttpClientErrorException(I_AM_A_TEAPOT);
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenThrow(exception);
		
		thrown.expect(is(exception));
		
		restOperations.getResource(URI.create("http://example.com"), Entity.class);
	}
	
	@Test
	public void getResourcesReturnsResources() throws Exception {
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenReturn(createObjectNode("{\"_embedded\":{\"entities\":[{\"field\":\"value\"}]}}"));
		
		Resources<Resource<Entity>> resources = restOperations.getResources(URI.create("http://example.com"),
			Entity.class);
		
		assertThat(resources.getContent().iterator().next().getContent().getField(), is("value"));
	}
	
	@Test
	public void getResourcesOnNotFoundHttpClientExceptionReturnsEmpty() throws Exception {
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenThrow(new HttpClientErrorException(NOT_FOUND));
		
		Resources<Resource<Entity>> resources = restOperations.getResources(URI.create("http://example.com"),
			Entity.class);
		
		assertThat(resources.getContent(), is(empty()));
	}
	
	@Test
	public void getResourcesOnOtherHttpClientExceptionThrowsException() throws Exception {
		HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
		when(restTemplate.getForObject(URI.create("http://example.com"), ObjectNode.class))
			.thenThrow(exception);

		thrown.expect(is(exception));
		
		restOperations.getResources(URI.create("http://example.com"), Entity.class);
	}
	
	@Test
	public void postObjectReturnsURI() {
		Entity entity = new Entity();
		when(restTemplate.postForLocation(URI.create("http://example.com"), entity))
			.thenReturn(URI.create("http://example.com/1"));
		
		URI id = restOperations.postObject(URI.create("http://example.com"), entity);
		
		assertThat(id, is(URI.create("http://example.com/1")));
	}
	
	@Test
	public void putObjectPutsObject() {
		Entity entity = new Entity();
		restOperations.putObject(URI.create("http://example.com/1"), entity);
		
		verify(restTemplate).put(URI.create("http://example.com/1"), entity);
	}
	
	@Test
	public void deleteResourceDeletesResource() {
		restOperations.deleteResource(URI.create("http://example.com/1"));
		
		verify(restTemplate).delete(URI.create("http://example.com/1"));
	}

	private ObjectNode createObjectNode(String json) throws IOException, JsonParseException, JsonMappingException {
		return objectMapper.readValue(json, ObjectNode.class);
	}
}
