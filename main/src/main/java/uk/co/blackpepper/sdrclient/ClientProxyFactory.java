package uk.co.blackpepper.sdrclient;

import java.net.URI;

import org.springframework.hateoas.Resource;
import org.springframework.web.client.RestTemplate;

public interface ClientProxyFactory {
	
	<T> T create(URI uri, Class<T> entityType, RestTemplate restTemplate);
	
	<T> T create(Resource<T> resource, Class<T> entityType, RestTemplate restTemplate);
}
