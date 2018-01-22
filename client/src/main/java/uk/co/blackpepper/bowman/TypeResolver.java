package uk.co.blackpepper.bowman;

import org.springframework.hateoas.Links;

/**
 * Narrowing type resolution strategy.
 * 
 * @author Ryan Pickett
 */
public interface TypeResolver {
	
	/**
	 * Get the type to use for a resource. This will be the superclass of proxies generated for
	 * the properties (or property collection items) returning the resource, and so must be a 
	 * subtype of the property's (or property collection item's) declared type.
	 *
	 * @param declaredType
	 * 	declared type of the property or property collection item
	 * @param resourceLinks
	 * 	links of the resource
	 * @param configuration
	 * 	client factory configuration
	 * @return
	 * 	the type to use for the superclass of the generated proxy for a resource
	 */
	<T> Class<? extends T> resolveType(Class<T> declaredType, Links resourceLinks, Configuration configuration);
}
