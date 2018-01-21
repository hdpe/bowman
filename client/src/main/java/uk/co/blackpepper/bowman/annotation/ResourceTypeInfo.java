package uk.co.blackpepper.bowman.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.co.blackpepper.bowman.TypeResolver;

/**
 * Class-level annotation to define the narrowing polymorphic deserialization strategy for
 * properties (or property collection items) of this type.
 * <p>
 * Only one of {@link #subtypes} or {@link #typeResolver} should be specified.
 * 
 * @author Ryan Pickett
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceTypeInfo {

	interface NullTypeResolver extends TypeResolver {
		// no members
	}
	
	/**
	 * The subtypes to consider in polymorphic deserialization of properties involving this type. On
	 * deserialization, the final type of the property (or property collection item) will be determined 
	 * by the self link of the resource.
	 * 
	 * @return the subtypes to consider in deserialization
	 */
	Class<?>[] subtypes() default {};
	
	/**
	 * A custom narrowing type resolution strategy to use in polymorphic deserialization of properties
	 * (or property collection items) of this type. 
	 * 
	 * @return the type resolver to use to determine the final type
	 */
	Class<? extends TypeResolver> typeResolver() default NullTypeResolver.class;
}
