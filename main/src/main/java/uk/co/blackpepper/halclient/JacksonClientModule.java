package uk.co.blackpepper.halclient;

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

import uk.co.blackpepper.halclient.annotation.LinkedResource;

public class JacksonClientModule extends SimpleModule {

	private static class LinkedResourceUriSerializer extends StdSerializer<Object> {

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
