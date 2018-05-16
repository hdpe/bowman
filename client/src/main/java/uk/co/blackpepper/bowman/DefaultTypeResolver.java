package uk.co.blackpepper.bowman;

import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Links;

import uk.co.blackpepper.bowman.annotation.ResourceTypeInfo;

class DefaultTypeResolver implements TypeResolver {
	
	@Override
	public <T> Class<? extends T> resolveType(Class<T> declaredType, Links resourceLinks, Configuration configuration) {
		
		ResourceTypeInfo info = AnnotationUtils.findAnnotation(declaredType, ResourceTypeInfo.class);
		
		if (info == null) {
			return declaredType;
		}
		
		boolean customTypeResolverIsSpecified = info.typeResolver() != ResourceTypeInfo.NullTypeResolver.class;
		
		if (!(info.subtypes().length > 0 ^ customTypeResolverIsSpecified)) {
			throw new ClientProxyException("one of subtypes or typeResolver must be specified");
		}
		
		TypeResolver delegateTypeResolver = customTypeResolverIsSpecified
			? BeanUtils.instantiateClass(info.typeResolver())
			: new SelfLinkTypeResolver(info.subtypes());
		
		return delegateTypeResolver.resolveType(declaredType, resourceLinks, configuration);
	}
}
