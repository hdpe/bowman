package uk.co.blackpepper.sdrclient;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

public class Client<T> {

	private static class GetterInterceptor<T> implements MethodInterceptor {

		private final URI uri;

		private final Class<T> entityType;

		private final RestTemplate restTemplate;

		private Resource<T> resource;
		
		private T value;

		GetterInterceptor(URI uri, Class<T> entityType, RestTemplate restTemplate) {
			this.uri = uri;
			this.entityType = entityType;
			this.restTemplate = restTemplate;
		}

		// CHECKSTYLE:OFF
		
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
			
			// CHECKSTYLE:ON
			
			String methodName = proxy.getSignature().getName();

			if (methodName.startsWith("get") && !"getURI".equals(methodName)) {
				if (value == null) {
					// TODO: reduce chattiness
					resource = restTemplate.getForObject(uri, Resource.class);
					value = restTemplate.getForObject(uri, entityType);
				}

				if (method.getAnnotation(LinkedResource.class) != null) {
					
					URI associationResource = URI.create(resource.getLink(toLinkName(methodName)).getHref());
					
					// TODO: reduce this chattiness too, and cache
					URI linkedResource = URI.create(restTemplate.getForObject(associationResource, Resource.class)
							.getLink(Link.REL_SELF).getHref());
					
					return createProxy(linkedResource, (Class) method.getReturnType(), restTemplate);
				}
				
				return proxy.invoke(value, args);
			}

			return proxy.invoke(obj, args);
		}

		private static String toLinkName(String methodName) {
			if (methodName.startsWith("is")) {
				methodName = methodName.substring(2);
			}
			else if (methodName.startsWith("get")) {
				methodName = methodName.substring(3);
			}
			else {
				throw new IllegalArgumentException("not a bean property method: " + methodName);
			}
			
			return Introspector.decapitalize(methodName);
		}
	}

	private final Class<T> entityType;

	private final URI baseUri;

	private final RestTemplate restTemplate;

	public Client(Class<T> entityType, URI baseUri, RestTemplate restTemplate) {
		this.entityType = entityType;
		this.baseUri = baseUri;
		this.restTemplate = restTemplate;
	}

	public T get(URI uri) {
		return createProxy(uri, entityType, restTemplate);
	}
	
	private static <T> T createProxy(URI uri, Class<T> entityType, RestTemplate restTemplate) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(entityType);
		enhancer.setCallback(new GetterInterceptor<T>(uri, entityType, restTemplate));

		@SuppressWarnings("unchecked")
		T value = (T) enhancer.create();

		setId(value, uri);

		return value;
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

	private static void setId(Object value, URI uri) {
		Field idField = ReflectionUtils.findField(value.getClass(), "id");
		idField.setAccessible(true);
		ReflectionUtils.setField(idField, value, uri);
	}
}
