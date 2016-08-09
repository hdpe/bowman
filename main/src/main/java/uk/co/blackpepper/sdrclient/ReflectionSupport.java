package uk.co.blackpepper.sdrclient;

import java.lang.reflect.Field;
import java.net.URI;

import org.springframework.util.ReflectionUtils;

import uk.co.blackpepper.sdrclient.gen.annotation.IdField;

public final class ReflectionSupport {

	private ReflectionSupport() {
	}
	
	public static URI getId(Object object) {
		Field field = getIdField(object.getClass());
		ReflectionUtils.makeAccessible(field);
		return (URI) ReflectionUtils.getField(field, object);
	}

	public static void setId(Object value, URI uri) {
		Field idField = getIdField(value.getClass());
		idField.setAccessible(true);
		ReflectionUtils.setField(idField, value, uri);
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
