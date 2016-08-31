package uk.co.blackpepper.hal.client;

import java.net.URI;

public class ClientFactory {
	
	private final URI baseUri;

	private final RestOperations restOperations;
	
	private final ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();

	public ClientFactory(URI baseUri) {
		this.baseUri = baseUri;
		this.restOperations = new RestOperationsFactory(new ObjectMapperFactory(), proxyFactory).create();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<T>(entityType, baseUri, restOperations, proxyFactory);
	}
}
