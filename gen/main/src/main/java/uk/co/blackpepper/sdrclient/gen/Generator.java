package uk.co.blackpepper.sdrclient.gen;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.SourceVersion;
import javax.persistence.Entity;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.PropertySource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import uk.co.blackpepper.sdrclient.EmbeddedChildDeserializer;
import uk.co.blackpepper.sdrclient.gen.AnnotationRegistry.AnnotationTargetType;
import uk.co.blackpepper.sdrclient.gen.annotation.IdField;
import uk.co.blackpepper.sdrclient.gen.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.gen.annotation.RemoteResource;
import uk.co.blackpepper.sdrclient.gen.model.Annotation;
import uk.co.blackpepper.sdrclient.gen.model.ClassSource;
import uk.co.blackpepper.sdrclient.gen.model.Field;

public class Generator {

	private static AnnotationRegistry annotationRegistry = new AnnotationRegistry();
	
	static {
		annotationRegistry.registerAnnotationMapping(
				javax.persistence.Id.class.getName(),
				JsonIgnore.class.getName());
		
		annotationRegistry.registerAnnotationMapping(
				javax.persistence.Id.class.getName(),
				IdField.class.getName(),
				AnnotationTargetType.FIELD);
		
		annotationRegistry.registerAnnotationMapping(
				uk.co.blackpepper.sdrclient.annotation.RemoteResource.class.getName(),
				RemoteResource.class.getName());
		
		annotationRegistry.registerAnnotationMapping(
				uk.co.blackpepper.sdrclient.annotation.LinkedResource.class.getName(),
				LinkedResource.class.getName());
		
		annotationRegistry.registerAnnotationMapping(
				uk.co.blackpepper.sdrclient.annotation.EmbeddedResource.class.getName(),
				JsonDeserialize.class.getName(),
				Collections.<String, Object>singletonMap("using", EmbeddedChildDeserializer.class));
		
		annotationRegistry.registerAnnotationMapping(
				uk.co.blackpepper.sdrclient.annotation.EmbeddedResources.class.getName(),
				JsonDeserialize.class.getName(),
				Collections.<String, Object>singletonMap("contentUsing", EmbeddedChildDeserializer.class));
	}

	private Logger logger;
	
	public Generator(Logger logger) {
		this.logger = logger;
	}
	
	public void generate(ClassSource source, String targetPackageName, GeneratedClassWriter classWriter)
		throws IOException {

		Annotation entityAnnotation = getAnnotation(source, Entity.class);
		
		if (entityAnnotation == null) {
			return;
		}
		
		JavaClassSource result = Roaster.create(JavaClassSource.class)
				.setName(source.getName())
				.setPackage(targetPackageName);
		
		Annotation remoteResourceAnnotation = getAnnotation(source,
				uk.co.blackpepper.sdrclient.annotation.RemoteResource.class);
		
		if (remoteResourceAnnotation != null) {
			result.addAnnotation(RemoteResource.class)
					.setStringValue((String) remoteResourceAnnotation.values().get("value")).getOrigin();
		}

		for (Field field : source.getFields()) {
			String type = convertFieldType(field, source, targetPackageName);
			PropertySource<?> property = result.addProperty(type, field.getName());
			
			if (isIdField(field) || isCollectionField(field)) {
				property.removeMutator();
			}
			
			addAnnotations(property, field.getAnnotations());
			addInitializer(property.getField(), result);
		}
				
		logger.info("Generated data model class " + result.getQualifiedName());
		
		classWriter.write(createSourceFileRelativePath(result), result.toString());
	}
	
	private static boolean isIdField(Field field) {
		return getFieldAnnotation(field, javax.persistence.Id.class.getName()) != null;
	}
	
	private boolean isCollectionField(Field field) {
		String type = field.getQualifiedTypeNameWithGenerics();
		type = type.substring(0, type.indexOf("<") > -1 ? type.indexOf("<") : type.length());
		
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
	
	private static Annotation getFieldAnnotation(Field field, String fullyQualifiedType) {
		for (Annotation annotation : field.getAnnotations()) {
			if (fullyQualifiedType.equals(annotation.getFullyQualifiedName())) {
				return annotation;
			}
		}
		
		return null;
	}

	private static void addInitializer(FieldSource<?> field, JavaClassSource result) {
		String implementationType = getImplementationType(field);
		if (implementationType != null) {
			String simpleName = result.addImport(implementationType).getSimpleName();
			String typeArgs = getTypeArgs(field.getType());
			
			field.setLiteralInitializer("new " + simpleName + typeArgs + "()");
		}
	}

	private static String getTypeArgs(Type<?> type) {
		StringBuilder sb = new StringBuilder("<");
		for (Type<?> typeArg : type.getTypeArguments()) {
			if (sb.length() > 1) {
				sb.append(", ");
			}
			sb.append(typeArg);
		}
		return sb.append(">").toString();
	}

	private static String getImplementationType(FieldSource<?> field) {
		String qualifiedName = field.getType().getQualifiedName();
		
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

	private static String convertFieldType(Field field, ClassSource source, String targetPackageName) {
		if (isIdField(field)) {
			return URI.class.getName();
		}
		
		if (source.getPackage() == null) {
			return field.getQualifiedTypeNameWithGenerics();
		}
		
		return field.getQualifiedTypeNameWithGenerics().replaceAll(source.getPackage(), targetPackageName);
	}
	
	private static void addAnnotations(PropertySource<?> property, Collection<Annotation> fieldAnnotations) {
		AnnotationApplicator applicator = createAnnotationApplicator(property);
		
		for (Annotation annotation : fieldAnnotations) {
			annotationRegistry.applyAnnotations(annotation.getFullyQualifiedName(), applicator);
		}
	}
	
	private static AnnotationApplicator createAnnotationApplicator(final PropertySource<?> property) {
		return new AnnotationApplicator() {
			
			@Override
			public void apply(String fullyQualifiedAnnotationName, Map<String, Object> annotationAttributes,
				AnnotationTargetType targetType) {
				
				AnnotationSource<?> annotation;
				
				if (targetType == AnnotationTargetType.FIELD) {
					annotation = property.getField().addAnnotation(fullyQualifiedAnnotationName);
				}
				else {
					annotation = property.getAccessor().addAnnotation(fullyQualifiedAnnotationName);
				}
				
				for (Entry<String, Object> attr : annotationAttributes.entrySet()) {
					if (attr.getValue() instanceof Class<?>) {
						annotation.setClassValue(attr.getKey(), (Class<?>) attr.getValue());
					}
				}
			}
		};
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
