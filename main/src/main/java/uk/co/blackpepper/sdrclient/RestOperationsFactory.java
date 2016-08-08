package uk.co.blackpepper.sdrclient;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static java.util.Arrays.asList;

public class RestOperationsFactory {

	private final ObjectMapperFactory objectMapperFactory = new ObjectMapperFactory();
	
	public RestOperations create() {
		return new RestOperations(createRestTemplate(), objectMapperFactory.create());
	}
	
	private RestTemplate createRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(
			new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
		
		restTemplate.getMessageConverters().add(0,
			new MappingJackson2HttpMessageConverter(objectMapperFactory.create()));
		
		restTemplate.setInterceptors(
			asList(new JsonClientHttpRequestInterceptor(), new LoggingClientHttpRequestInterceptor()));
		
		return restTemplate;
	}
}
