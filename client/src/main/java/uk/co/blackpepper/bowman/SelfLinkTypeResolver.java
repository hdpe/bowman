package uk.co.blackpepper.bowman;

import java.net.URI;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

class SelfLinkTypeResolver implements TypeResolver {

	private Class<?>[] subtypes;
	
	SelfLinkTypeResolver(Class<?>[] subtypes) {
		this.subtypes = subtypes;
	}
	
	@Override
	public <T> Class<? extends T> resolveType(Class<T> declaredType, Links resourceLinks, Configuration configuration) {

		Link self = resourceLinks.getLink(Link.REL_SELF);
		
		if (self == null) {
			return declaredType;
		}
		
		for (Class<?> candidateClass : subtypes) {
			RemoteResource candidateClassInfo = AnnotationUtils.findAnnotation(candidateClass, RemoteResource.class);
			
			if (candidateClassInfo == null) {
				throw new ClientProxyException(String.format("%s is not annotated with @%s", candidateClass.getName(),
					RemoteResource.class.getSimpleName()));
			}
			
			String resourcePath = candidateClassInfo.value();
			
			String resourceBaseUriString = UriComponentsBuilder.fromUri(configuration.getBaseUri())
				.path(resourcePath)
				.toUriString();
			
			String selfLinkUriString = toAbsoluteUriString(self.getHref(), configuration.getBaseUri());
			
			if (selfLinkUriString.startsWith(resourceBaseUriString + "/")) {
				if (!declaredType.isAssignableFrom(candidateClass)) {
					throw new ClientProxyException(String.format("%s is not a subtype of %s", candidateClass.getName(),
						declaredType.getName()));
				}
				
				@SuppressWarnings("unchecked")
				Class<? extends T> result = (Class<? extends T>) candidateClass;
				
				return result;
			}
		}
		
		return declaredType;
	}
	
	private static String toAbsoluteUriString(String uri, URI baseUri) {
		if (UriComponentsBuilder.fromUriString(uri).build().getHost() != null) {
			return uri;
		}
		
		return UriComponentsBuilder.fromUri(baseUri)
			.path(uri)
			.toUriString();
	}
}
