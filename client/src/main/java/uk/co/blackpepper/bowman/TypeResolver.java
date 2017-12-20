package uk.co.blackpepper.bowman;

import org.springframework.hateoas.Links;

public interface TypeResolver {

	Class<?> resolveType(Class<?> declaredType, Links resourceLinks, Configuration configuration);
}
