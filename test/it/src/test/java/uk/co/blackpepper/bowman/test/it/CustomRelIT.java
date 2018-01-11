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

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.CustomRelEntity;
import uk.co.blackpepper.bowman.test.it.model.SimpleEntity;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomRelIT extends AbstractIT {
	
	private Client<CustomRelEntity> client;
	
	private Client<SimpleEntity> simpleEntityClient;
	
	@Before
	public void setup() {
		client = clientFactory.create(CustomRelEntity.class);
		simpleEntityClient = clientFactory.create(SimpleEntity.class);
	}
	
	@Test
	public void canGetCustomRelLinkedAssociation() {
		SimpleEntity related = new SimpleEntity();
		related.setName("x");
		simpleEntityClient.post(related);
		
		CustomRelEntity entity = new CustomRelEntity();
		entity.setRelated(related);
		client.post(entity);
		
		entity = client.get(entity.getId());
		
		assertThat(entity.getRelated().getName(), is("x"));
	}
}
