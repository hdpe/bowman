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

import java.util.Arrays;

import org.springframework.hateoas.Resource;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import static java.util.Arrays.asList;

class JavassistClientProxyFactory implements ClientProxyFactory {

	@Override
	public <T> T create(Resource<T> resource, RestOperations restOperations) {
		@SuppressWarnings("unchecked")
		Class<T> entityType = (Class<T>) resource.getContent().getClass();
		
		MethodHandlerChain handlerChain = new MethodHandlerChain(asList(
			new ResourceIdMethodHandler(resource),
			new LinkedResourceMethodHandler(resource, restOperations, this),
			new SimplePropertyMethodHandler<>(resource)
		));
		
		return createProxyInstance(entityType, handlerChain);
	}
	
	private static <T> T createProxyInstance(Class<T> entityType, MethodHandlerChain handlerChain) {
		ProxyFactory factory = new ProxyFactory();
		if (ProxyFactory.isProxyClass(entityType)) {
			factory.setInterfaces(getNonProxyInterfaces(entityType));
			factory.setSuperclass(entityType.getSuperclass());
		}
		else {
			factory.setSuperclass(entityType);
		}
		factory.setFilter(handlerChain);
		
		Class<?> clazz = factory.createClass();
		T proxy = instantiateClass(clazz);
		((Proxy) proxy).setHandler(handlerChain);
		return proxy;
	}
	
	private static Class[] getNonProxyInterfaces(Class<?> entityType) {
		return Arrays.stream(entityType.getInterfaces())
			.filter(i -> !Proxy.class.isAssignableFrom(i))
			.toArray(Class[]::new);
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
