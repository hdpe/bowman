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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

import static uk.co.blackpepper.bowman.ReflectionSupport.getId;
import static uk.co.blackpepper.bowman.ReflectionSupport.setId;

/**
 * Class for retrieving, persisting and deleting annotated entity instances via remote
 * hal+json resources.
 *
 * <p>Entities can contain simple (directly mappable to JSON) properties, and inline or
 * linked associations to further objects.
 * 
 * <p><code>Client</code>s are created via {@link ClientFactory#create}.
 *
 * @param <T> the entity type for this client
 * 
 * @author Ryan Pickett
 * 
 */
public class Client<T> {

	private final Class<T> entityType;

	private final URI baseUri;
	
	private final ClientProxyFactory proxyFactory;

	private final RestOperations restOperations;

	Client(Class<T> entityType, Configuration configuration, RestOperations restOperations,
			ClientProxyFactory proxyFactory) {
		this.entityType = entityType;
		this.baseUri = getEntityBaseUri(entityType, configuration);
		this.proxyFactory = proxyFactory;
		this.restOperations = restOperations;
	}
	
	/**
	 * GET a single entity from the entity's base resource (determined by the class's
	 * {@link uk.co.blackpepper.bowman.annotation.RemoteResource} annotation).
	 *
	 * @return the entity, or null if not found
	 */
	public T get() {
		return get(baseUri);
	}
	
	/**
	 * GET a single entity located at the given URI. 
	 * 
	 * @param uri the URI from which to retrieve the entity
	 * @return the entity, or null if not found
	 */
	public T get(URI uri) {
		Resource<T> resource = restOperations.getResource(uri, entityType);
		
		if (resource == null) {
			return null;
		}
		
		return proxyFactory.create(resource, restOperations);
	}

	/**
	 * GET a collection of entities from the entity's base resource (determined by the class's
	 * {@link uk.co.blackpepper.bowman.annotation.RemoteResource} annotation). 
	 * 
	 * @return the entities retrieved
	 */
	public Iterable<T> getAll() {
		return getAll(baseUri);
	}
	
	/**
	 * GET a collection of entities from the given URI.
	 * 
	 * @param uri the URI from which to retrieve the entities
	 * @return the entities retrieved
	 */
	public Iterable<T> getAll(URI uri) {
		List<T> result = new ArrayList<>();

		Resources<Resource<T>> resources = restOperations.getResources(uri, entityType);

		for (Resource<T> resource : resources) {
			result.add(proxyFactory.create(resource, restOperations));
		}

		return result;
	}
	
	/**
	 * POST the given entity to the entity's base resource (determined by the class's
	 * {@link uk.co.blackpepper.bowman.annotation.RemoteResource} annotation).
	 * 
	 * The entity will be updated with the URI ID the remote service has assigned it.
	 * 
	 * @param object the entity to submit
	 * @return the URI ID of the newly created remote entity
	 */
	public URI post(T object) {
		URI resourceUri = restOperations.postForId(baseUri, object);
		
		setId(object, resourceUri);
		
		return resourceUri;
	}
	
	/**
	 * PUT the given entity to the entity's base resource (determined by the class's
	 * {@link uk.co.blackpepper.bowman.annotation.RemoteResource} annotation).
	 *
	 * @param object the entity to submit
	 */
	public void put(T object) {
		restOperations.put(getId(object), object);
	}

	/**
	 * DELETE the entity at the given URI.
	 * 
	 * @param uri a URI of the entity to delete 
	 */
	public void delete(URI uri) {
		restOperations.delete(uri);
	}

	/**
	 * PATCH (partial update) of the entity at the given URI.
	 *
	 * @param uri a URI of the entity to update
	 * @param patch any type that can be serialized to a set of changes, for example a Map
	 * @return The patched entity, or null if no response content was returned
	 */
	public T patch(URI uri, Object patch) {
		Resource<T> resource = restOperations.patchForResource(uri, patch, entityType);

		return proxyFactory.create(resource, restOperations);
	}
	
	URI getBaseUri() {
		return baseUri;
	}
	
	private static URI getEntityBaseUri(Class<?> entityType, Configuration configuration) {
		String path = entityType.getAnnotation(RemoteResource.class).value();
		
		return UriComponentsBuilder.fromUri(configuration.getBaseUri()).path(path).build().toUri();
	}
}
