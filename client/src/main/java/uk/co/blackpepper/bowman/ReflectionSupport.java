/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.bowman;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.util.ReflectionUtils;

import uk.co.blackpepper.bowman.annotation.ResourceId;

final class ReflectionSupport {

	private static final Class<ResourceId> ID_ACCESSOR_ANNOTATION = ResourceId.class;
	
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
			if (method.getAnnotation(ID_ACCESSOR_ANNOTATION) != null) {
				return method;
			}
		}
		
		throw new IllegalArgumentException(String.format("No @%s found for %s",
			ID_ACCESSOR_ANNOTATION.getSimpleName(), clazz.getName()));
	}

	private static Field getIdField(Class<?> clazz) {
		Method idAccessor = getIdAccessor(clazz);
		return ReflectionUtils.findField(clazz, HalSupport.toLinkName(idAccessor.getName()));
	}
}
