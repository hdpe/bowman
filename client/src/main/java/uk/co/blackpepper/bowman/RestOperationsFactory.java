/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.bowman;

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
		
		RestOperationsInstantiation(Configuration configuration, ClientProxyFactory proxyFactory,
				ObjectMapperFactory objectMapperFactory, RestTemplateFactory restTemplateFactory) {
			
			ObjectMapper objectMapper = objectMapperFactory.create(this);
			RestTemplate restTemplate = restTemplateFactory.create(configuration.getClientHttpRequestFactory(),
					objectMapper);
			
			if (configuration.getRestTemplateConfigurer() != null) {
				configuration.getRestTemplateConfigurer().configure(restTemplate);
			}
			
			if (configuration.getObjectMapperConfigurer() != null) {
				configuration.getObjectMapperConfigurer().configure(objectMapper);
			}
			
			restOperations = new RestOperations(restTemplate, objectMapper);
			
			handlerMap.put(InlineAssociationDeserializer.class,
					new InlineAssociationDeserializer<>(Object.class, restOperations, proxyFactory));
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
	
	private final ClientProxyFactory proxyFactory;

	private final ObjectMapperFactory objectMapperFactory;

	private final RestTemplateFactory restTemplateFactory;
	
	RestOperationsFactory(Configuration configuration, ClientProxyFactory proxyFactory) {
		this(configuration, proxyFactory, new DefaultObjectMapperFactory(), new DefaultRestTemplateFactory());
	}
	
	RestOperationsFactory(Configuration configuration, ClientProxyFactory proxyFactory,
			ObjectMapperFactory objectMapperFactory, RestTemplateFactory restTemplateFactory) {
		this.configuration = configuration;
		this.proxyFactory = proxyFactory;
		this.objectMapperFactory = objectMapperFactory;
		this.restTemplateFactory = restTemplateFactory;
	}
	
	public RestOperations create() {
		return new RestOperationsInstantiation(configuration, proxyFactory, objectMapperFactory, restTemplateFactory)
				.getRestOperations();
	}
}
