package uk.co.blackpepper.sdrclient;

import java.net.URI;

import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.sdrclient.gen.annotation.RemoteResource;

import static uk.co.blackpepper.sdrclient.ReflectionSupport.setId;

public class Client<T> {

	private final Class<T> entityType;

	private final URI baseUri;

	private final RestOperations restOperations;

	private final ClientProxyFactory proxyFactory;

	public Client(Class<T> entityType, URI baseUri, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		this.entityType = entityType;
		this.baseUri = baseUri;
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}

	public T get(URI uri) {
		return proxyFactory.create(uri, entityType, restOperations);
	}

	public URI post(T object) {
		String path = object.getClass().getAnnotation(RemoteResource.class).value();
		URI postUri = UriComponentsBuilder.fromUri(baseUri).path(path).build().toUri();
		URI resourceUri = restOperations.postObject(postUri, object);
		
		setId(object, resourceUri);
		
		return resourceUri;
	}

	public void delete(URI uri) {
		restOperations.deleteResource(uri);
	}
}
