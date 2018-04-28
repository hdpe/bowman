package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;

interface ConditionalMethodHandler extends MethodHandler {
	
	boolean supports(Method method);
}
