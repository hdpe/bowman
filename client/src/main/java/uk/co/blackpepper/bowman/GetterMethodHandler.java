package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import org.springframework.hateoas.Resource;

class GetterMethodHandler extends AbstractContentDelegatingMethodHandler {
	
	GetterMethodHandler(Resource<?> resource) {
		super(resource);
	}
	
	@Override
	public boolean supports(Method method) {
		return method.getName().startsWith("get") || method.getName().startsWith("is");
	}
}
