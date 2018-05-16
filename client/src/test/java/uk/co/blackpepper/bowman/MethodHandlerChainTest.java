package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public class MethodHandlerChainTest {
	
	private interface ResourceContent {
		
		Object method1();
		
		Object method2();
	}
	
	private ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
	}
	
	// CHECKSTYLE:OFF
	
	@Test
	public void invokeInvokesFirstSupportedDelegate() throws Throwable {
		
		// CHECKSTYLE:ON
		
		Method method = ResourceContent.class.getMethod("method1");
		
		ConditionalMethodHandler expectedHandler = newHandlerSupportedFor(method);
		
		MethodHandlerChain chain = new MethodHandlerChain(asList(
			newHandlerUnsupportedFor(method),
			expectedHandler,
			newHandlerSupportedFor(method)
		));
		
		Method proceedMethod = ResourceContent.class.getMethod("method2");
		Object self = new Object();
		Object arg = new Object();
		
		chain.invoke(self, method, proceedMethod, new Object[] {arg});
		
		verify(expectedHandler).invoke(eq(self), eq(method), eq(proceedMethod), argThat(arrayContaining(arg)));
	}
	
	// CHECKSTYLE:OFF
	
	@Test
	public void invokeWithNoSupportedDelegateThrowsException() throws Throwable {
		
		// CHECKSTYLE:ON
		
		Method method = ResourceContent.class.getMethod("method1");
		
		MethodHandlerChain chain = new MethodHandlerChain(emptyList());
		
		thrown.expect(IllegalStateException.class);
		
		chain.invoke(new Object(), method, null, new Object[0]);
	}
	
	@Test
	public void isHandledWithNoDelegateSupportsIsFalse() throws Exception {
		Method method = ResourceContent.class.getMethod("method1");
		
		MethodHandlerChain chain = new MethodHandlerChain(singletonList(newHandlerUnsupportedFor(method)));
		
		assertThat(chain.isHandled(method), is(false));
	}
	
	@Test
	public void isHandledWithDelegateSupportsIsTrue() throws Exception {
		Method method = ResourceContent.class.getMethod("method1");
		
		MethodHandlerChain chain = new MethodHandlerChain(singletonList(newHandlerSupportedFor(method)));
		
		assertThat(chain.isHandled(method), is(true));
	}
	
	private ConditionalMethodHandler newHandlerSupportedFor(Method method) {
		ConditionalMethodHandler supported1 = mock(ConditionalMethodHandler.class);
		when(supported1.supports(method)).thenReturn(true);
		return supported1;
	}
	
	private ConditionalMethodHandler newHandlerUnsupportedFor(Method method) {
		ConditionalMethodHandler notSupported = mock(ConditionalMethodHandler.class);
		when(notSupported.supports(method)).thenReturn(false);
		return notSupported;
	}
}
