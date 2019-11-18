package uk.co.blackpepper.bowman;

import java.net.URI;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

class MethodLinkUriResolver {
	
	URI resolveForMethod(EntityModel<?> resource, String linkName, Object[] args) {
		Link link = resource.getLink(linkName).orElseThrow(() -> new NoSuchLinkException(linkName));
		return URI.create(link.expand(args).getHref());
	}
}
