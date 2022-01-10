package uk.co.blackpepper.bowman;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.EntityModel;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class SimplePropertyMethodHandlerTest {

	@SuppressWarnings("unused")
	private static class ResourceContent {
		
		public String getThing() {
			return null;
		}
		
		public void setThing(int i) {
		}
		
		public boolean isThing2() {
			return false;
		}
		
		public String getThing3(int param) {
			return null;
		}
		
		public void getThing4() {
		}
		
		public String notApplicable() {
			return null;
		}

		public void setThing(String value) {
		}

		public void setThing2(String value, int param) {
		}

		public String setThing3(String value) {
			return null;
		}
	}
	
	private SimplePropertyMethodHandler handler;
	
	@Before
	public void setUp() {
		handler = new SimplePropertyMethodHandler<>(EntityModel.of(new ResourceContent()));
	}
	
	@Test
	public void supportsWithGetterIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "getThing")), is(true));
	}
	
	@Test
	public void supportsWithIsGetterIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "isThing2")), is(true));
	}
	
	@Test
	public void supportsWithGetMethodWithParameterIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "getThing3", int.class)), is(false));
	}
	
	@Test
	public void supportsWithGetMethodWithVoidReturnTypeIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "getThing4")), is(false));
	}
	
	@Test
	public void supportsWithNonGetMethodIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "notApplicable")), is(false));
	}

	@Test
	public void supportsWithSetterIsTrue() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "setThing", String.class)), is(true));
	}

	@Test
	public void supportsWithSetMethodWithParameterIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "setThing2", String.class, int.class)),
				is(false));
	}

	@Test
	public void supportsWithSetMethodWithReturnTypeIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "setThing3", String.class)), is(false));
	}

	@Test
	public void supportsWithNonSetMethodIsFalse() {
		assertThat(handler.supports(findMethod(ResourceContent.class, "notApplicable")), is(false));
	}
}
