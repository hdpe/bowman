package uk.co.blackpepper.sdrclient.test.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RestConfig extends RepositoryRestConfigurerAdapter {

	@Override
	public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
		objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
			.withFieldVisibility(Visibility.ANY)
			.withGetterVisibility(Visibility.NONE)
			.withIsGetterVisibility(Visibility.NONE));
	}
}
