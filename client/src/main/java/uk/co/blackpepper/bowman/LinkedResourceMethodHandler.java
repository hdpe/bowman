package uk.co.blackpepper.bowman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

class LinkedResourceMethodHandler implements ConditionalMethodHandler {
	
	private final RestOperations restOperations;

	private final Resource<?> resource;
	
	private final ClientProxyFactory proxyFactory;
	
	private final PropertyValueFactory propertyValueFactory = new DefaultPropertyValueFactory();
	
	private final Map<String, Object> linkedResourceResults = new HashMap<>();
	
	LinkedResourceMethodHandler(Resource<?> resource, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		this.resource = resource;
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}
	
	@Override
	public boolean supports(Method method) {
		return method.isAnnotationPresent(LinkedResource.class);
	}
	
	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args)
		throws InvocationTargetException, IllegalAccessException {
		
		Object linkedResourceResult = linkedResourceResults.get(method.getName());
		
		if (linkedResourceResult == null) {
			linkedResourceResult = resolveLinkedResource(self, method, proceed, args);
			linkedResourceResults.put(method.getName(), linkedResourceResult);
		}
		
		return linkedResourceResult;
	}

	private Object resolveLinkedResource(Object self, Method method, Method proceed, Object[] args)
			throws IllegalAccessException, InvocationTargetException {
		
		URI associationResource = new MethodLinkUriResolver(resource).resolveForMethod(method, args);
		
		if (Collection.class.isAssignableFrom(method.getReturnType())) {
			Class<?> linkedEntityType = (Class<?>) ((ParameterizedType) method.getGenericReturnType())
				.getActualTypeArguments()[0];
			
			return resolveCollectionLinkedResource(associationResource, linkedEntityType, self, proceed);
		}

		return resolveSingleLinkedResource(associationResource, method.getReturnType());
	}

	private <F> F resolveSingleLinkedResource(URI associationResource, Class<F> linkedEntityType) {
		Resource<F> linkedResource = restOperations.getResource(associationResource, linkedEntityType);
		
		if (linkedResource == null) {
			return null;
		}
		
		return proxyFactory.create(linkedResource, restOperations);
	}

	private <F> Collection<F> resolveCollectionLinkedResource(URI associationResource, Class<F> linkedEntityType,
		Object contextEntity, Method originalMethod) throws IllegalAccessException, InvocationTargetException {
		
		Resources<Resource<F>> resources = restOperations.getResources(associationResource, linkedEntityType);
		
		@SuppressWarnings("unchecked")
		Collection<F> collection = (Collection<F>) originalMethod.invoke(contextEntity);
		
		if (collection == null) {
			collection = propertyValueFactory.createCollection(originalMethod.getReturnType());
		}
		else {
			collection.clear();
		}
		
		for (Resource<F> resource : resources) {
			collection.add(proxyFactory.create(resource, restOperations));
		}
		
		return collection;
	}
}
