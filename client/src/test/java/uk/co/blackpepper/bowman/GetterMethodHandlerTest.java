package uk.co.blackpepper.bowman;

import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Resource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.util.ReflectionUtils.findMethod;

public class GetterMethodHandlerTest {
	
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
	}
	
	private GetterMethodHandler handler;
	
	@Before
	public void setUp() {
		handler = new GetterMethodHandler(new Resource<>(new ResourceContent()));
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
}
