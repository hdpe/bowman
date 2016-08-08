package uk.co.blackpepper.sdrclient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Collection;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javassist.util.proxy.MethodHandler;
import uk.co.blackpepper.sdrclient.gen.annotation.LinkedResource;

import static uk.co.blackpepper.sdrclient.HalSupport.toLinkName;

class GetterMethodHandler<T> implements MethodHandler {
	
	private final URI uri;
	
	private final Class<T> entityType;
	
	private final RestTemplate restTemplate;

	private Resource<T> resource;
	
	private T value;

	private ClientProxyFactory proxyFactory;
	
	GetterMethodHandler(URI uri, Class<T> entityType, RestTemplate restTemplate, ClientProxyFactory proxyFactory) {
		this(uri, null, null, entityType, restTemplate, proxyFactory);
	}
	
	GetterMethodHandler(Resource<T> resource, Class<T> entityType, RestTemplate restTemplate,
		ClientProxyFactory proxyFactory) {
		this(null, resource, resource.getContent(), entityType, restTemplate, proxyFactory);
	}
	
	private GetterMethodHandler(URI uri, Resource<T> resource, T value, Class<T> entityType, RestTemplate restTemplate,
		ClientProxyFactory proxyFactory) {
		this.uri = uri;
		this.resource = resource;
		this.value = value;
		this.entityType = entityType;
		this.restTemplate = restTemplate;
		this.proxyFactory = proxyFactory;
	}

	// CHECKSTYLE:OFF
	
	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
		
		// CHECKSTYLE:ON
		
		if (value == null) {
			// TODO: reduce chattiness
			resource = restTemplate.getForObject(uri, Resource.class);
			value = restTemplate.getForObject(uri, entityType);
		}

		if (method.getAnnotation(LinkedResource.class) != null) {
			return resolveLinkedResource(self, method, proceed);
		}
		
		return method.invoke(value, args);
	}

	private Object resolveLinkedResource(Object self, Method method, Method proceed)
			throws IllegalAccessException, InvocationTargetException {
		
		URI associationResource = URI.create(resource.getLink(toLinkName(method.getName())).getHref());
		
		if (Collection.class.isAssignableFrom(method.getReturnType())) {
			return resolveCollectionLinkedResource(self, method, proceed, associationResource);
		}

		return resolveSingleLinkedResource(method, associationResource);
	}

	// TODO: reduce this chattiness too, and cache
	private Object resolveSingleLinkedResource(Method method, URI associationResource) {
		Resource<?> linkedResource = doGetResource(associationResource);
		
		if (linkedResource == null) {
			return null;
		}
		
		URI linkedResourceUri = URI.create(linkedResource.getLink(Link.REL_SELF).getHref());
		
		return proxyFactory.create(linkedResourceUri, (Class) method.getReturnType(), restTemplate);
	}

	// TODO: reduce this chattiness too, and cache
	private Object resolveCollectionLinkedResource(Object self, Method method, Method proceed,
			URI associationResource) throws IllegalAccessException, InvocationTargetException {
		Resources<Resource<?>> resources = restTemplate.exchange(associationResource, HttpMethod.GET,
				null, new ParameterizedTypeReference<Resources<Resource<?>>>() { }).getBody();
		
		Class<?> entityType = (Class<?>) ((ParameterizedType) method.getGenericReturnType())
				.getActualTypeArguments()[0];
		
		Collection collection = (Collection) proceed.invoke(self);
		collection.clear();
		
		for (Resource<?> resource : resources) {
			URI linkedResource = URI.create(resource.getLink(Link.REL_SELF).getHref());
			
			collection.add(proxyFactory.create(linkedResource, entityType, restTemplate));
		}
		
		return collection;
	}

	private Resource<?> doGetResource(URI associationResource) {
		Resource<?> linkedResource = null;
		try {
			linkedResource = restTemplate.getForObject(associationResource, Resource.class);
		}
		catch (HttpClientErrorException exception) {
			if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}
			
			throw exception;
		}
		return linkedResource;
	}
}
