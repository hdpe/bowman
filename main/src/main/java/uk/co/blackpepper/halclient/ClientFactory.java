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
package uk.co.blackpepper.halclient;

public class ClientFactory {
	
	private final Configuration configuration;

	private final RestOperations restOperations;

	ClientFactory(Configuration configuration) {
		this.configuration = configuration;
		this.restOperations = new RestOperationsFactory(configuration).create();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<>(entityType, configuration, restOperations);
	}
}
