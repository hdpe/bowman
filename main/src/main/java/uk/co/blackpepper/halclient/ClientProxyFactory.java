package uk.co.blackpepper.halclient;

import org.springframework.hateoas.Resource;

public interface ClientProxyFactory {
	
	<T> T create(Resource<T> resource, Class<T> entityType, RestOperations restOperations);
}
