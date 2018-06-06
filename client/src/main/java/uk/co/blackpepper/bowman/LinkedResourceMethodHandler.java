package uk.co.blackpepper.bowman;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

class LinkedResourceMethodHandler extends AbstractContentDelegatingMethodHandler {

	private final RestOperations restOperations;

	private final ClientProxyFactory proxyFactory;

	private final PropertyValueFactory propertyValueFactory = new DefaultPropertyValueFactory();

	private final Map<String, Object> linkedResourceResults = new HashMap<>();

	LinkedResourceMethodHandler(Resource<?> resource, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		super(resource);
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public boolean supports(Method method) {
		return method.isAnnotationPresent(LinkedResource.class) && (isSetter(method) || isGetter(method));
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args)
	throws InvocationTargetException, IllegalAccessException {
		return isSetter(method)
				? invokeSetter(self, method, proceed, args)
				: invokeGetter(self, method, proceed, args);
	}

	private Object invokeGetter(Object self, Method method, Method proceed, Object[] args)
	throws InvocationTargetException, IllegalAccessException {
		if (!linkedResourceResults.containsKey(method.getName())) {
			linkedResourceResults.put(method.getName(), resolveLinkedResource(self, method, proceed, args));
		}
		return linkedResourceResults.get(method.getName());
	}

	private Object invokeSetter(Object self, Method method, Method proceed, Object[] args)
	throws InvocationTargetException, IllegalAccessException {
		super.invoke(self, method, proceed, args);
		final String getterName = getGetterFromSetter(method).getName();
		linkedResourceResults.put(getterName, args[0]);
		return linkedResourceResults.get(getterName);
	}

	private boolean isSetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.map(PropertyDescriptor::getWriteMethod)
				.anyMatch(method::equals);
	}

	private boolean isGetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.map(PropertyDescriptor::getReadMethod)
				.anyMatch(method::equals);
	}

	private Method getGetterFromSetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.filter(pd -> method.equals(pd.getWriteMethod()))
				.collect(Collectors.toList()).get(0).getReadMethod();
	}

	private Object resolveLinkedResource(Object self, Method method, Method proceed, Object[] args)
			throws IllegalAccessException, InvocationTargetException {
		
		URI associationResource = new MethodLinkUriResolver(getResource()).resolveForMethod(method, args);
		
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
