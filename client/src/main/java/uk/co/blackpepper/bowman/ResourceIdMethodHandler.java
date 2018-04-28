package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;
import java.net.URI;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.ResourceId;

class ResourceIdMethodHandler implements ConditionalMethodHandler {
	
	private final Resource<?> resource;

	ResourceIdMethodHandler(Resource<?> resource) {
		this.resource = resource;
	}
	
	@Override
	public boolean supports(Method method) {
		return method.isAnnotationPresent(ResourceId.class);
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) {
		Link selfLink = resource.getLink(Link.REL_SELF);
		return selfLink == null ? null : URI.create(selfLink.getHref());
	}
}
