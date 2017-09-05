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
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

/**
 * A module for handling serialization of Bowman annotated classes.
 * 
 * <p>Registering this module with an {@link com.fasterxml.jackson.databind.ObjectMapper}
 * will cause properties annotated with {@link LinkedResource} to be serialized as
 * URI strings (single-valued associations) or arrays of URI strings (collection-valued
 * associations).
 * 
 * @author Ryan Pickett
 * 
 */
public class JacksonClientModule extends SimpleModule {

	private static final long serialVersionUID = 3399166531461618498L;

	private static class LinkedResourceUriSerializer extends StdSerializer<Object> {

		private static final long serialVersionUID = -5901774722661025524L;

		protected LinkedResourceUriSerializer() {
			super(Object.class);
		}

		@Override
		public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
				throws IOException, JsonGenerationException {
			if (value instanceof Iterable<?>) {
				jgen.writeStartArray();
				for (Object child : (Iterable<?>) value) {
					jgen.writeString(getEntityUri(child));
				}
				jgen.writeEndArray();
			}
			else {
				jgen.writeString(getEntityUri(value));
			}
		}

		private static String getEntityUri(Object value) {
			return ReflectionSupport.getId(value).toString();
		}
	}
	
	public JacksonClientModule() {
		setSerializerModifier(new BeanSerializerModifier() {

			@Override
			public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
					List<BeanPropertyWriter> beanProperties) {
				
				for (BeanPropertyWriter writer : beanProperties) {
					if (writer.getAnnotation(LinkedResource.class) != null) {
						writer.assignSerializer(new LinkedResourceUriSerializer());
					}
				}
				
				return beanProperties;
			}
		});
	}
}
