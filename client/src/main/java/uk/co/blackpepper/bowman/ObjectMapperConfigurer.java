package uk.co.blackpepper.bowman;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Interface supporting the configuration of the Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} which is
 * used internally by the {@link Client}.
 *
 * @author Karl Spies
 */
public interface ObjectMapperConfigurer {
	
	void configure(ObjectMapper objectMapper);
}
