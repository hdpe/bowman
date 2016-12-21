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
package uk.co.blackpepper.halclient.test.it;

import java.net.URI;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.halclient.Client;
import uk.co.blackpepper.halclient.test.client.EmbeddedBidiChildEntity;
import uk.co.blackpepper.halclient.test.client.EmbeddedBidiParentEntity;
import uk.co.blackpepper.halclient.test.client.SimpleEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EmbeddedBidiIT extends AbstractIT {

	private Client<EmbeddedBidiParentEntity> client;
	
	private Client<SimpleEntity> simpleEntityClient;
	
	@Before
	public void setup() {
		client = clientFactory.create(EmbeddedBidiParentEntity.class);
		simpleEntityClient = clientFactory.create(SimpleEntity.class);
	}
	
	@Test
	public void canGetChildAssocation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		simpleEntityClient.post(related);
		
		EmbeddedBidiParentEntity parent = new EmbeddedBidiParentEntity();
		
		EmbeddedBidiChildEntity child = new EmbeddedBidiChildEntity();
		child.setName("x");
		child.setRelated(related);
//		child.setParent(parent);

		parent.setChild(child);
		
		URI location = client.post(parent);
		
		EmbeddedBidiParentEntity retrieved = client.get(location);
		
		EmbeddedBidiChildEntity retrievedItem = retrieved.getChild();
		assertThat(retrievedItem.getName(), is("x"));
		assertThat(retrievedItem.getRelated().getName(), is("related"));
	}
	
	@Test
	public void canGetChildrenAssocation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		simpleEntityClient.post(related);
		
		EmbeddedBidiParentEntity parent = new EmbeddedBidiParentEntity();
		
		EmbeddedBidiChildEntity child = new EmbeddedBidiChildEntity();
		child.setName("x");
		child.setRelated(related);
//		child.setParent(parent);

		parent.getChildren().add(child);
		
		URI location = client.post(parent);
		
		EmbeddedBidiParentEntity retrieved = client.get(location);
		
		EmbeddedBidiChildEntity retrievedItem = retrieved.getChildren().iterator().next();
		assertThat(retrievedItem.getName(), is("x"));
		assertThat(retrievedItem.getRelated().getName(), is("related"));
	}
}
