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

import java.lang.reflect.Method;

import org.springframework.hateoas.Resource;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

class JavassistClientProxyFactory implements ClientProxyFactory {

	private static final class GetterSetterMethodFilter implements MethodFilter {
		@Override
		public boolean isHandled(Method method) {
			return method.getName().startsWith("get")
				|| method.getName().startsWith("is") || method.getName().startsWith("set");
		}
	}
	
	private static final MethodFilter FILTER_INSTANCE = new GetterSetterMethodFilter();

	@Override
	public <T> T create(Resource<T> resource, Class<T> entityType, RestOperations restOperations) {
		return createProxyInstance(entityType,
			new GetterSetterMethodHandler<>(resource, entityType, restOperations, this));
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
