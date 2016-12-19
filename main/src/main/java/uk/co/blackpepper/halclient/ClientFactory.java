package uk.co.blackpepper.halclient;

public class ClientFactory {
	
	private final Configuration configuration;

	private final RestOperations restOperations;

	ClientFactory(Configuration configuration) {
		this.configuration = configuration;
		this.restOperations = new RestOperationsFactory(configuration).create();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<T>(entityType, configuration, restOperations);
	}
}
