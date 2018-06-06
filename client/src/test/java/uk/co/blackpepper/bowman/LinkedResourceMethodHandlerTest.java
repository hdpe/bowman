package uk.co.blackpepper.bowman;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class LinkedResourceMethodHandlerTest {

	private final ResourceContent resourceContent = new ResourceContent();
	private final LinkedResourceMethodHandler handler = createHandler();

	@Test
	public void supportsLinkedResourceSetterIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "setSomething", String.class)), is(true));
	}

	@Test
	public void supportsLinkedResourceGetterIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "getSomething")), is(true));
	}

	@Test
	public void supportsNonAnnotatedSetterIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "setSomethingElse", String.class)), is(false));
	}

	@Test
	public void supportsNonAnnotatedGetterIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "getSomethingElse")), is(false));
	}

	@Test
	public void invokeSetsAndReturnsSameLinkedResource() throws InvocationTargetException, IllegalAccessException {
		final Method setterMethod = findMethod(ResourceContent.class, "setSomething", String.class);
		assertThat(setterMethod, is(notNullValue()));

		final String setterValue = "STRING";
		final Object setterResult = handler.invoke(resourceContent, setterMethod, null, new String[]{setterValue});

		assertThat(setterValue, equalTo(setterResult));

		final Method getterMethod = findMethod(ResourceContent.class, "getSomething");
		assertThat(getterMethod, is(notNullValue()));

		final Object getterResult = handler.invoke(resourceContent, getterMethod, null, null);

		assertThat(getterResult, equalTo(setterResult));

		assertThat(setterValue, equalTo(resourceContent.getSomething()));
	}

	private LinkedResourceMethodHandler createHandler() {
		final JavassistClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
		return new LinkedResourceMethodHandler(new Resource<>(resourceContent),
				new RestOperationsFactory(Configuration.builder().build(), proxyFactory).create(), proxyFactory);
	}

	@SuppressWarnings("ALL")
	private static class ResourceContent {
		private String something;

		@LinkedResource
		public void setSomething(String value) {
			something = value;
		}

		@LinkedResource
		public String getSomething() {
			return something;
		}

		public void setSomethingElse(String value) {
			// no-op
		}

		public String getSomethingElse() {
			return null;
		}
	}
}
