package uk.co.blackpepper.bowman;

import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.EntityModel;

public class AbstractPropertyAwareMethodHandlerTest {
	
	private static class TestMethodHandler extends AbstractPropertyAwareMethodHandler {
		
		TestMethodHandler(EntityModel<?> resource, BeanInfoProvider beanInfoProvider) {
			super(resource.getContent().getClass(), beanInfoProvider);
		}
		
		@Override
		public boolean supports(Method method) {
			return false;
		}

		@Override
		public Object invoke(Object o, Method method, Method method1, Object[] objects) {
			return null;
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
		
		new TestMethodHandler(EntityModel.of(new Object()), (clazz) -> {
			throw new IntrospectionException("x");
		});
	}
}
