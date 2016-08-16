package uk.co.blackpepper.sdrclient.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AnnotationRegistry {
	
	private static class AnnotationMapping {
		
		private AnnotationMappingCondition condition;
		
		private String targetAnnotationFullyQualifiedName;
		
		private Map<String, Object> targetAnnotationAttributes = new LinkedHashMap<String, Object>();
		
		AnnotationMapping(AnnotationMappingCondition condition,
			String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes) {
			this.condition = condition;
			this.targetAnnotationFullyQualifiedName = targetAnnotationFullyQualifiedName;
			this.targetAnnotationAttributes = targetAnnotationAttributes;
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
				Collections.<String, Object>emptyMap());
	}
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
		String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes) {
		
		registerAnnotationMapping(new NameMatchAnnotationMappingCondition(sourceAnnotationFullyQualifiedName),
			targetAnnotationFullyQualifiedName, targetAnnotationAttributes);
	}
	
	public void registerAnnotationMapping(AnnotationMappingCondition condition,
			String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes) {
		
		annotations.add(new AnnotationMapping(condition, targetAnnotationFullyQualifiedName,
			targetAnnotationAttributes));
	}
	
	public void applyAnnotations(PropertyGenerationContext sourceProperty, AnnotationApplicator applicator) {
		for (AnnotationMapping annotation : annotations) {
			if (annotation.isApplicableFor(sourceProperty)) {
				applicator.apply(annotation.getTargetAnnotationFullyQualifiedName(),
					annotation.getTargetAnnotationAttributes());
			}
		}
	}
}
