package uk.co.blackpepper.sdrclient.gen;

import java.util.Map;

public interface AnnotationApplicator {

	void apply(String fullyQualifiedAnnotationName, Map<String, Object> annotationAttributes);
}
