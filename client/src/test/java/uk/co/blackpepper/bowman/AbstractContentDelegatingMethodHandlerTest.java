package uk.co.blackpepper.bowman;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.Resource;

public class AbstractContentDelegatingMethodHandlerTest {
	
	private static class TestMethodHandler extends AbstractContentDelegatingMethodHandler {
		
		TestMethodHandler(Resource<?> resource, BeanInfoProvider beanInfoProvider) {
			super(resource, beanInfoProvider);
		}
		
		@Override
		public boolean supports(Method method) {
			return false;
		}
	}
	
	private ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
	}
	
	@Test
	public void constructorOnIntrospectionExceptionThrowsException() {
		thrown.expect(ClientProxyException.class);
		
		new TestMethodHandler(new Resource<>(new Object()), (clazz) -> {
			throw new IntrospectionException("x");
		});
	}
}
