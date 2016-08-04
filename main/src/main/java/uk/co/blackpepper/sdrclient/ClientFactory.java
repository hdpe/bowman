package uk.co.blackpepper.sdrclient;

import java.net.URI;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;

public class ClientFactory {

	private final URI baseUri;

	private final RestTemplate restTemplate;

	public ClientFactory(URI baseUri) {
		this.baseUri = baseUri;
		this.restTemplate = createRestTemplate();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<T>(entityType, baseUri, restTemplate);
	}

	private RestTemplate createRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(
				new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter());
		restTemplate.setInterceptors(
				asList(new JsonClientHttpRequestInterceptor(), new LoggingClientHttpRequestInterceptor()));
		return restTemplate;
	}
}
