package uk.co.blackpepper.hal.client;

import java.net.URI;

public final class Configuration {

	public static class Builder {
		
		private URI baseUri = URI.create("http://localhost:8080");
		
		private ObjectMapperFactory objectMapperFactory = new DefaultObjectMapperFactory();
		
		private RestTemplateFactory restTemplateFactory = new DefaultRestTemplateFactory();

		private ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
		
		public Configuration build() {
			return new Configuration(this);
		}
		
		public Builder baseUri(URI baseUri) {
			this.baseUri = baseUri;
			return this;
		}

		public Builder objectMapperFactory(ObjectMapperFactory objectMapperFactory) {
			this.objectMapperFactory = objectMapperFactory;
			return this;
		}

		public Builder restTemplateFactory(RestTemplateFactory restTemplateFactory) {
			this.restTemplateFactory = restTemplateFactory;
			return this;
		}
		
		public Builder proxyFactory(ClientProxyFactory proxyFactory) {
			this.proxyFactory = proxyFactory;
			return this;
		}
	}
	
	private URI baseUri;

	private ObjectMapperFactory objectMapperFactory;
	
	private RestTemplateFactory restTemplateFactory;
	
	private ClientProxyFactory proxyFactory;
	
	private Configuration(Builder builder) {
		this.baseUri = builder.baseUri;
		this.objectMapperFactory = builder.objectMapperFactory;
		this.restTemplateFactory = builder.restTemplateFactory;
		this.proxyFactory = builder.proxyFactory;
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public ObjectMapperFactory getObjectMapperFactory() {
		return objectMapperFactory;
	}
	
	public RestTemplateFactory getRestTemplateFactory() {
		return restTemplateFactory;
	}
	
	public ClientProxyFactory getProxyFactory() {
		return proxyFactory;
	}
}
