package uk.co.blackpepper.hal.client;

import java.net.URI;

public final class Configuration {
	
	private URI baseUri = URI.create("http://localhost:8080");

	private ObjectMapperFactory objectMapperFactory = new DefaultObjectMapperFactory();
	
	private RestTemplateFactory restTemplateFactory = new DefaultRestTemplateFactory();
	
	private ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
	
	public ClientFactory buildClientFactory() {
		return new ClientFactory(this);
	}
	
	public URI getBaseUri() {
		return baseUri;
	}
	
	public Configuration setBaseUri(String baseUri) {
		this.baseUri = URI.create(baseUri);
		return this;
	}

	public Configuration setBaseUri(URI baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	public ObjectMapperFactory getObjectMapperFactory() {
		return objectMapperFactory;
	}

	public Configuration setObjectMapperFactory(ObjectMapperFactory objectMapperFactory) {
		this.objectMapperFactory = objectMapperFactory;
		return this;
	}

	public RestTemplateFactory getRestTemplateFactory() {
		return restTemplateFactory;
	}

	public Configuration setRestTemplateFactory(RestTemplateFactory restTemplateFactory) {
		this.restTemplateFactory = restTemplateFactory;
		return this;
	}
	
	public ClientProxyFactory getProxyFactory() {
		return proxyFactory;
	}

	public Configuration setProxyFactory(ClientProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
		return this;
	}
}
