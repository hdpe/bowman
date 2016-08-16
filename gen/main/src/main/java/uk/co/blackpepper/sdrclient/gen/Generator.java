package uk.co.blackpepper.sdrclient.gen;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.Entity;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.PropertySource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import uk.co.blackpepper.sdrclient.EmbeddedChildDeserializer;
import uk.co.blackpepper.sdrclient.annotation.IdAccessor;
import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;
import uk.co.blackpepper.sdrclient.gen.AnnotationRegistry.AnnotationMappingCondition;
import uk.co.blackpepper.sdrclient.gen.annotation.EmbeddedResource;
import uk.co.blackpepper.sdrclient.gen.annotation.EmbeddedResources;
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
				IdAccessor.class.getName());
		
		annotationRegistry.registerAnnotationMapping(
				uk.co.blackpepper.sdrclient.gen.annotation.RestRepository.class.getName(),
				RemoteResource.class.getName());
		
		annotationRegistry.registerAnnotationMapping(
				new AnnotationMappingCondition() {
					
					@Override
					public boolean appliesTo(PropertyGenerationContext sourceProperty) {
						return sourceProperty.isAssociationField()
							&& !sourceProperty.hasAnnotation(EmbeddedResource.class.getName())
							&& !sourceProperty.hasAnnotation(EmbeddedResources.class.getName());
					}
				},
				LinkedResource.class.getName());
		
		annotationRegistry.registerAnnotationMapping(
				EmbeddedResource.class.getName(),
				JsonDeserialize.class.getName(),
				Collections.<String, Object>singletonMap("using", EmbeddedChildDeserializer.class));
		
		annotationRegistry.registerAnnotationMapping(
				EmbeddedResources.class.getName(),
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
				uk.co.blackpepper.sdrclient.gen.annotation.RestRepository.class);
		
		if (remoteResourceAnnotation != null) {
			result.addAnnotation(RemoteResource.class)
					.setStringValue((String) remoteResourceAnnotation.values().get("value")).getOrigin();
		}

		for (Field field : source.getFields()) {
			PropertyGenerationContext sourceProperty = new PropertyGenerationContext(field, source, targetPackageName,
				logger);
			
			if (!sourceProperty.isIgnoredField()) {
				generateProperty(sourceProperty, result);
			}
		}
				
		logger.info("Generated data model class " + result.getQualifiedName());
		
		classWriter.write(createSourceFileRelativePath(result), result.toString());
	}

	private static void generateProperty(PropertyGenerationContext sourceProperty, JavaClassSource result) {
		PropertySource<?> property = result.addProperty(sourceProperty.getTargetType(), sourceProperty.getName());
		
		if (sourceProperty.isIdField() || sourceProperty.isCollectionField()) {
			property.removeMutator();
		}
		
		addAnnotations(sourceProperty, property);
		addInitializer(sourceProperty, property, result);
	}

	private static void addInitializer(PropertyGenerationContext sourceProperty, PropertySource<?> property,
			JavaClassSource result) {
		String implementationType = sourceProperty.getImplementationType();
		if (implementationType != null) {
			String simpleName = result.addImport(implementationType).getSimpleName();
			String typeArgs = sourceProperty.convertPackageIfRequired(sourceProperty.getTypeArgs());
			
			property.getField().setLiteralInitializer("new " + simpleName + "<" + typeArgs + ">()");
		}
	}
	
	private static void addAnnotations(PropertyGenerationContext sourceProperty, PropertySource<?> property) {
		annotationRegistry.applyAnnotations(sourceProperty, createAnnotationApplicator(property));
	}
	
	private static AnnotationApplicator createAnnotationApplicator(final PropertySource<?> property) {
		return new AnnotationApplicator() {
			
			@Override
			public void apply(String fullyQualifiedAnnotationName, Map<String, Object> annotationAttributes) {
				
				AnnotationSource<?> annotation = property.getAccessor()
					.addAnnotation(fullyQualifiedAnnotationName);
				
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
