package uk.co.blackpepper.sdrclient.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.co.blackpepper.sdrclient.gen.model.Annotation;
import uk.co.blackpepper.sdrclient.gen.model.ClassSource;
import uk.co.blackpepper.sdrclient.gen.model.Field;

public class ReflectionClassSourceAdapter implements ClassSource {

	private static class AnnotationImpl implements Annotation {

		private final java.lang.annotation.Annotation annotation;

		AnnotationImpl(java.lang.annotation.Annotation annotation) {
			this.annotation = annotation;
		}

		@Override
		public String getFullyQualifiedName() {
			return annotation.annotationType().getTypeName();
		}

		@Override
		public Map<String, Object> values() {
			Map<String, Object> result = new LinkedHashMap<String, Object>();
			try {
				result.put("value", annotation.annotationType().getMethod("value").invoke(annotation));
			}
			catch (Exception exception) {
				throw new IllegalStateException(exception);
			}
			return result;
		}
	}

	private static class FieldImpl implements Field {

		private final java.lang.reflect.Field field;

		FieldImpl(java.lang.reflect.Field field) {
			this.field = field;
		}

		@Override
		public String getName() {
			return field.getName();
		}

		@Override
		public String getQualifiedTypeNameWithGenerics() {
			return field.getGenericType().getTypeName();
		}

		@Override
		public Collection<Annotation> getAnnotations() {
			List<Annotation> result = new ArrayList<Annotation>();
			for (java.lang.annotation.Annotation annotation : field.getAnnotations()) {
				result.add(new AnnotationImpl(annotation));
			}
			return result;
		}
	}

	private final Class<?> clazz;

	public ReflectionClassSourceAdapter(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public String getName() {
		return clazz.getSimpleName();
	}

	@Override
	public String getPackage() {
		return clazz.getPackage().getName();
	}

	@Override
	public Collection<Annotation> getAnnotations() {
		List<Annotation> result = new ArrayList<Annotation>();

		for (java.lang.annotation.Annotation annotation : clazz.getAnnotations()) {
			result.add(new AnnotationImpl(annotation));
		}

		return result;
	}

	@Override
	public Collection<Field> getFields() {
		List<Field> result = new ArrayList<Field>();

		for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
			result.add(new FieldImpl(field));
		}

		return result;
	}
}
