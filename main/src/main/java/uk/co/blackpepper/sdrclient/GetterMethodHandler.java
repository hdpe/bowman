package uk.co.blackpepper.sdrclient;

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

import javassist.util.proxy.MethodHandler;
import uk.co.blackpepper.sdrclient.gen.annotation.LinkedResource;

import static uk.co.blackpepper.sdrclient.HalSupport.toLinkName;

class GetterMethodHandler<T> implements MethodHandler {
	
	private final URI uri;
	
	private final Class<T> entityType;
	
	private final RestOperations restOperations;

	private Resource<T> resource;
	
	private ClientProxyFactory proxyFactory;
	
	private Map<String, Object> linkedResourceResults = new HashMap<String, Object>();
	
	GetterMethodHandler(URI uri, Class<T> entityType, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		this(uri, null, entityType, restOperations, proxyFactory);
	}
	
	GetterMethodHandler(Resource<T> resource, Class<T> entityType, RestOperations restOperations,
		ClientProxyFactory proxyFactory) {
		this(getResourceURI(resource), resource, entityType, restOperations, proxyFactory);
	}

	private GetterMethodHandler(URI uri, Resource<T> resource, Class<T> entityType, RestOperations restOperations,
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
		
		if (resource == null) {
			resource = restOperations.getResource(uri, entityType);
		}

		if (method.getAnnotation(LinkedResource.class) != null) {
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
		
		URI associationResource = URI.create(resource.getLink(toLinkName(method.getName())).getHref());
		
		if (Collection.class.isAssignableFrom(method.getReturnType())) {
			Class<?> linkedEntityType = (Class<?>) ((ParameterizedType) method.getGenericReturnType())
				.getActualTypeArguments()[0];
			
			return resolveCollectionLinkedResource(associationResource, linkedEntityType, self, proceed);
		}

		return resolveSingleLinkedResource(associationResource, method.getReturnType());
	}

	private Object resolveSingleLinkedResource(URI associationResource, Class<?> linkedEntityType) {
		Resource<?> linkedResource = restOperations.getResource(associationResource, linkedEntityType);
		
		if (linkedResource == null) {
			return null;
		}
		
		return proxyFactory.create(linkedResource, (Class) linkedEntityType, restOperations);
	}

	private Object resolveCollectionLinkedResource(URI associationResource, Class<?> linkedEntityType,
		Object contextEntity, Method originalMethod) throws IllegalAccessException, InvocationTargetException {
		
		Resources<Resource<?>> resources = restOperations.getResources(associationResource, (Class) linkedEntityType);
		
		Collection collection = (Collection) originalMethod.invoke(contextEntity);
		collection.clear();
		
		for (Resource<?> resource : resources) {
			collection.add(proxyFactory.create(resource, (Class) linkedEntityType, restOperations));
		}
		
		return collection;
	}

	private static <T> URI getResourceURI(Resource<T> resource) {
		Link selfLink = resource.getLink(Link.REL_SELF);
		return selfLink == null ? null : URI.create(selfLink.getHref());
	}
}
