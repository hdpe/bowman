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

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import uk.co.blackpepper.sdrclient.gen.annotation.LinkedResource;

import static uk.co.blackpepper.sdrclient.HalSupport.toLinkName;

public class JavassistClientProxyFactory implements ClientProxyFactory {

	private static class GetterMethodHandler<T> implements MethodHandler {
		
		private final URI uri;
		
		private final Class<T> entityType;
		
		private final RestTemplate restTemplate;

		private Resource<T> resource;
		
		private T value;
		
		GetterMethodHandler(URI uri, Class<T> entityType, RestTemplate restTemplate) {
			this.uri = uri;
			this.entityType = entityType;
			this.restTemplate = restTemplate;
		}
		
		GetterMethodHandler(Resource<T> resource, Class<T> entityType, RestTemplate restTemplate) {
			this.uri = null;
			this.resource = resource;
			this.value = resource.getContent();
			this.entityType = entityType;
			this.restTemplate = restTemplate;
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
			
			return createProxy(linkedResourceUri, (Class) method.getReturnType(), restTemplate);
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
				
				collection.add(createProxy(linkedResource, entityType, restTemplate));
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
	
	@Override
	public <T> T create(URI uri, Class<T> entityType, RestTemplate restTemplate) {
		return createProxy(uri, entityType, restTemplate);
	}

	@Override
	public <T> T create(Resource<T> resource, Class<T> entityType, RestTemplate  restTemplate) {
		T entity = createProxyInstance(entityType);
		
		((Proxy) entity).setHandler(new GetterMethodHandler<T>(resource, entityType, restTemplate));
		
		return entity;
	}

	private static <T> T createProxy(URI uri, Class<T> entityType, RestTemplate restTemplate) {
		T entity = createProxyInstance(entityType);
		
		((Proxy) entity).setHandler(new GetterMethodHandler<T>(uri, entityType, restTemplate));
		
		return entity;
	}

	private static <T> T createProxyInstance(Class<T> entityType) {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(entityType);
		factory.setFilter(new MethodFilter() {
			
			@Override
			public boolean isHandled(Method m) {
				String methodName = m.getName();
				
				return methodName.startsWith("get") && !"getId".equals(methodName);
			}
		});
		
		Class<?> clazz = factory.createClass();
		
		T entity;
		try {
			entity = (T) clazz.newInstance();
		}
		catch (Exception exception) {
			throw new ClientProxyException("couldn't create proxy instance of " + clazz, exception);
		}
		return entity;
	}
}
