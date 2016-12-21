package uk.co.blackpepper.halclient;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

class RestOperationsFactory {
	
	private static class RestOperationsInstantiation extends HandlerInstantiator {
		
		private final RestOperations restOperations;
		
		private final Map<Class<?>, Object> handlerMap = new HashMap<>();
		
		RestOperationsInstantiation(Configuration configuration) {
			
			ObjectMapper objectMapper = configuration.getObjectMapperFactory().create(this);
			RestTemplate restTemplate = configuration.getRestTemplateFactory().create(objectMapper);
			
			restOperations = new RestOperations(restTemplate, objectMapper);
			
			handlerMap.put(EmbeddedChildDeserializer.class,
					new EmbeddedChildDeserializer(restOperations, configuration.getProxyFactory()));
		}
		
		public RestOperations getRestOperations() {
			return restOperations;
		}
		
		@Override
		public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated,
				Class<?> deserClass) {
			return (JsonDeserializer<?>) findHandlerInstance(deserClass);
		}

		@Override
		public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated,
				Class<?> keyDeserClass) {
			return (KeyDeserializer) findHandlerInstance(keyDeserClass);
		}

		@Override
		public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated,
				Class<?> serClass) {
			return (JsonSerializer<?>) findHandlerInstance(serClass);
		}

		@Override
		public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated,
				Class<?> builderClass) {
			return (TypeResolverBuilder<?>) findHandlerInstance(builderClass);
		}

		@Override
		public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated,
				Class<?> resolverClass) {
			return (TypeIdResolver) findHandlerInstance(resolverClass);
		}

		private Object findHandlerInstance(Class<?> clazz) {
			Object handler = handlerMap.get(clazz);
			return handler != null ? handler : BeanUtils.instantiate(clazz);
		}
	}

	private final Configuration configuration;
	
	RestOperationsFactory(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public RestOperations create() {
		return new RestOperationsInstantiation(configuration).getRestOperations();
	}
}
