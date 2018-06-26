package uk.co.blackpepper.bowman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * This class adapted from {@code org.springframework.hateoas.hal.Jackson2HalModule.HalResourcesDeserializer}
 * from Spring HATEOAS (https://github.com/spring-projects/spring-hateoas).
 *
 * (c) Pivotal etc., licensed under Apache 2.0.
 */
public class HalResourcesDeserializer extends ContainerDeserializerBase<List<Object>>
	implements ContextualDeserializer {
	
	private static final long serialVersionUID = 4755806754621032622L;
	
	private JavaType contentType;
	
	private JsonDeserializer contentDeserializer;
	
	public HalResourcesDeserializer() {
		this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Object.class), null);
	}
	
	public HalResourcesDeserializer(JavaType vc) {
		this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, vc), vc, null);
	}
	
	public HalResourcesDeserializer(JavaType vc, JsonDeserializer contentDeserializer) {
		this(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, vc), null, contentDeserializer);
	}
	
	private HalResourcesDeserializer(JavaType type, JavaType contentType, JsonDeserializer contentDeserializer) {
		super(type);
		this.contentType = contentType;
		this.contentDeserializer = contentDeserializer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentType()
	 */
	@Override
	public JavaType getContentType() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase#getContentDeserializer()
	 */
	@Override
	public JsonDeserializer<Object> getContentDeserializer() {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
	 * com.fasterxml.jackson.databind.DeserializationContext)
	 */
	@Override
	public List<Object> deserialize(JsonParser jp, DeserializationContext ctxt)
		throws IOException, JsonProcessingException {
		
		List<Object> result = new ArrayList<Object>();
		
		JsonDeserializer deser = contentDeserializer != null ? contentDeserializer
			: ctxt.findRootValueDeserializer(contentType);
		
		Object object;
		
		// links is an object, so we parse till we find its end.
		while (!JsonToken.END_OBJECT.equals(jp.nextToken())) {
			
			if (!JsonToken.FIELD_NAME.equals(jp.getCurrentToken())) {
				throw new JsonParseException(jp, "Expected relation name", jp.getCurrentLocation());
			}
			
			if (JsonToken.START_ARRAY.equals(jp.nextToken())) {
				while (!JsonToken.END_ARRAY.equals(jp.nextToken())) {
					object = deser.deserialize(jp, ctxt);
					result.add(object);
				}
			}
			else {
				object = deser.deserialize(jp, ctxt);
				result.add(object);
			}
		}
		
		return result;
	}
	
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
		throws JsonMappingException {
		
		JavaType vc = property.getType().getContentType();
		
		JsonDeserializer contentDeserializer = vc.getValueHandler();
		if (contentDeserializer != null) {
			contentDeserializer = ctxt.handleSecondaryContextualization(contentDeserializer, property, vc);
		}
		
		HalResourcesDeserializer des = new HalResourcesDeserializer(vc, contentDeserializer);
		return des;
	}
}
