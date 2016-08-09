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
		
		private String sourceAnnotationFullyQualifiedName;
		
		private String targetAnnotationFullyQualifiedName;
		
		private Map<String, Object> targetAnnotationAttributes = new LinkedHashMap<String, Object>();
		
		private AnnotationTargetType targetType;

		AnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes,
			AnnotationTargetType targetType) {
			this.sourceAnnotationFullyQualifiedName = sourceAnnotationFullyQualifiedName;
			this.targetAnnotationFullyQualifiedName = targetAnnotationFullyQualifiedName;
			this.targetAnnotationAttributes = targetAnnotationAttributes;
			this.targetType = targetType;
		}

		public boolean isApplicableFor(String sourceAnnotationFullyQualifiedName) {
			return this.sourceAnnotationFullyQualifiedName.equals(sourceAnnotationFullyQualifiedName);
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
	
	private List<AnnotationMapping> annotations = new ArrayList<AnnotationMapping>();
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName) {
		registerAnnotationMapping(sourceAnnotationFullyQualifiedName, targetAnnotationFullyQualifiedName,
			Collections.<String, Object>emptyMap());
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
		
		annotations.add(new AnnotationMapping(sourceAnnotationFullyQualifiedName,
			targetAnnotationFullyQualifiedName,
			targetAnnotationAttributes, targetType));
	}
	
	public void applyAnnotations(String sourceAnnotationFullyQualifiedName, AnnotationApplicator applicator) {
		for (AnnotationMapping annotation : annotations) {
			if (annotation.isApplicableFor(sourceAnnotationFullyQualifiedName)) {
				applicator.apply(annotation.getTargetAnnotationFullyQualifiedName(),
					annotation.getTargetAnnotationAttributes(), annotation.getTargetType());
			}
		}
	}
}
