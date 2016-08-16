package uk.co.blackpepper.sdrclient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.util.ReflectionUtils;

import uk.co.blackpepper.sdrclient.annotation.ResourceId;

public final class ReflectionSupport {

	private ReflectionSupport() {
	}
	
	public static URI getId(Object object) {
		Method accessor = getIdAccessor(object.getClass());
		return (URI) ReflectionUtils.invokeMethod(accessor, object);
	}

	public static void setId(Object value, URI uri) {
		Field idField = getIdField(value.getClass());
		idField.setAccessible(true);
		ReflectionUtils.setField(idField, value, uri);
	}
	
	private static Method getIdAccessor(Class<?> clazz) {
		for (Method method : ReflectionUtils.getAllDeclaredMethods(clazz)) {
			if (method.getAnnotation(ResourceId.class) != null) {
				return method;
			}
		}
		
		throw new IllegalArgumentException("No @IdAccessor found for " + clazz);
	}

	private static Field getIdField(Class<?> clazz) {
		Method idAccessor = getIdAccessor(clazz);
		return ReflectionUtils.findField(clazz, HalSupport.toLinkName(idAccessor.getName()));
	}
}
