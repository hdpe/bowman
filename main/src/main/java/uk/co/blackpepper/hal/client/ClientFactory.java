package uk.co.blackpepper.hal.client;

import java.net.URI;

public class ClientFactory {
	
	private final Configuration configuration;

	private final RestOperations restOperations;
	
	public ClientFactory() {
		this(Configuration.builder().build());
	}
	
	public ClientFactory(URI baseUri) {
		this(Configuration.builder().baseUri(baseUri).build());
	}

	public ClientFactory(Configuration configuration) {
		this.configuration = configuration;
		this.restOperations = new RestOperationsFactory(configuration).create();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<T>(entityType, configuration, restOperations);
	}
}
