package uk.co.blackpepper.sdrclient.gen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AnnotationRegistry {

	private static class AnnotationMapping {
		
		private String sourceAnnotationFullyQualifiedName;
		
		private String targetAnnotationFullyQualifiedName;
		
		private Map<String, Object> targetAnnotationAttributes = new LinkedHashMap<String, Object>();

		AnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName, Map<String, Object> targetAnnotationAttributes) {
			this.sourceAnnotationFullyQualifiedName = sourceAnnotationFullyQualifiedName;
			this.targetAnnotationFullyQualifiedName = targetAnnotationFullyQualifiedName;
			this.targetAnnotationAttributes = targetAnnotationAttributes;
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
	}
	
	private List<AnnotationMapping> annotations = new ArrayList<AnnotationMapping>();
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName) {
		registerAnnotationMapping(sourceAnnotationFullyQualifiedName, targetAnnotationFullyQualifiedName,
				Collections.<String, Object>emptyMap());
	}
	
	public void registerAnnotationMapping(String sourceAnnotationFullyQualifiedName,
			String targetAnnotationFullyQualifiedName,
			Map<String, Object> targetAnnotationAttributes) {
		
		annotations.add(new AnnotationMapping(sourceAnnotationFullyQualifiedName,
			targetAnnotationFullyQualifiedName,
			targetAnnotationAttributes));
	}
	
	public void applyAnnotations(String sourceAnnotationFullyQualifiedName, AnnotationApplicator applicator) {
		for (AnnotationMapping annotation : annotations) {
			if (annotation.isApplicableFor(sourceAnnotationFullyQualifiedName)) {
				applicator.apply(annotation.getTargetAnnotationFullyQualifiedName(),
						annotation.getTargetAnnotationAttributes());
			}
		}
	}
}
