package uk.co.blackpepper.halclient;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.halclient.annotation.RemoteResource;

import static uk.co.blackpepper.halclient.ReflectionSupport.setId;

public class Client<T> {

	private final Class<T> entityType;

	private final URI baseUri;
	
	private final ClientProxyFactory proxyFactory;

	private final RestOperations restOperations;

	Client(Class<T> entityType, Configuration configuration, RestOperations restOperations) {
		this.entityType = entityType;
		this.baseUri = configuration.getBaseUri();
		this.proxyFactory = configuration.getProxyFactory();
		this.restOperations = restOperations;
	}

	public T get(URI uri) {
		Resource<T> resource = restOperations.getResource(uri, entityType);
		
		if (resource == null) {
			return null;
		}
		
		return proxyFactory.create(resource, entityType, restOperations);
	}
	
	public Iterable<T> getAll() {
		return getAll(getEntityBaseUri());
	}
	
	public Iterable<T> getAll(URI uri) {
		List<T> result = new ArrayList<>();

		Resources<Resource<T>> resources = restOperations.getResources(uri, entityType);

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
