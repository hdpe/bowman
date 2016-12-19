package uk.co.blackpepper.halclient;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultRestTemplateFactory implements RestTemplateFactory {

	@Override
	public RestTemplate create(ObjectMapper objectMapper) {
		RestTemplate restTemplate = new RestTemplate(
			new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));
		
		restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(objectMapper));
		restTemplate.getInterceptors().add(new JsonClientHttpRequestInterceptor());
		
		return restTemplate;
	}
}
