package uk.co.blackpepper.sdrclient.test.it;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import uk.co.blackpepper.sdrclient.Client;
import uk.co.blackpepper.sdrclient.ClientFactory;
import uk.co.blackpepper.sdrclient.test.server.model.client.Entity1;

import java.net.URI;

public class Entity1IT {

  @Test
  public void testEntity1() {
    
    Client<Entity1> client = new ClientFactory(URI.create(System.getProperty("baseUrl")))
        .create(Entity1.class);
    
    Entity1 sent = new Entity1();
    sent.setName("x");
    
    URI location = client.post(sent);
    
    Entity1 retrieved = client.get(location);
    assertThat(retrieved.getName(), is("x"));
  }
}
