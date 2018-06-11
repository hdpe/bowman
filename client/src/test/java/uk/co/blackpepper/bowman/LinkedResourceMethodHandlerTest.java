package uk.co.blackpepper.bowman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class LinkedResourceMethodHandlerTest {

	private final ResourceContent resourceContent = new ResourceContent();
	private final LinkedResourceMethodHandler handler = createHandler();

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
	public void invokeSetsAndReturnsSameLinkedResource() throws InvocationTargetException, IllegalAccessException {
		final Method getterMethod = findMethod(ResourceContent.class, "getLinkedResource");
		final Method setterMethod = findMethod(ResourceContent.class, "setLinkedResource", String.class);

		handler.invoke(resourceContent, setterMethod, null, new String[]{"X"});

		assertThat(handler.invoke(resourceContent, getterMethod, null, null), equalTo("X"));
	}

	private LinkedResourceMethodHandler createHandler() {
		final JavassistClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
		return new LinkedResourceMethodHandler(new Resource<>(resourceContent),
				new RestOperationsFactory(Configuration.builder().build(), proxyFactory).create(), proxyFactory);
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
