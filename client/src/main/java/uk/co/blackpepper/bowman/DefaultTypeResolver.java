package uk.co.blackpepper.bowman;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Links;
import org.springframework.util.Assert;

import uk.co.blackpepper.bowman.annotation.ResourceTypeInfo;

class DefaultTypeResolver implements TypeResolver {
	
	@Override
	public Class<?> resolveType(Class<?> declaredType, Links resourceLinks, Configuration configuration) {
		
		ResourceTypeInfo info = AnnotationUtils.findAnnotation(declaredType, ResourceTypeInfo.class);
		
		if (info == null) {
			return declaredType;
		}
		
		boolean customTypeResolverIsSpecified = info.typeResolver() != ResourceTypeInfo.NullTypeResolver.class;
		
		Assert.state(info.subtypes().length > 0 ^ customTypeResolverIsSpecified,
			"one of subtypes or typeResolver must be specified");
		
		TypeResolver delegateTypeResolver = customTypeResolverIsSpecified
			? BeanUtils.instantiate(info.typeResolver())
			: new SelfLinkTypeResolver(info.subtypes());
		
		return delegateTypeResolver.resolveType(declaredType, resourceLinks, configuration);
	}
}
