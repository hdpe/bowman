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

import java.net.URI;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * Class encapsulating the user-provided configuration of a HAL {@link ClientFactory}.
 * 
 * @author Ryan Pickett
 *
 */
public final class Configuration {
	
	/**
	 * Fluent builder for <code>Configuration</code> instances.
	 *
	 * @author Ryan Pickett
	 * 
	 */
	public static final class Builder {

		private URI baseUri = URI.create("http://localhost:8080");
		
		private RestTemplateConfigurer restTemplateConfigurer;
		
		private ObjectMapperConfigurer objectMapperConfigurer;

		private ClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		
		private Builder() {
		}
		
		/**
		 * Build a <code>Configuration</code> from the provided settings.
		 * 
		 * @return the new Configuration
		 */
		public Configuration build() {
			return new Configuration(this);
		}
		
		/**
		 * @see #setBaseUri(URI) 
		 * @param baseUri the base URI as a string
		 * @return this builder
		 */
		public Builder setBaseUri(String baseUri) {
			this.baseUri = URI.create(baseUri);
			return this;
		}

		/**
		 * Set the base URI of the created configuration.
		 * 
		 * <p>Collection resource paths specified on entity classes with
		 * {@link uk.co.blackpepper.bowman.annotation.RemoteResource} annotations
		 * will be resolved relative to this URI. 
		 * 
		 * @param baseUri the base URI
		 * @return this builder
		 */
		public Builder setBaseUri(URI baseUri) {
			this.baseUri = baseUri;
			return this;
		}
		
		/**
		 * Set the <code>RestTemplateConfigurer</code> for the created configuration. Allows 
		 * further configuration of the Spring <code>RestTemplate</code> used internally. 
		 * 
		 * @param restTemplateConfigurer the <code>RestTemplateConfigurer</code>
		 * @return this builder
		 */
		public Builder setRestTemplateConfigurer(RestTemplateConfigurer restTemplateConfigurer) {
			this.restTemplateConfigurer = restTemplateConfigurer;
			return this;
		}

		/**
		 * Set the <code>ClientHttpRequestFactory</code> for the created configuration.
		 * 
		 * <p><b>N.B.</b> this MUST be an implementation that throws a 
		 * {@link org.springframework.web.client.HttpClientErrorException} when a HTTP 404 is returned
		 * accessing the remote resource, as this fact is used internally to distinguish between
		 * empty results and other client error conditions. The Spring implementations that satisfy this
		 * requirement are {@link org.springframework.http.client.HttpComponentsClientHttpRequestFactory},
		 * and any wrapping implementations delegating to this.
		 * 
		 * @param clientHttpRequestFactory the <code>ClientHttpRequestFactory</code> 
		 * @return this builder
		 */
		public Builder setClientHttpRequestFactory(ClientHttpRequestFactory clientHttpRequestFactory) {
			this.clientHttpRequestFactory = clientHttpRequestFactory;
			return this;
		}
		
		/**
		 * Set the <code>ObjectMapperConfigurer</code> for the created configuration. Allows
		 * further configuration of the Jackson <code>ObjectMapper</code> used internally.
		 *
		 * @param objectMapperConfigurer the <code>ObjectMapperConfigurer</code>
		 * @return this builder
		 */
		public Builder setObjectMapperConfigurer(ObjectMapperConfigurer objectMapperConfigurer) {
			this.objectMapperConfigurer = objectMapperConfigurer;
			return this;
		}
	}
	
	private final URI baseUri;
	
	private final RestTemplateConfigurer restTemplateConfigurer;
	
	private final ClientHttpRequestFactory clientHttpRequestFactory;
	
	private final ObjectMapperConfigurer objectMapperConfigurer;
	
	private Configuration(Builder builder) {
		baseUri = builder.baseUri;
		restTemplateConfigurer = builder.restTemplateConfigurer;
		clientHttpRequestFactory = builder.clientHttpRequestFactory;
		objectMapperConfigurer = builder.objectMapperConfigurer;
	}
	
	/**
	 * Create a configuration builder.
	 * 
	 * @return the builder
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * Create a configuration with default settings.
	 * 
	 * @return the configuration
	 */
	public static Configuration build() {
		return new Builder().build();
	}
	
	/**
	 * Build a {@link ClientFactory} for this configuration.
	 * 
	 * @return the created <code>ClientFactory</code>
	 */
	public ClientFactory buildClientFactory() {
		return new ClientFactory(this);
	}
	
	/**
	 * Get the base URI for this configuration.
	 * 
	 * @return the configuration's base URI.
	 */
	public URI getBaseUri() {
		return baseUri;
	}
	
	/**
	 * Get the <code>RestTemplateConfigurer</code> for this configuration.
	 * 
	 * @return the configuration's <code>RestTemplateConfigurer</code>.
	 */
	public RestTemplateConfigurer getRestTemplateConfigurer() {
		return restTemplateConfigurer;
	}
	
	/**
	 * Get the <code>ClientHttpRequestFactory</code> for this configuration.
	 * 
	 * @return the configuration's <code>ClientHttpRequestFactory</code>.
	 */
	public ClientHttpRequestFactory getClientHttpRequestFactory() {
		return clientHttpRequestFactory;
	}
	
	/**
	 * Get the <code>ObjectMapperConfigurer</code> for this configuration.
	 *
	 * @return the configuration's <code>ObjectMapperConfigurer</code>.
	 */
	public ObjectMapperConfigurer getObjectMapperConfigurer() {
		return objectMapperConfigurer;
	}
}
