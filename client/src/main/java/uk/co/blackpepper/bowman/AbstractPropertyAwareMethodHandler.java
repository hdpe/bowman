package uk.co.blackpepper.bowman;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;

import javassist.util.proxy.ProxyFactory;

abstract class AbstractPropertyAwareMethodHandler implements ConditionalMethodHandler {

	interface BeanInfoProvider {
		BeanInfo getBeanInfo(Class clazz) throws IntrospectionException;
	}

	private final BeanInfo contentBeanInfo;

	AbstractPropertyAwareMethodHandler(Class clazz) {
		this(clazz, Introspector::getBeanInfo);
	}

	AbstractPropertyAwareMethodHandler(Class clazz, BeanInfoProvider beanInfoProvider) {
		try {
			contentBeanInfo = beanInfoProvider.getBeanInfo(getBeanType(clazz));
		}
		catch (IntrospectionException exception) {
			throw new ClientProxyException(String.format("couldn't determine properties for %s", clazz.getName()),
				exception);
		}
	}

	boolean isSetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.map(PropertyDescriptor::getWriteMethod)
				.anyMatch(method::equals);
	}

	boolean isGetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.map(PropertyDescriptor::getReadMethod)
				.anyMatch(method::equals);
	}
	
	BeanInfo getContentBeanInfo() {
		return contentBeanInfo;
	}
	
	private static Class getBeanType(Class clazz) {
		if (!ProxyFactory.isProxyClass(clazz)) {
			return clazz;
		}
		
		return clazz.getSuperclass();
	}
}
