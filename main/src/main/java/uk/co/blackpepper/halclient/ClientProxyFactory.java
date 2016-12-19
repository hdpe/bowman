package uk.co.blackpepper.halclient;

import java.net.URI;

import org.springframework.hateoas.Resource;

public interface ClientProxyFactory {
	
	<T> T create(URI uri, Class<T> entityType, RestOperations restOperations);
	
	<T> T create(Resource<T> resource, Class<T> entityType, RestOperations restOperations);
}
