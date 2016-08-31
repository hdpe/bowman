package uk.co.blackpepper.hal.client;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Arrays.asList;

public class DefaultRestTemplateFactory implements RestTemplateFactory {

	@Override
	public RestTemplate create(ObjectMapper objectMapper) {
		RestTemplate restTemplate = new RestTemplate(
			new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
		
		restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(objectMapper));
		
		restTemplate.setInterceptors(
			asList(new JsonClientHttpRequestInterceptor(), new LoggingClientHttpRequestInterceptor()));
		
		return restTemplate;
	}
}
