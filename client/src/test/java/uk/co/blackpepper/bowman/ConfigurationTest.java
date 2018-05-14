package uk.co.blackpepper.bowman;

import java.net.URI;

import org.junit.Test;

import uk.co.blackpepper.bowman.annotation.RemoteResource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ConfigurationTest {
	
	@RemoteResource("/y")
	private static class Entity {
	}
	
	@Test
	public void buildClientFactoryBuildsFactoryWithConfiguration() {
		ClientFactory factory = Configuration.builder()
			.setBaseUri(URI.create("http://x.com")).build().buildClientFactory();
		
		Client<Entity> client = factory.create(Entity.class);
		
		assertThat(client.getBaseUri(), is(URI.create("http://x.com/y")));
	}
}
