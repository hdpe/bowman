package uk.co.blackpepper.sdrclient.gen.model;

import java.util.Collection;

public interface Field {

	String getName();

	String getQualifiedTypeNameWithGenerics();
	
	Collection<Annotation> getAnnotations();
}
