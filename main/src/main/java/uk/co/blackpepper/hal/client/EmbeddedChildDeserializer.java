package uk.co.blackpepper.hal.client;

import java.io.IOException;

import org.springframework.hateoas.Resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class EmbeddedChildDeserializer extends StdDeserializer<Object> implements ContextualDeserializer {
	
	private RestOperations restOperations;

	private ClientProxyFactory proxyFactory;
	
	public EmbeddedChildDeserializer(RestOperations restOperations, ClientProxyFactory proxyFactory) {
		this(Object.class, restOperations, proxyFactory);
	}
	
	private EmbeddedChildDeserializer(Class<?> type, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		super(type);
		
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JavaType resourceType = ctxt.getTypeFactory().constructParametrizedType(Resource.class, Resource.class,
				handledType());
		
		Object resource = p.getCodec().readValue(p, resourceType);
		
		return proxyFactory.create((Resource) resource, (Class) handledType(), restOperations);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		return new EmbeddedChildDeserializer(ctxt.getContextualType().getRawClass(), restOperations, proxyFactory);
	}
}
