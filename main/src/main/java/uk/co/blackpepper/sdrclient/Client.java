package uk.co.blackpepper.sdrclient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class Client<T> {

	private static class GetterInterceptor<T> implements MethodInterceptor {

		private final URI uri;

		private final Class<T> entityType;

		private final RestTemplate restTemplate;

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
					value = restTemplate.getForObject(uri, entityType);
				}

				return proxy.invoke(value, args);
			}

			return proxy.invoke(obj, args);
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
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(entityType);
		enhancer.setCallback(new GetterInterceptor<T>(uri, entityType, restTemplate));

		@SuppressWarnings("unchecked")
		T value = (T) enhancer.create();

		Field idField = ReflectionUtils.findField(entityType, "id");
		idField.setAccessible(true);
		ReflectionUtils.setField(idField, value, uri);

		return value;
	}

	public URI post(T object) {
		return restTemplate.postForLocation(UriComponentsBuilder.fromUri(baseUri).path("/entities").build().toUri(),
				object);
	}

	public void delete(URI uri) {
		restTemplate.delete(uri);
	}
}
