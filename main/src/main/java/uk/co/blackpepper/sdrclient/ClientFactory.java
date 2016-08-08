package uk.co.blackpepper.sdrclient;

import java.net.URI;

import org.springframework.web.client.RestTemplate;

public class ClientFactory {
	
	private final URI baseUri;

	private final RestTemplate restTemplate;
	
	private final ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();

	public ClientFactory(URI baseUri) {
		this.baseUri = baseUri;
		this.restTemplate = new RestTemplateFactory().createRestTemplate();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<T>(entityType, baseUri, restTemplate, proxyFactory);
	}
}
