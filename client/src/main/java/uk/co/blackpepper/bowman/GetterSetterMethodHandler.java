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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import javassist.util.proxy.MethodHandler;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

import static uk.co.blackpepper.bowman.HalSupport.toLinkName;

@JsonIgnoreType
class GetterSetterMethodHandler<T> implements MethodHandler {
	
	private final URI uri;
	
	private final Class<T> entityType;
	
	private final RestOperations restOperations;

	private Resource<T> resource;
	
	private final ClientProxyFactory proxyFactory;
	
	private final PropertyValueFactory propertyValueFactory = new DefaultPropertyValueFactory();
	
	private final Map<String, Object> linkedResourceResults = new HashMap<>();
	
	GetterSetterMethodHandler(Resource<T> resource, Class<T> entityType, RestOperations restOperations,
		ClientProxyFactory proxyFactory) {
		this(getResourceURI(resource), resource, entityType, restOperations, proxyFactory);
	}

	private GetterSetterMethodHandler(URI uri, Resource<T> resource, Class<T> entityType, RestOperations restOperations,
		ClientProxyFactory proxyFactory) {
		this.uri = uri;
		this.resource = resource;
		this.entityType = entityType;
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}

	// CHECKSTYLE:OFF
	
	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
		
		// CHECKSTYLE:ON
		
		if (method.getName().startsWith("set")) {
			return method.invoke(resource.getContent(), args);
		}
		
		if (method.isAnnotationPresent(ResourceId.class)) {
			return uri;
		}
		
		if (resource == null) {
			resource = restOperations.getResource(uri, entityType);
		}

		if (method.isAnnotationPresent(LinkedResource.class)) {
			Object linkedResourceResult = linkedResourceResults.get(method.getName());
			
			if (linkedResourceResult == null) {
				linkedResourceResult = resolveLinkedResource(self, method, proceed);
				linkedResourceResults.put(method.getName(), linkedResourceResult);
			}
			
			return linkedResourceResult;
		}
		
		return method.invoke(resource.getContent(), args);
	}

	private Object resolveLinkedResource(Object self, Method method, Method proceed)
			throws IllegalAccessException, InvocationTargetException {
		
		String linkName = getLinkName(method);
		Link link = resource.getLink(linkName);
		
		if (link == null) {
			throw new ClientProxyException(String.format("Link '%s' could not be found!", linkName));
		}
		
		URI associationResource = URI.create(link.getHref());
		
		if (Collection.class.isAssignableFrom(method.getReturnType())) {
			Class<?> linkedEntityType = (Class<?>) ((ParameterizedType) method.getGenericReturnType())
				.getActualTypeArguments()[0];
			
			return resolveCollectionLinkedResource(associationResource, linkedEntityType, self, proceed);
		}

		return resolveSingleLinkedResource(associationResource, method.getReturnType());
	}

	private <F> F resolveSingleLinkedResource(URI associationResource, Class<F> linkedEntityType) {
		Resource<F> linkedResource = restOperations.getResource(associationResource, linkedEntityType);
		
		if (linkedResource == null) {
			return null;
		}
		
		return proxyFactory.create(linkedResource, linkedEntityType, restOperations);
	}

	private <F> Collection<F> resolveCollectionLinkedResource(URI associationResource, Class<F> linkedEntityType,
		Object contextEntity, Method originalMethod) throws IllegalAccessException, InvocationTargetException {
		
		Resources<Resource<F>> resources = restOperations.getResources(associationResource, linkedEntityType);
		
		@SuppressWarnings("unchecked")
		Collection<F> collection = (Collection<F>) originalMethod.invoke(contextEntity);
		
		if (collection == null) {
			collection = propertyValueFactory.createCollection(originalMethod.getReturnType());
		}
		else {
			collection.clear();
		}
		
		for (Resource<F> resource : resources) {
			collection.add(proxyFactory.create(resource, linkedEntityType, restOperations));
		}
		
		return collection;
	}
	
	private static String getLinkName(Method method) {
		String rel = method.getAnnotation(LinkedResource.class).rel();
		
		if ("".equals(rel)) {
			rel = toLinkName(method.getName());
		}
		
		return rel;
	}

	private static <T> URI getResourceURI(Resource<T> resource) {
		Link selfLink = resource.getLink(Link.REL_SELF);
		return selfLink == null ? null : URI.create(selfLink.getHref());
	}
}
