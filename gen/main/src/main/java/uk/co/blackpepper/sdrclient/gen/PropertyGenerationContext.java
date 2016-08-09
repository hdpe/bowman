package uk.co.blackpepper.sdrclient.gen;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;

import javax.lang.model.SourceVersion;

import uk.co.blackpepper.sdrclient.gen.model.Annotation;
import uk.co.blackpepper.sdrclient.gen.model.ClassSource;
import uk.co.blackpepper.sdrclient.gen.model.Field;

class PropertyGenerationContext {

	private final Field field;
	
	private final ClassSource clazz;
	
	private final String targetPackageName;

	private final Logger logger;

	PropertyGenerationContext(Field field, ClassSource clazz, String targetPackageName, Logger logger) {
		this.field = field;
		this.clazz = clazz;
		this.targetPackageName = targetPackageName;
		this.logger = logger;
	}

	public String getName() {
		return field.getName();
	}

	public String getTargetType() {
		if (isIdField()) {
			return URI.class.getName();
		}
		
		return convertPackageIfRequired(field.getQualifiedTypeNameWithGenerics());
	}

	public String convertPackageIfRequired(String type) {
		if (clazz.getPackage() == null) {
			return type;
		}
		
		return type.replaceAll(clazz.getPackage(), targetPackageName);
	}
	
	public Iterable<Annotation> getAnnotations() {
		return field.getAnnotations();
	}

	public boolean hasAnnotation(String fullyQualifiedName) {
		return getFieldAnnotation(fullyQualifiedName) != null;
	}
	
	public boolean isIdField() {
		return hasAnnotation(javax.persistence.Id.class.getName());
	}
	
	public boolean isCollectionField() {
		String type = getQualifiedType();
		
		if (SourceVersion.isKeyword(type)) {
			// primitive
			return false;
		}
		
		try {
			return Collection.class.isAssignableFrom(Class.forName(type));
		}
		catch (ClassNotFoundException exception) {
			logger.debug("couldn't find class " + type, exception);
			return false;
		}
	}

	private String getQualifiedType() {
		String type = field.getQualifiedTypeNameWithGenerics();
		return type.substring(0, type.indexOf("<") > -1 ? type.indexOf("<") : type.length());
	}
		
	public boolean isAssociationField() {
		return Objects.equals(getPackage(), clazz.getPackage())
			|| (isCollectionField() && Objects.equals(getPackageFromType(getTypeArgs()), clazz.getPackage()));
	}

	private String getPackage() {
		return getPackageFromType(getQualifiedType());
	}

	private static String getPackageFromType(String type) {
		return type.indexOf(".") > -1 ? type.substring(0, type.lastIndexOf(".")) : null;
	}

	public String getTypeArgs() {
		String type = field.getQualifiedTypeNameWithGenerics();
		return type.substring(type.indexOf("<") + 1, type.lastIndexOf(">"));
	}

	public String getImplementationType() {
		String qualifiedName = getQualifiedType();
		
		if ("java.util.Set".equals(qualifiedName)) {
			return "java.util.LinkedHashSet";
		}
		if ("java.util.List".equals(qualifiedName)) {
			return "java.util.ArrayList";
		}
		if ("java.util.SortedSet".equals(qualifiedName)) {
			return "java.util.TreeSet";
		}
		
		return null;
	}
	
	private Annotation getFieldAnnotation(String fullyQualifiedType) {
		for (Annotation annotation : field.getAnnotations()) {
			if (fullyQualifiedType.equals(annotation.getFullyQualifiedName())) {
				return annotation;
			}
		}
		
		return null;
	}
}
