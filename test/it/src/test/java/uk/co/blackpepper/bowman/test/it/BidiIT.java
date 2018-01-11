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
import uk.co.blackpepper.bowman.test.it.model.BidiChildEntity;
import uk.co.blackpepper.bowman.test.it.model.BidiParentEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BidiIT extends AbstractIT {

	private Client<BidiParentEntity> parentClient;
	
	private Client<BidiChildEntity> childClient;

	@Before
	public void setup() {
		parentClient = clientFactory.create(BidiParentEntity.class);
		childClient = clientFactory.create(BidiChildEntity.class);
	}

	@Test
	public void canGetParentAssociation() {
		BidiParentEntity parent = new BidiParentEntity();
		parent.setName("x");
		parentClient.post(parent);

		BidiChildEntity sent = new BidiChildEntity();
		sent.setParent(parent);

		URI location = childClient.post(sent);

		BidiChildEntity retrieved = childClient.get(location);
		assertThat(retrieved.getParent().getName(), is("x"));
	}
	
	@Test
	public void canGetChildrenAssociation() {
		BidiParentEntity parent = new BidiParentEntity();
		URI location = parentClient.post(parent);
		
		BidiChildEntity child = new BidiChildEntity();
		child.setName("x");
		child.setParent(parent);
		childClient.post(child);
		
		BidiParentEntity retrieved = parentClient.get(location);
		assertThat(retrieved.getChildren().iterator().next().getName(), is("x"));
	}
}
