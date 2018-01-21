package uk.co.blackpepper.bowman;

import org.springframework.hateoas.Links;

public interface TypeResolver {
	
	<T> Class<? extends T> resolveType(Class<T> declaredType, Links resourceLinks, Configuration configuration);
}
