package uk.co.blackpepper.bowman;

import java.net.URI;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.getField;

public class ConfigurationTest {
	
	@Test
	public void buildClientFactoryBuildsFactoryWithConfiguration() {
		ClientFactory factory = Configuration.builder()
			.setBaseUri(URI.create("http://x.com")).build().buildClientFactory();
		
		Client<Object> client = factory.create(Object.class);
		
		assertThat((URI) getField(client, "baseUri"), is(URI.create("http://x.com")));
	}
}
