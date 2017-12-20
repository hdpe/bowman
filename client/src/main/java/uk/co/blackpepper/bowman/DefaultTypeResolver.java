package uk.co.blackpepper.bowman;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceTypeInfo;

class DefaultTypeResolver implements TypeResolver {
	
	@Override
	public Class<?> resolveType(Class<?> declaredType, Links resourceLinks, Configuration configuration) {
		Link self = resourceLinks.getLink(Link.REL_SELF);
		
		if (self == null) {
			return declaredType;
		}
		
		ResourceTypeInfo info = AnnotationUtils.findAnnotation(declaredType, ResourceTypeInfo.class);
		
		if (info == null) {
			return declaredType;
		}
		
		Class<?>[] subTypes = info.subtypes();
		
		for (Class<?> candidateClass : subTypes) {
			RemoteResource candidateClassInfo = AnnotationUtils.findAnnotation(candidateClass, RemoteResource.class);
			
			if (candidateClassInfo == null) {
				continue;
			}
			
			String resourcePath = candidateClassInfo.value();
			
			String resourceBaseUriString = UriComponentsBuilder.fromUri(configuration.getBaseUri())
				.path(resourcePath)
				.toUriString();
			
			if (self.getHref().startsWith(resourceBaseUriString)) {
				return candidateClass;
			}
		}
		
		return declaredType;
	}
}
