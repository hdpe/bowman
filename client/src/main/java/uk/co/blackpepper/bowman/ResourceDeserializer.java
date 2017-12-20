package uk.co.blackpepper.bowman;

import java.io.IOException;

import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

class ResourceDeserializer extends StdDeserializer<Resource<?>> implements ContextualDeserializer {

	private static final long serialVersionUID = -7290132544264448620L;
	
	private TypeResolver typeResolver;

	private Configuration configuration;

	ResourceDeserializer(Class<?> type, TypeResolver typeResolver, Configuration configuration) {
		super(type);
		this.typeResolver = typeResolver;
		this.configuration = configuration;
	}
	
	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
		throws JsonMappingException {
		
		Class<?> resourceContentType = ctxt.getContextualType().getBindings().getTypeParameters().get(0).getRawClass();
		return new ResourceDeserializer(resourceContentType, typeResolver, configuration);
	}

	@Override
	public Resource<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException,
		JsonProcessingException {

		ObjectNode node = p.readValueAs(ObjectNode.class);

		ObjectMapper mapper = (ObjectMapper) p.getCodec();

		ResourceSupport resource = mapper.convertValue(node, ResourceSupport.class);
		Links links = new Links(resource.getLinks());
		
		Class<?> resourceContentType = typeResolver.resolveType(handledType(), links, configuration);
		return createResource(resourceContentType, node, links, mapper);
	}
	
	private static <T> Resource<T> createResource(Class<T> contentType, ObjectNode sourceTree,
		Links links, ObjectMapper mapper) {
		
		T content = mapper.convertValue(sourceTree, contentType);
		return new Resource<>(content, links);
	}
}
