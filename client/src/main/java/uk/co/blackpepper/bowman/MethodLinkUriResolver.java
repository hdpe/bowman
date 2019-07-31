package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static uk.co.blackpepper.bowman.HalSupport.toLinkName;

class MethodLinkUriResolver {
	
	URI resolveForMethod(Resource<?> resource, Method method, Object[] args) {
		String linkName = getLinkName(method);
		Link link = resource.getLink(linkName);
		
		if (link == null) {
			throw new NoSuchLinkException(linkName);
		}
		
		return URI.create(link.expand(args).getHref());
	}
	
	private static String getLinkName(Method method) {
		String rel = method.getAnnotation(LinkedResource.class).rel();
		
		if ("".equals(rel)) {
			rel = toLinkName(method.getName());
		}
		
		return rel;
	}
}
