package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import org.springframework.hateoas.Resource;

abstract class AbstractContentDelegatingMethodHandler implements ConditionalMethodHandler {
	
	private final Resource<?> resource;
	
	AbstractContentDelegatingMethodHandler(Resource<?> resource) {
		this.resource = resource;
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
		return method.invoke(resource.getContent(), args);
	}
}
