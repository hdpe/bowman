package uk.co.blackpepper.sdrclient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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
	
	public Iterable<T> getAll() {
		List<T> result = new ArrayList<T>();
		
		Resources<Resource<T>> resources = restOperations.getResources(getEntityBaseUri(), entityType);
		
		for (Resource<T> resource : resources) {
			result.add(proxyFactory.create(resource, entityType, restOperations));
		}
		
		return result;
	}

	public URI post(T object) {
		URI resourceUri = restOperations.postObject(getEntityBaseUri(), object);
		
		setId(object, resourceUri);
		
		return resourceUri;
	}

	public void delete(URI uri) {
		restOperations.deleteResource(uri);
	}

	private URI getEntityBaseUri() {
		String path = entityType.getAnnotation(RemoteResource.class).value();
		
		return UriComponentsBuilder.fromUri(baseUri).path(path).build().toUri();
	}
}
