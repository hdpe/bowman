package uk.co.blackpepper.hal.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;

public interface ObjectMapperFactory {

	ObjectMapper create(HandlerInstantiator instantiator);
}
