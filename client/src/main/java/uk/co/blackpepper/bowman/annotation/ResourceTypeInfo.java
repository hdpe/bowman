package uk.co.blackpepper.bowman.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import uk.co.blackpepper.bowman.TypeResolver;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ResourceTypeInfo {

	interface NullTypeResolver extends TypeResolver {
		// no members
	}
	
	Class<?>[] subtypes() default {};
	
	Class<? extends TypeResolver> typeResolver() default NullTypeResolver.class;
}
