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

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

class LinkedResourceMethodHandler extends AbstractPropertyAwareMethodHandler {

	private static final class LinkedResourceResult {
		
		private Object value;
		
		LinkedResourceResult(Object value) {
			this.value = value;
		}
		
		Object getValue() {
			return value;
		}
	}
	
	private final EntityModel resource;

	private final RestOperations restOperations;

	private final ClientProxyFactory proxyFactory;

	private final PropertyValueFactory propertyValueFactory;
	
	private final MethodLinkAttributesResolver methodLinkAttributesResolver;
	
	private final MethodLinkUriResolver methodLinkUriResolver;
	
	private final Map<String, LinkedResourceResult> linkedResourceResults = new HashMap<>();

	LinkedResourceMethodHandler(EntityModel resource, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		this(resource, restOperations, proxyFactory, new DefaultPropertyValueFactory(),
			new MethodLinkAttributesResolver(), new MethodLinkUriResolver());
	}

	LinkedResourceMethodHandler(EntityModel resource, RestOperations restOperations, ClientProxyFactory proxyFactory,
		PropertyValueFactory propertyValueFactory, MethodLinkAttributesResolver methodLinkAttributesResolver,
		MethodLinkUriResolver methodLinkUriResolver) {
		
		super(resource.getContent().getClass());
		
		this.resource = resource;
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
		this.propertyValueFactory = propertyValueFactory;
		this.methodLinkAttributesResolver = methodLinkAttributesResolver;
		this.methodLinkUriResolver = methodLinkUriResolver;
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
		linkedResourceResults.put(getterName, new LinkedResourceResult(args[0]));
	}

	private Method getGetterFromSetter(Method method) {
		return Arrays.stream(getContentBeanInfo().getPropertyDescriptors())
				.filter(pd -> method.equals(pd.getWriteMethod()))
				.collect(Collectors.toList()).get(0).getReadMethod();
	}

	private Object invokeAnnotatedMethod(Object self, Method method, Method proceed, Object[] args)
	throws InvocationTargetException, IllegalAccessException {
		LinkedResourceResult result = linkedResourceResults.get(method.getName());

		if (result == null) {
			Object resultValue = resolveLinkedResource(self, method, proceed, args);

			result = new LinkedResourceResult(resultValue);

			linkedResourceResults.put(method.getName(), result);
		}

		return result.getValue();
	}

	private Object resolveLinkedResource(Object self, Method method, Method proceed, Object[] args)
			throws IllegalAccessException, InvocationTargetException {
		
		boolean isCollection = Collection.class.isAssignableFrom(method.getReturnType());
		
		MethodLinkAttributes attribs = methodLinkAttributesResolver.resolveForMethod(method);
		
		URI associationResource;
		
		try {
			associationResource = methodLinkUriResolver.resolveForMethod(resource, attribs.getLinkName(), args);
		}
		catch (NoSuchLinkException exception) {
			if (attribs.isOptional()) {
				return isCollection ? createCollectionForMethod(method) : null;
			}
			
			throw exception;
		}
		
		if (isCollection) {
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
		EntityModel<F> linkedResource = restOperations.getResource(associationResource, linkedEntityType);

		if (linkedResource == null) {
			return null;
		}

		return proxyFactory.create(linkedResource, restOperations);
	}
	
	private <F> Collection<F> resolveCollectionLinkedResource(CollectionModel<EntityModel<F>> resources,
		Object contextEntity,
		Method originalMethod) throws IllegalAccessException, InvocationTargetException {

		@SuppressWarnings("unchecked")
		Collection<F> collection = (Collection<F>) originalMethod.invoke(contextEntity);

		if (collection == null) {
			collection = createCollectionForMethod(originalMethod);
		}
		else {
			collection.clear();
		}
		return updateCollectionWithLinkedResources(collection, resources);
	}
	
	private <F> Collection<F> resolveCollectionLinkedResource(CollectionModel<EntityModel<F>> resources,
		Method method) {
		return updateCollectionWithLinkedResources(
			createCollectionForMethod(method), resources);
	}

	private <F> Collection<F> updateCollectionWithLinkedResources(Collection<F> collection,
		CollectionModel<EntityModel<F>> resources) {
		for (EntityModel<F> fResource : resources) {
			collection.add(proxyFactory.create(fResource, restOperations));
		}

		return collection;
	}

	private <F> CollectionModel<EntityModel<F>> getLinkedResources(URI associationResource, Class<F> linkedEntityType) {
		return restOperations.getResources(associationResource, linkedEntityType);
	}
	
	private <F> Collection<F> createCollectionForMethod(Method method) {
		return propertyValueFactory.createCollection(method.getReturnType());
	}
}
