package uk.co.blackpepper.sdrclient;

import java.io.IOException;

import org.springframework.hateoas.Resource;
import org.springframework.web.client.RestTemplate;

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

	private ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();
	
	private RestTemplate restTemplate = new RestTemplateFactory().createRestTemplate();
	
	protected EmbeddedChildDeserializer() {
		super(Object.class);
	}
	
	private EmbeddedChildDeserializer(JavaType entityType) {
		super(entityType);
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JavaType resourceType = ctxt.getTypeFactory().constructParametrizedType(Resource.class, Resource.class,
				handledType());
		
		Object resource = p.getCodec().readValue(p, resourceType);
		
		return proxyFactory.create((Resource) resource, (Class) handledType(), restTemplate);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		return new EmbeddedChildDeserializer(ctxt.getContextualType());
	}
}
