package uk.co.blackpepper.bowman;

import java.net.URI;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

class MethodLinkUriResolver {
	
	URI resolveForMethod(Resource<?> resource, String linkName, Object[] args) {
		Link link = resource.getLink(linkName);
		
		if (link == null) {
			throw new NoSuchLinkException(linkName);
		}
		
		return URI.create(link.expand(args).getHref());
	}
}
