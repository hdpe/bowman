package uk.co.blackpepper.sdrclient.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AnnotationRegistry {

	enum AnnotationTargetType {
		FIELD, PROPERTY;
	}
	
	private static class AnnotationMapping {
		
		private AnnotationMappingCondition condition;
		
		private String targetAnnotationFullyQualifiedName;
		
		private Map<String, Object> targetAnnotationAttributes = new LinkedHashMap<String, Object>();
		
		private AnnotationTargetType targetType;

		AnnotationMapping(AnnotationMappingCondition condition,
			String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes,
			AnnotationTargetType targetType) {
			this.condition = condition;
			this.targetAnnotationFullyQualifiedName = targetAnnotationFullyQualifiedName;
			this.targetAnnotationAttributes = targetAnnotationAttributes;
			this.targetType = targetType;
		}

		public boolean isApplicableFor(PropertyGenerationContext sourceProperty) {
			return condition.appliesTo(sourceProperty);
		}

		public String getTargetAnnotationFullyQualifiedName() {
			return targetAnnotationFullyQualifiedName;
		}

		public Map<String, Object> getTargetAnnotationAttributes() {
			return targetAnnotationAttributes;
		}
		
		public AnnotationTargetType getTargetType() {
			return targetType;
		}
	}
	
	interface AnnotationMappingCondition {
		boolean appliesTo(PropertyGenerationContext sourceProperty);
	}
	
	private static class NameMatchAnnotationMappingCondition implements AnnotationMappingCondition {

		private String sourceAnnotationFullyQualifiedName;

		NameMatchAnnotationMappingCondition(String sourceAnnotationFullyQualifiedName) {
			this.sourceAnnotationFullyQualifiedName = sourceAnnotationFullyQualifiedName;
		}
		
		@Override
		public boolean appliesTo(PropertyGenerationContext sourceProperty) {
			return sourceProperty.hasAnnotation(sourceAnnotationFullyQualifiedName);
		}
	}
	
	private List<AnnotationMapping> annotations = new ArrayList<AnnotationMapping>();
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName) {
		registerAnnotationMapping(sourceAnnotationFullyQualifiedName, targetAnnotationFullyQualifiedName,
			Collections.<String, Object>emptyMap());
	}
	
	public void registerAnnotationMapping(AnnotationMappingCondition condition,
			String targetAnnotationFullyQualifiedName) {
		registerAnnotationMapping(condition, targetAnnotationFullyQualifiedName,
				Collections.<String, Object>emptyMap(), AnnotationTargetType.PROPERTY);
	}
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName, AnnotationTargetType targetType) {
		registerAnnotationMapping(sourceAnnotationFullyQualifiedName, targetAnnotationFullyQualifiedName,
			Collections.<String, Object>emptyMap(), targetType);
	}
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName,
			Map<String, Object> targetAnnotationAttributes) {
		registerAnnotationMapping(sourceAnnotationFullyQualifiedName, targetAnnotationFullyQualifiedName,
			targetAnnotationAttributes, AnnotationTargetType.PROPERTY);
	}
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
		String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes,
		AnnotationTargetType targetType) {
		
		registerAnnotationMapping(new NameMatchAnnotationMappingCondition(sourceAnnotationFullyQualifiedName),
			targetAnnotationFullyQualifiedName, targetAnnotationAttributes, targetType);
	}
	
	public void registerAnnotationMapping(AnnotationMappingCondition condition,
			String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes,
			AnnotationTargetType targetType) {
		
		annotations.add(new AnnotationMapping(condition, targetAnnotationFullyQualifiedName, targetAnnotationAttributes,
			targetType));
	}
	
	public void applyAnnotations(PropertyGenerationContext sourceProperty, AnnotationApplicator applicator) {
		for (AnnotationMapping annotation : annotations) {
			if (annotation.isApplicableFor(sourceProperty)) {
				applicator.apply(annotation.getTargetAnnotationFullyQualifiedName(),
					annotation.getTargetAnnotationAttributes(), annotation.getTargetType());
			}
		}
	}
}
