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
package uk.co.blackpepper.bowman.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.client.SimpleEntity;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SimpleEntityIT extends AbstractIT {

	private Client<SimpleEntity> client;

	@Before
	public void setup() {
		client = clientFactory.create(SimpleEntity.class);
	}

	@Test
	public void canGetEntityName() {
		SimpleEntity sent = new SimpleEntity();
		sent.setName("x");

		URI location = client.post(sent);

		SimpleEntity retrieved = client.get(location);
		assertThat(retrieved.getName(), is("x"));
	}

	@Test
	public void canGetEntityAssociation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("x");
		client.post(related);

		SimpleEntity sent = new SimpleEntity();
		sent.setRelated(related);

		URI location = client.post(sent);

		SimpleEntity retrieved = client.get(location);
		assertThat(retrieved.getRelated().getName(), is("x"));
	}
	
	@Test
	public void canGetNullEntityAssocation() {
		URI location = client.post(new SimpleEntity());

		SimpleEntity retrieved = client.get(location);
		assertThat(retrieved.getRelated(), is(nullValue()));
	}
	
	@Test
	public void canGetAllEntities() {
		SimpleEntity sent = new SimpleEntity();
		
		URI location = client.post(sent);
		
		assertThat(Lists.<Object>newArrayList(client.getAll()),
			hasItem(hasProperty("id", equalTo(location))));
	}
	
	@Test
	public void canPostEntityWithAssociatedProxy() {
		SimpleEntity related = new SimpleEntity();
		URI relatedResource = client.post(related);
		
		SimpleEntity sent = new SimpleEntity();
		sent.setRelated(client.get(relatedResource));
		
		client.post(sent);
	}
	
	@Test
	public void canPutEntity() {
		SimpleEntity sent = new SimpleEntity();
		sent.setName("current");
		
		URI posted = client.post(sent);
		
		assertThat(sent.getId(), is(posted));
		assertThat(sent.getName(), is("current"));
		
		sent.setName("updated");
		client.put(sent);
		
		SimpleEntity updated = client.get(sent.getId());
		
		assertThat(updated.getId(), is(posted));
		assertThat(updated.getName(), is("updated"));
	}
	
	@Test
	public void canGetAndPutEntity() {
		SimpleEntity sent = new SimpleEntity();
		sent.setName("current");
		
		URI posted = client.post(sent);
		
		assertThat(sent.getId(), is(posted));
		assertThat(sent.getName(), is("current"));
		
		sent = client.get(posted);
		
		sent.setName("updated");
		client.put(sent);
		
		SimpleEntity updated = client.get(sent.getId());
		
		assertThat(updated.getId(), is(posted));
		assertThat(updated.getName(), is("updated"));
	}
}
