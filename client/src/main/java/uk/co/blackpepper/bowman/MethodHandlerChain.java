package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;

@JsonIgnoreType
class MethodHandlerChain implements MethodHandler, MethodFilter {
	
	private List<ConditionalMethodHandler> delegateHandlers;
	
	MethodHandlerChain(List<ConditionalMethodHandler> delegateHandlers) {
		this.delegateHandlers = new ArrayList<>(delegateHandlers);
	}
	
	@Override
	public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		for (ConditionalMethodHandler handler : delegateHandlers) {
			if (handler.supports(thisMethod)) {
				return handler.invoke(self, thisMethod, proceed, args);
			}
		}
		
		throw new IllegalStateException(String.format("invoke called for non-handled method %s", thisMethod));
	}
	
	@Override
	public boolean isHandled(Method method) {
		return delegateHandlers.stream().anyMatch(h -> h.supports(method));
	}
}
