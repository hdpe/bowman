package uk.co.blackpepper.sdrclient;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.util.ReflectionUtils;

import uk.co.blackpepper.sdrclient.annotation.IdAccessor;
import uk.co.blackpepper.sdrclient.annotation.IdField;

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
			if (method.getAnnotation(IdAccessor.class) != null) {
				return method;
			}
		}
		
		throw new IllegalArgumentException("No @IdAccessor found for " + clazz);
	}

	private static Field getIdField(Class<?> clazz) {
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(IdField.class) != null) {
				return field;
			}
		}
		
		throw new IllegalArgumentException("No @IdField found for " + clazz);
	}
}
