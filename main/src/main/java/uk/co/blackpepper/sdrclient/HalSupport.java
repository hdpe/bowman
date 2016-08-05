package uk.co.blackpepper.sdrclient;

import java.beans.Introspector;

public final class HalSupport {
	
	private HalSupport() {
	}
	
	public static String toLinkName(String methodName) {
		if (methodName.startsWith("is")) {
			methodName = methodName.substring(2);
		}
		else if (methodName.startsWith("get")) {
			methodName = methodName.substring(3);
		}
		else {
			throw new IllegalArgumentException("not a bean property getter: " + methodName);
		}
		
		return Introspector.decapitalize(methodName);
	}
}
