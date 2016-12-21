package uk.co.blackpepper.halclient;

import java.lang.reflect.Method;

import org.springframework.hateoas.Resource;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

class JavassistClientProxyFactory implements ClientProxyFactory {

	private static final class GetterMethodFilter implements MethodFilter {
		@Override
		public boolean isHandled(Method method) {
			return method.getName().startsWith("get");
		}
	}
	
	private static final MethodFilter FILTER_INSTANCE = new GetterMethodFilter();

	@Override
	public <T> T create(Resource<T> resource, Class<T> entityType, RestOperations restOperations) {
		return createProxyInstance(entityType, new GetterMethodHandler<>(resource, entityType, restOperations, this));
	}

	private static <T> T createProxyInstance(Class<T> entityType, MethodHandler methodHandler) {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(entityType);
		factory.setFilter(FILTER_INSTANCE);
		
		Class<?> clazz = factory.createClass();
		T proxy = instantiateClass(clazz);
		((Proxy) proxy).setHandler(methodHandler);
		return proxy;
	}

	private static <T> T instantiateClass(Class<?> clazz) {
		try {
			@SuppressWarnings("unchecked")
			T proxy = (T) clazz.newInstance();
			return proxy;
		}
		catch (Exception exception) {
			throw new ClientProxyException("couldn't create proxy instance of " + clazz, exception);
		}
	}
}
