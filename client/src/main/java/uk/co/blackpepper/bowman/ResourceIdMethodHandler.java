package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Optional;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;

import uk.co.blackpepper.bowman.annotation.ResourceId;

class ResourceIdMethodHandler implements ConditionalMethodHandler {
	
	private final EntityModel<?> resource;

	ResourceIdMethodHandler(EntityModel<?> resource) {
		this.resource = resource;
	}
	
	@Override
	public boolean supports(Method method) {
		return method.isAnnotationPresent(ResourceId.class);
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args) {
		Optional<Link> selfLink = resource.getLink(IanaLinkRelations.SELF);
		return selfLink.map(link -> URI.create(link.getHref())).orElse(null);
	}
}
