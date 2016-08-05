package uk.co.blackpepper.sdrclient;

import java.net.URI;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.sdrclient.gen.annotation.RemoteResource;

import static uk.co.blackpepper.sdrclient.ReflectionSupport.setId;

public class Client<T> {

	private final Class<T> entityType;

	private final URI baseUri;

	private final RestTemplate restTemplate;

	private ClientProxyFactory proxyFactory;

	public Client(Class<T> entityType, URI baseUri, RestTemplate restTemplate, ClientProxyFactory proxyFactory) {
		this.entityType = entityType;
		this.baseUri = baseUri;
		this.restTemplate = restTemplate;
		this.proxyFactory = proxyFactory;
	}

	public T get(URI uri) {
		return proxyFactory.create(uri, entityType, restTemplate);
	}

	public URI post(T object) {
		String path = object.getClass().getAnnotation(RemoteResource.class).value();
		URI postUri = UriComponentsBuilder.fromUri(baseUri).path(path).build().toUri();
		URI resourceUri = restTemplate.postForLocation(postUri, object);
		
		setId(object, resourceUri);
		
		return resourceUri;
	}

	public void delete(URI uri) {
		restTemplate.delete(uri);
	}
}
