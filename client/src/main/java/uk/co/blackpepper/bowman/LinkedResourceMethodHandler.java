package uk.co.blackpepper.bowman;

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

class LinkedResourceMethodHandler extends AbstractPropertyAwareMethodHandler {

	private final Resource resource;

	private final RestOperations restOperations;

	private final ClientProxyFactory proxyFactory;

	private final PropertyValueFactory propertyValueFactory = new DefaultPropertyValueFactory();

	private final Map<String, Object> linkedResourceResults = new HashMap<>();

	LinkedResourceMethodHandler(Resource resource, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		super(resource.getContent().getClass());
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
		this.resource = resource;
	}

	@Override
	public boolean supports(Method method) {
		if (isSetter(method)) {
			Method getter = getGetterFromSetter(method);
			if (getter != null) {
				return getter.isAnnotationPresent(LinkedResource.class);
			}
		}

		return method.isAnnotationPresent(LinkedResource.class);
	}

	@Override
	public Object invoke(Object self, Method method, Method proceed, Object[] args)
	throws InvocationTargetException, IllegalAccessException {
		if (isSetter(method)) {
			invokeSetterMethod(method, args);
			return null;
		}
		else {
			return invokeAnnotatedMethod(self, method, proceed, args);
		}
	}

	private void invokeSetterMethod(Method method, Object[] args) {
		final String getterName = getGetterFromSetter(method).getName();
		linkedResourceResults.put(getterName, args[0]);
	}

	private Method getGetterFromSetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.filter(pd -> method.equals(pd.getWriteMethod()))
				.collect(Collectors.toList()).get(0).getReadMethod();
	}

	private Object invokeAnnotatedMethod(Object self, Method method, Method proceed, Object[] args)
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

			if (proceed == null) {
				return resolveCollectionLinkedResource(getLinkedResources(associationResource,
						linkedEntityType), method);
			}
			else {
				return resolveCollectionLinkedResource(getLinkedResources(associationResource, linkedEntityType),
						self, proceed);
			}
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

	private <F> Collection<F> resolveCollectionLinkedResource(Resources<Resource<F>> resources, Object contextEntity,
		Method originalMethod) throws IllegalAccessException, InvocationTargetException {

		@SuppressWarnings("unchecked")
		Collection<F> collection = (Collection<F>) originalMethod.invoke(contextEntity);

		if (collection == null) {
			collection = propertyValueFactory.createCollection(originalMethod.getReturnType());
		}
		else {
			collection.clear();
		}
		return updateCollectionWithLinkedResources(collection, resources);
	}

	private <F> Collection<F> resolveCollectionLinkedResource(Resources<Resource<F>> resources, Method method) {
		return updateCollectionWithLinkedResources(
				propertyValueFactory.createCollection(method.getReturnType()), resources);
	}

	private <F> Collection<F> updateCollectionWithLinkedResources(Collection<F> collection,
		Resources<Resource<F>> resources) {
		for (Resource<F> fResource : resources) {
			collection.add(proxyFactory.create(fResource, restOperations));
		}

		return collection;
	}

	private <F> Resources<Resource<F>> getLinkedResources(URI associationResource, Class<F> linkedEntityType) {
		return restOperations.getResources(associationResource, linkedEntityType);
	}
}
