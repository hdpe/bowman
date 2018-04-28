package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import org.springframework.hateoas.Resource;

class SetterMethodHandler extends AbstractContentDelegatingMethodHandler {
	
	SetterMethodHandler(Resource<?> resource) {
		super(resource);
	}
	
	@Override
	public boolean supports(Method method) {
		return method.getName().startsWith("set");
	}
}
