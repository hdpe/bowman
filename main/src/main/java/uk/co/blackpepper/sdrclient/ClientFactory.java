package uk.co.blackpepper.sdrclient;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import uk.co.blackpepper.sdrclient.gen.annotation.LinkedResource;

import static java.util.Arrays.asList;

public class ClientFactory {

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
	
	private final URI baseUri;

	private final RestTemplate restTemplate;
	
	private final ClientProxyFactory proxyFactory = new JavassistClientProxyFactory();

	public ClientFactory(URI baseUri) {
		this.baseUri = baseUri;
		this.restTemplate = createRestTemplate();
	}

	public <T> Client<T> create(Class<T> entityType) {
		return new Client<T>(entityType, baseUri, restTemplate, proxyFactory);
	}

	private static RestTemplate createRestTemplate() {
		RestTemplate restTemplate = new RestTemplate(
				new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
		restTemplate.getMessageConverters().add(0, new MappingJackson2HttpMessageConverter(createObjectMapper()));
		restTemplate.setInterceptors(
				asList(new JsonClientHttpRequestInterceptor(), new LoggingClientHttpRequestInterceptor()));
		return restTemplate;
	}

	private static ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		mapper.registerModule(new Jackson2HalModule());
		
		SimpleModule module = new SimpleModule();
		module.setSerializerModifier(new BeanSerializerModifier() {

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
		
		mapper.registerModule(module);
		
		return mapper;
	}
}
