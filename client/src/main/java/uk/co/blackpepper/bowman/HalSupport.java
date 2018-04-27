/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.bowman;

import java.beans.Introspector;

final class HalSupport {
	
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
			return methodName;
		}
		
		return Introspector.decapitalize(methodName);
	}
}
