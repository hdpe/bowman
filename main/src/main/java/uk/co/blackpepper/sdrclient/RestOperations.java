package uk.co.blackpepper.sdrclient;

import java.net.URI;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class RestOperations {

	private final RestTemplate restTemplate;
	
	private final ObjectMapper objectMapper;
	
	RestOperations(RestTemplate restTemplate, ObjectMapper objectMapper) {
		this.restTemplate = restTemplate;
		this.objectMapper = objectMapper;
	}
	
	public <T> Resource<T> getResource(URI uri, Class<T> entityType) {
		ObjectNode node;
		
		try {
			node = restTemplate.getForObject(uri, ObjectNode.class);
		}
		catch (HttpClientErrorException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}
			
			throw exception;
		}
		
		JavaType targetType = objectMapper.getTypeFactory().constructParametricType(Resource.class, entityType);
		
		return objectMapper.convertValue(node, targetType);
	}

	public <T> Resources<Resource<T>> getResources(URI uri, Class<T> entityType) {
		ObjectNode node = restTemplate.getForObject(uri, ObjectNode.class);
		
		JavaType innerType = objectMapper.getTypeFactory().constructParametricType(Resource.class, entityType);
		JavaType targetType = objectMapper.getTypeFactory().constructParametricType(Resources.class, innerType);
		
		return objectMapper.convertValue(node, targetType);
	}
	
	public URI postObject(URI uri, Object object) {
		return restTemplate.postForLocation(uri, object);
	}
	
	public void deleteResource(URI uri) {
		restTemplate.delete(uri);
	}
}
