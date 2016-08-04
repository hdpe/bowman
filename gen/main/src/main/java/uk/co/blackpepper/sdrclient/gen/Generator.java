package uk.co.blackpepper.sdrclient.gen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.PropertySource;

import uk.co.blackpepper.sdrclient.annotation.RemoteResource;
import uk.co.blackpepper.sdrclient.gen.model.Annotation;
import uk.co.blackpepper.sdrclient.gen.model.ClassSource;
import uk.co.blackpepper.sdrclient.gen.model.Field;

public class Generator {

	private static final String CLIENT_ANNOTATION_PACKAGE = RemoteResource.class.getPackage().getName();
	
	public void generate(ClassSource source, GeneratedClassWriter classWriter) throws IOException {

		Annotation expectedAnnotation = getAnnotation(source, RemoteResource.class);

		if (expectedAnnotation == null) {
			return;
		}

		JavaClassSource result = Roaster.create(JavaClassSource.class)
			.setName(source.getName())
			.setPackage(convertToClientPackage(source.getPackage()))
			.addAnnotation(RemoteResource.class)
				.setStringValue((String) expectedAnnotation.values().get("value")).getOrigin();

		Field idField = getIdField(source);

		result.addProperty(URI.class, idField.getName()).removeMutator();

		for (Field field : getNonIdFields(source)) {
			String type = field.getQualifiedTypeNameWithGenerics();
			String fieldTypePackage = getFieldTypePackage(field);
			if (fieldTypePackage.equals(source.getPackage())) {
				type = convertToClientPackage(fieldTypePackage) + "." + getFieldTypeName(field);
			}
					
			PropertySource<?> property = result.addProperty(type, field.getName());
			addAnnotations(property.getAccessor(), getClientAnnotations(field.getAnnotations()));
		}

		classWriter.write(createSourceFileRelativePath(result), result.toString());
	}

	private static String getFieldTypePackage(Field field) {
		String fqTypeName = getFieldQualifiedTypeName(field);
		return fqTypeName.substring(0, fqTypeName.indexOf(".") > -1 ? fqTypeName.lastIndexOf(".")
				: fqTypeName.length());
	}
	
	private static String getFieldTypeName(Field field) {
		String fqTypeName = getFieldQualifiedTypeName(field);
		return fqTypeName.substring(fqTypeName.indexOf(".") > -1 ? (fqTypeName.lastIndexOf(".") + 1) : 0);
	}
	
	private static String getFieldQualifiedTypeName(Field field) {
		String fqTypeName = field.getQualifiedTypeNameWithGenerics();
		fqTypeName = fqTypeName.substring(0, fqTypeName.indexOf("<") > -1 ? fqTypeName.indexOf("<")
				: fqTypeName.length());
		return fqTypeName;
	}
	
	private static String convertToClientPackage(String modelPackage) {
		return modelPackage + ".client";
	}
	
	private static void addAnnotations(MethodSource<?> getter, Collection<Annotation> annotations) {
		for (Annotation annotation : annotations) {
			getter.addAnnotation(annotation.getFullyQualifiedName());
		}
	}

	private static Collection<Annotation> getClientAnnotations(Collection<Annotation> annotations) {
		List<Annotation> result = new ArrayList<Annotation>();
		for (Annotation annotation : annotations) {
			if (isClientAnnotation(annotation)) {
				result.add(annotation);
			}
		}
		return result;
	}

	private static boolean isClientAnnotation(Annotation annotation) {
		return annotation.getFullyQualifiedName().startsWith(CLIENT_ANNOTATION_PACKAGE + ".");
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

	private static Annotation getAnnotation(ClassSource clazz, Class<?> type) {
		for (Annotation annotation : clazz.getAnnotations()) {
			if (type.getName().equals(annotation.getFullyQualifiedName())) {
				return annotation;
			}
		}

		return null;
	}

	private static String createSourceFileRelativePath(JavaClassSource result) {
		return result.getPackage().replaceAll("\\.", File.separator) + File.separator
				+ result.getName() + ".java";
	}
}
