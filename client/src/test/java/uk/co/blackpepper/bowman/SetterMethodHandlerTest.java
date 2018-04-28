package uk.co.blackpepper.bowman;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Resource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class SetterMethodHandlerTest {
	
	private static class ResourceContent {
		
		public void setThing(String value) {
		}
		
		public void setThing2(String value, int param) {
		}
		
		public String setThing3(String value) {
			return null;
		}
		
		public String notApplicable() {
			return null;
		}
	}
	
	private SetterMethodHandler handler;
	
	@Before
	public void setUp() {
		handler = new SetterMethodHandler(new Resource<>(new ResourceContent()));
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
