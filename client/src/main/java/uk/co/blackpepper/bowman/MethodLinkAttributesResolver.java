package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static uk.co.blackpepper.bowman.HalSupport.toLinkName;

class MethodLinkAttributesResolver {
	
	MethodLinkAttributes resolveForMethod(Method method) {
		LinkedResource annotation = method.getAnnotation(LinkedResource.class);
		
		String rel = annotation.rel();
		
		if ("".equals(rel)) {
			rel = toLinkName(method.getName());
		}
		
		return new MethodLinkAttributes(rel);
	}
}
