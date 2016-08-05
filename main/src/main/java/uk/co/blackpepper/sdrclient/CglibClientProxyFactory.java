package uk.co.blackpepper.sdrclient;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Collection;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import uk.co.blackpepper.sdrclient.gen.annotation.LinkedResource;

public class CglibClientProxyFactory implements ClientProxyFactory {

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

			if ("getURI".equals(methodName) || !methodName.startsWith("get")) {
				return proxy.invokeSuper(obj, args);
			}
			
			if (value == null) {
				// TODO: reduce chattiness
				resource = restTemplate.getForObject(uri, Resource.class);
				value = restTemplate.getForObject(uri, entityType);
			}

			if (method.getAnnotation(LinkedResource.class) != null) {
				
				URI associationResource = URI.create(resource.getLink(toLinkName(methodName)).getHref());
				
				Object result;
				
				// TODO: reduce this chattiness too, and cache
				if (Collection.class.isAssignableFrom(method.getReturnType())) {
					
					Resources<Resource<?>> resources = restTemplate.exchange(associationResource, HttpMethod.GET,
							null, new ParameterizedTypeReference<Resources<Resource<?>>>() { }).getBody();
					
					Class<?> entityType = (Class<?>) ((ParameterizedType) method.getGenericReturnType())
							.getActualTypeArguments()[0];
					
					Collection collection = (Collection) proxy.invokeSuper(obj, new Object[0]);
					collection.clear();
					
					for (Resource<?> resource : resources) {
						URI linkedResource = URI.create(resource.getLink(Link.REL_SELF).getHref());
						
						collection.add(createProxy(linkedResource, entityType, restTemplate));
					}
					
					result = collection;
				}
				else {
					URI linkedResource = URI.create(restTemplate.getForObject(associationResource, Resource.class)
							.getLink(Link.REL_SELF).getHref());
					
					result = createProxy(linkedResource, (Class) method.getReturnType(), restTemplate);
				}
				
				return result;
			}
			
			return proxy.invoke(value, args);
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
	
	public <T> T create(URI uri, Class<T> entityType, RestTemplate restTemplate) {
		return createProxy(uri, entityType, restTemplate);
	}

	private static <T> T createProxy(URI uri, Class<T> entityType, RestTemplate restTemplate) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(entityType);
		enhancer.setCallback(new GetterInterceptor<T>(uri, entityType, restTemplate));

		@SuppressWarnings("unchecked")
		T value = (T) enhancer.create();

		ReflectionSupport.setId(value, uri);

		return value;
	}
}
