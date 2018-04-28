package uk.co.blackpepper.bowman;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.springframework.hateoas.Resource;

class SetterMethodHandler extends AbstractContentDelegatingMethodHandler {
	
	SetterMethodHandler(Resource<?> resource) {
		super(resource);
	}
	
	@Override
	public boolean supports(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
			.map(PropertyDescriptor::getWriteMethod)
			.anyMatch(method::equals);
	}
}
