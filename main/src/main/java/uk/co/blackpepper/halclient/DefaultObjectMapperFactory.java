package uk.co.blackpepper.halclient;

import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

public class DefaultObjectMapperFactory implements ObjectMapperFactory {
	
	@Override
	public ObjectMapper create(HandlerInstantiator instantiator) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.registerModule(new Jackson2HalModule());
		mapper.registerModule(new JacksonClientModule());
		mapper.setHandlerInstantiator(instantiator);
		return mapper;
	}
}
