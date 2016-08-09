package uk.co.blackpepper.sdrclient.gen;

import java.util.Map;

import uk.co.blackpepper.sdrclient.gen.AnnotationRegistry.AnnotationTargetType;

public interface AnnotationApplicator {

	void apply(String fullyQualifiedAnnotationName, Map<String, Object> annotationAttributes,
		AnnotationTargetType targetType);
}
