package uk.co.blackpepper.sdrclient.gen.model;

import java.util.Collection;

public interface ClassSource {

	String getName();

	String getPackage();

	Collection<Annotation> getAnnotations();

	Collection<Field> getFields();
}
