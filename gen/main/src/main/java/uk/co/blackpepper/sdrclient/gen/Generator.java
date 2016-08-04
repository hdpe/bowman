package uk.co.blackpepper.sdrclient.gen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;
import uk.co.blackpepper.sdrclient.gen.model.Annotation;
import uk.co.blackpepper.sdrclient.gen.model.ClassSource;
import uk.co.blackpepper.sdrclient.gen.model.Field;

public class Generator {

	public void generate(ClassSource source, GeneratedClassWriter classWriter) throws IOException {

		Annotation expectedAnnotation = getAnnotation(source, RemoteResource.class);

		if (expectedAnnotation == null) {
			return;
		}

		JavaClassSource result = Roaster.create(JavaClassSource.class)
			.setName(source.getName())
			.setPackage(source.getPackage() + ".client")
			.addAnnotation(RemoteResource.class)
				.setStringValue((String) expectedAnnotation.values().get("value")).getOrigin();

		Field idField = getIdField(source);

		result.addProperty(URI.class, idField.getName()).removeMutator();

		for (Field field : getNonIdFields(source)) {
			result.addProperty(field.getQualifiedTypeNameWithGenerics(), field.getName());
		}

		classWriter.write(createSourceFileRelativePath(result), result.toString());
	}

	public Field getIdField(ClassSource clazz) {
		for (Field field : clazz.getFields()) {
			if ("id".equals(field.getName())) {
				return field;
			}
		}

		throw new IllegalStateException("no @Id field found");
	}

	public Collection<Field> getNonIdFields(ClassSource clazz) {
		List<Field> result = new ArrayList<Field>();
		for (Field fieldSource : clazz.getFields()) {
			if (!"id".equals(fieldSource.getName())) {
				result.add(fieldSource);
			}
		}
		return result;
	}

	private Annotation getAnnotation(ClassSource clazz, Class<?> type) {
		for (Annotation annotation : clazz.getAnnotations()) {
			if (type.getName().equals(annotation.getFullyQualifiedName())) {
				return annotation;
			}
		}

		return null;
	}

	private String createSourceFileRelativePath(JavaClassSource result) {
		return result.getPackage().replaceAll("\\.", File.separator) + File.separator
				+ result.getName() + ".java";
	}
}
