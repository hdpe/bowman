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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.test.it.model.PageableEntity;
import uk.co.blackpepper.bowman.test.it.model.PageableEntityResultPage;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PagingIT extends AbstractIT {
	
	private Client<PageableEntity> entityClient;
	
	private Client<PageableEntityResultPage> pageClient;

	@Before
	public void setup() {
		entityClient = clientFactory.create(PageableEntity.class);
		pageClient = clientFactory.create(PageableEntityResultPage.class);
	}

	@Test
	public void canPageEntities() {
		for (int i = 1; i <= 3; i++) {
			PageableEntity sent = new PageableEntity();
			sent.setName(String.valueOf(i));
			
			entityClient.post(sent);
		}
		
		URI firstPageUri = UriComponentsBuilder.fromUri(baseUri)
			.path("/pageable-entities")
			.query("page=0&size=1")
			.build().toUri();
		
		PageableEntityResultPage page1 = pageClient.get(firstPageUri);
		
		assertThat(page1.getContent().get(0).getName(), is("1"));
		
		PageableEntityResultPage page2 = page1.getNext();
		assertThat(page2.getContent().get(0).getName(), is("2"));
		
		PageableEntityResultPage page3 = page2.getNext();
		assertThat(page3.getContent().get(0).getName(), is("3"));
	}
	
	@Test
	@Ignore("https://github.com/spring-projects/spring-hateoas/issues/725")
	public void canGetLinkedResourceFromPage() {
		PageableEntity related = new PageableEntity();
		related.setName("related");
		entityClient.post(related);
		
		PageableEntity entity = new PageableEntity();
		entity.setLinked(related);
		entityClient.post(entity);
		
		URI firstPageUri = UriComponentsBuilder.fromUri(baseUri)
			.path("/pageable-entities")
			.query("page=0&size=1")
			.build().toUri();
		
		PageableEntityResultPage page = pageClient.get(firstPageUri);
		
		// entity will be on the second page
		assertThat(page.getNext().getContent().get(0).getLinked().getName(), is("related"));
	}
}
