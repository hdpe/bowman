package uk.co.blackpepper.sdrclient;

import java.lang.reflect.Field;
import java.net.URI;

import org.springframework.util.ReflectionUtils;

public final class ReflectionSupport {

	private ReflectionSupport() {
	}
	
	public static URI getId(Object object) {
		Field field = ReflectionUtils.findField(object.getClass(), "id");
		ReflectionUtils.makeAccessible(field);
		return (URI) ReflectionUtils.getField(field, object);
	}
}
