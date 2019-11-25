package uk.co.blackpepper.bowman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.hateoas.EntityModel;

class SimplePropertyMethodHandler<T> extends AbstractPropertyAwareMethodHandler {

	private final T content;

	SimplePropertyMethodHandler(EntityModel<T> resource) {
		super(resource.getContent().getClass());
		this.content = resource.getContent();
	}

	@Override
	public boolean supports(Method method) {
		return isSetter(method) || isGetter(method);
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args)
	throws InvocationTargetException, IllegalAccessException {

		return method.invoke(content, args);
	}
}
