package uk.co.blackpepper.bowman;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.lang.reflect.Method;

import org.springframework.hateoas.Resource;

abstract class AbstractContentDelegatingMethodHandler implements ConditionalMethodHandler {
	
	interface BeanInfoProvider {
		BeanInfo getBeanInfo(Class<?> clazz) throws IntrospectionException;
	}
	
	private final Resource<?> resource;
	
	private final BeanInfo contentBeanInfo;
	
	protected AbstractContentDelegatingMethodHandler(Resource<?> resource) {
		this(resource, Introspector::getBeanInfo);
	}
	
	AbstractContentDelegatingMethodHandler(Resource<?> resource, BeanInfoProvider beanInfoProvider) {
		this.resource = resource;
		
		Class<?> resourceContentType = resource.getContent().getClass();
		
		try {
			contentBeanInfo = beanInfoProvider.getBeanInfo(resourceContentType);
		}
		catch (IntrospectionException exception) {
			throw new ClientProxyException(String.format("couldn't determine properties for %s", resourceContentType),
				exception);
		}
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
		return method.invoke(resource.getContent(), args);
	}
	
	protected BeanInfo getContentBeanInfo() {
		return contentBeanInfo;
	}
}
