package uk.co.blackpepper.bowman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class LinkedResourceMethodHandlerTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	private ResourceContent resourceContent;

	private LinkedResourceMethodHandler handler;
	
	private MethodLinkUriResolver methodLinkUriResolver;
	
	private Resource<ResourceContent> resource;
	
	@Before
	public void setUp() {
		methodLinkUriResolver = mock(MethodLinkUriResolver.class);
		
		JavassistClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
		
		resourceContent = new ResourceContent();
		resource = new Resource<>(resourceContent);
		
		handler = new LinkedResourceMethodHandler(resource,
			new RestOperationsFactory(Configuration.builder().build(), proxyFactory).create(), proxyFactory,
			mock(PropertyValueFactory.class), methodLinkUriResolver);
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
	public void invokeWhenNoLinkThrowsException() throws InvocationTargetException, IllegalAccessException {
		final Method getterMethod = findMethod(ResourceContent.class, "getLinkedResource");
		
		when(methodLinkUriResolver.resolveForMethod(eq(resource), eq(getterMethod), argThat(is(emptyArray()))))
			.thenThrow(new NoSuchLinkException("x"));
		
		thrown.expect(NoSuchLinkException.class);
		thrown.expect(hasProperty("linkName", is("x")));
		
		handler.invoke(resourceContent, getterMethod, null, new String[0]);
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

		public void setLinkedResource(String value) {
			// no-op
		}

		public String nonAnnotatedMethod() {
			return null;
		}
	}
}
