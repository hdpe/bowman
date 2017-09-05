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

/**
 * Factory class for creating {@link Client}s.
 * 
 * <p><code>ClientFactories</code> are created via {@link Configuration#buildClientFactory()}.
 *
 * @author Ryan Pickett
 * 
 */
public class ClientFactory {
	
	private final Configuration configuration;
	
	private final ClientProxyFactory proxyFactory;

	private final RestOperations restOperations;

	ClientFactory(Configuration configuration) {
		this(configuration, new JavassistClientProxyFactory());
	}
	
	ClientFactory(Configuration configuration, ClientProxyFactory proxyFactory) {
		this.configuration = configuration;
		
		this.proxyFactory = proxyFactory;
		this.restOperations = new RestOperationsFactory(configuration, proxyFactory).create();
	}

	/**
	 * Create a <code>Client</code> for the given annotated entity type.
	 * 
	 * @param <T> the entity type of the required client
	 * @param entityType the entity type of the required client
	 * @return the created client
	 */
	public <T> Client<T> create(Class<T> entityType) {
		return new Client<>(entityType, configuration, restOperations, proxyFactory);
	}
}
