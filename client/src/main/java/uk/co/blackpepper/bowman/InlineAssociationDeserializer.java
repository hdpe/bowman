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

/**
 * A Jackson deserializer to properly handle inline associations in an annotated entity type. A proxy
 * will be created for the annotated property, allowing the resolution of further linked associations.
 * 
 * <p>Assign this deserializer to a property with
 * <code>@JsonDeserialize(contentUsing = InlineAssociationDeserializer.class)</code>.
 * 
 * @param <T> the type or a supertype of the type that this deserializer is intended for - not needed by 
 * client code
 * 
 * @author Ryan Pickett
 * 
 */
public class InlineAssociationDeserializer<T> extends StdDeserializer<T> implements ContextualDeserializer {
	
	private static final long serialVersionUID = -8694505834979017488L;
	
	private Class<T> type;

	private RestOperations restOperations;

	private ClientProxyFactory proxyFactory;
	
	InlineAssociationDeserializer(Class<T> type, RestOperations restOperations,
			ClientProxyFactory proxyFactory) {
		super(type);
		
		this.type = type;
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JavaType resourceType = ctxt.getTypeFactory().constructParametricType(Resource.class, type);
		
		Resource<T> resource = p.getCodec().readValue(p, resourceType);
		
		return proxyFactory.create(resource, type, restOperations);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		return new InlineAssociationDeserializer<>(ctxt.getContextualType().getRawClass(), restOperations,
				proxyFactory);
	}
	
	RestOperations getRestOperations() {
		return restOperations;
	}
	
	ClientProxyFactory getProxyFactory() {
		return proxyFactory;
	}
}
