package uk.co.blackpepper.sdrclient.gen.model;

import java.util.Map;

public interface Annotation {

	String getFullyQualifiedName();

	Map<String, Object> values();
}
