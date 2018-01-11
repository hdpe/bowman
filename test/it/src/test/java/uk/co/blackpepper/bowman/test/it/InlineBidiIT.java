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

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.InlineBidiChildEntity;
import uk.co.blackpepper.bowman.test.it.model.InlineBidiParentEntity;
import uk.co.blackpepper.bowman.test.it.model.SimpleEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class InlineBidiIT extends AbstractIT {

	private Client<InlineBidiParentEntity> client;
	
	private Client<SimpleEntity> simpleEntityClient;
	
	@Before
	public void setup() {
		client = clientFactory.create(InlineBidiParentEntity.class);
		simpleEntityClient = clientFactory.create(SimpleEntity.class);
	}
	
	@Test
	public void canGetChildAssociation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		simpleEntityClient.post(related);
		
		InlineBidiParentEntity parent = new InlineBidiParentEntity();
		
		InlineBidiChildEntity child = new InlineBidiChildEntity();
		child.setName("x");
		child.setRelated(related);

		parent.setChild(child);
		
		URI location = client.post(parent);
		
		InlineBidiParentEntity retrieved = client.get(location);
		
		InlineBidiChildEntity retrievedItem = retrieved.getChild();
		assertThat(retrievedItem.getName(), is("x"));
		assertThat(retrievedItem.getRelated().getName(), is("related"));
	}
	
	@Test
	public void canGetChildrenAssociation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("related");
		simpleEntityClient.post(related);
		
		InlineBidiParentEntity parent = new InlineBidiParentEntity();
		
		InlineBidiChildEntity child = new InlineBidiChildEntity();
		child.setName("x");
		child.setRelated(related);

		parent.getChildren().add(child);
		
		URI location = client.post(parent);
		
		InlineBidiParentEntity retrieved = client.get(location);
		
		InlineBidiChildEntity retrievedItem = retrieved.getChildren().iterator().next();
		assertThat(retrievedItem.getName(), is("x"));
		assertThat(retrievedItem.getRelated().getName(), is("related"));
	}
}
