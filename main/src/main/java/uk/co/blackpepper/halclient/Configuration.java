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

import java.net.URI;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public final class Configuration {
	
	public static final class Builder {

		private URI baseUri = URI.create("http://localhost:8080");
		
		private RestTemplateConfigurer restTemplateConfigurer;

		private ClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		
		private Builder() {
		}
		
		public Configuration build() {
			return new Configuration(this);
		}
		
		public Builder setBaseUri(String baseUri) {
			this.baseUri = URI.create(baseUri);
			return this;
		}

		public Builder setBaseUri(URI baseUri) {
			this.baseUri = baseUri;
			return this;
		}
		
		public Builder setRestTemplateConfigurer(RestTemplateConfigurer restTemplateConfigurer) {
			this.restTemplateConfigurer = restTemplateConfigurer;
			return this;
		}

		public Builder setClientHttpRequestFactory(ClientHttpRequestFactory clientHttpRequestFactory) {
			this.clientHttpRequestFactory = clientHttpRequestFactory;
			return this;
		}
	}
	
	private final URI baseUri;
	
	private final RestTemplateConfigurer restTemplateConfigurer;
	
	private final ClientHttpRequestFactory clientHttpRequestFactory;
	
	private Configuration(Builder builder) {
		baseUri = builder.baseUri;
		restTemplateConfigurer = builder.restTemplateConfigurer;
		clientHttpRequestFactory = builder.clientHttpRequestFactory;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public ClientFactory buildClientFactory() {
		return new ClientFactory(this);
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public RestTemplateConfigurer getRestTemplateConfigurer() {
		return restTemplateConfigurer;
	}
	
	public ClientHttpRequestFactory getClientHttpRequestFactory() {
		return clientHttpRequestFactory;
	}
}
