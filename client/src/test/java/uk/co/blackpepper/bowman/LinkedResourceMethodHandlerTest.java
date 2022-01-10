package uk.co.blackpepper.bowman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.EntityModel;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class LinkedResourceMethodHandlerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ResourceContent resourceContent;

	private LinkedResourceMethodHandler handler;
	
	private MethodLinkAttributesResolver methodLinkAttributesResolver;
	
	private MethodLinkUriResolver methodLinkUriResolver;
	
	private EntityModel<ResourceContent> resource;
	
	private PropertyValueFactory propertyValueFactory;
	
	@Before
	public void setUp() {
		propertyValueFactory = mock(PropertyValueFactory.class);
		methodLinkAttributesResolver = mock(MethodLinkAttributesResolver.class);
		methodLinkUriResolver = mock(MethodLinkUriResolver.class);
		
		JavassistClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
		
		resourceContent = new ResourceContent();
		resource = EntityModel.of(resourceContent);
		
		handler = new LinkedResourceMethodHandler(resource, mock(RestOperations.class), proxyFactory,
			propertyValueFactory, methodLinkAttributesResolver, methodLinkUriResolver);
		
		when(methodLinkAttributesResolver.resolveForMethod(any())).thenReturn(newRequiredLinkAttributes());
	}
	
	@Test
	public void supportsAnyAnnotatedMethodIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "anyAnnotatedMethod")), is(true));
	}

	@Test
	public void supportsSetterForAnnotatedGetterIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "setLinkedResource", String.class)), is(true));
	}

	@Test
	public void supportsNonAnnotatedMethodIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "nonAnnotatedMethod")), is(false));
	}
	
	@Test
	public void invokeResolvesLinkFromLinkAttributes() throws InvocationTargetException, IllegalAccessException {
		final Method getterMethod = findMethod(ResourceContent.class, "getLinkedResource");
		
		when(methodLinkAttributesResolver.resolveForMethod(getterMethod)).thenReturn(new MethodLinkAttributes("x",
			true));
		
		handler.invoke(resourceContent, getterMethod, null, new String[0]);
		
		verify(methodLinkUriResolver).resolveForMethod(eq(resource), eq("x"), argThat(is(emptyArray())));
	}
	
	@Test
	public void invokeWhenNoLinkAndRequiredLinkThrowsException() throws InvocationTargetException,
		IllegalAccessException {
		
		final Method getterMethod = findMethod(ResourceContent.class, "getLinkedResource");
		
		when(methodLinkAttributesResolver.resolveForMethod(any())).thenReturn(newRequiredLinkAttributes());
		when(methodLinkUriResolver.resolveForMethod(any(), any(), any())).thenThrow(new NoSuchLinkException("x"));
		
		thrown.expect(NoSuchLinkException.class);
		thrown.expect(hasProperty("linkName", is("x")));
		
		handler.invoke(resourceContent, getterMethod, null, new String[0]);
	}
	
	@Test
	public void invokeWhenNoLinkAndOptionalLinkAndSingleValueReturnsNull() throws InvocationTargetException,
		IllegalAccessException {
		
		final Method getterMethod = findMethod(ResourceContent.class, "getOptionalLinkedResource");
		
		when(methodLinkAttributesResolver.resolveForMethod(any())).thenReturn(newOptionalLinkAttributes());
		when(methodLinkUriResolver.resolveForMethod(any(), any(), any())).thenThrow(new NoSuchLinkException("x"));
		
		Object result = handler.invoke(resourceContent, getterMethod, null, new String[0]);
		
		assertThat(result, is(nullValue()));
	}
	
	@Test
	public void invokeWhenNoLinkAndOptionalLinkAndCollectionValueReturnsEmpty() throws InvocationTargetException,
		IllegalAccessException {
		
		final Method getterMethod = findMethod(ResourceContent.class, "getOptionalLinkedResources");
		
		when(methodLinkAttributesResolver.resolveForMethod(any())).thenReturn(newOptionalLinkAttributes());
		when(methodLinkUriResolver.resolveForMethod(any(), any(), any())).thenThrow(new NoSuchLinkException("x"));
		
		SortedSet<String> set = new TreeSet<>();
		when(propertyValueFactory.createCollection(SortedSet.class)).thenReturn(set);
		
		Object result = handler.invoke(resourceContent, getterMethod, null, new String[0]);
		
		assertThat(result, is(sameInstance(set)));
		assertThat((Collection<?>) result, is(empty()));
	}

	@Test
	public void invokeSetsAndReturnsSameLinkedResource() throws InvocationTargetException, IllegalAccessException {
		final Method getterMethod = findMethod(ResourceContent.class, "getLinkedResource");
		final Method setterMethod = findMethod(ResourceContent.class, "setLinkedResource", String.class);

		handler.invoke(resourceContent, setterMethod, null, new String[]{"X"});

		assertThat(handler.invoke(resourceContent, getterMethod, null, null), equalTo("X"));
	}

	@Test
	public void invokeSetsAndReturnsNull() throws InvocationTargetException, IllegalAccessException {
		final Method getterMethod = findMethod(ResourceContent.class, "getLinkedResource");
		final Method setterMethod = findMethod(ResourceContent.class, "setLinkedResource", String.class);

		handler.invoke(resourceContent, setterMethod, null, new String[]{null});

		assertThat(handler.invoke(resourceContent, getterMethod, null, null), is(nullValue()));
	}
	
	private static MethodLinkAttributes newRequiredLinkAttributes() {
		return new MethodLinkAttributes("_linkName", false);
	}
	
	private static MethodLinkAttributes newOptionalLinkAttributes() {
		return new MethodLinkAttributes("_linkName", true);
	}

	@SuppressWarnings("unused")
	private static class ResourceContent {
		@LinkedResource
		public void anyAnnotatedMethod() {
			// no-op
		}

		@LinkedResource
		public String getLinkedResource() {
			return null;
		}
		
		@LinkedResource(optionalLink = true)
		public String getOptionalLinkedResource() {
			return null;
		}
		
		@LinkedResource(optionalLink = true)
		public SortedSet<String> getOptionalLinkedResources() {
			return null;
		}

		public void setLinkedResource(String value) {
			// no-op
		}

		public String nonAnnotatedMethod() {
			return null;
		}
	}
}
