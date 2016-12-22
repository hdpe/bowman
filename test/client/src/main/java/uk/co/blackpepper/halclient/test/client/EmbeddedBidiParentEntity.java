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
package uk.co.blackpepper.halclient.test.client;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import uk.co.blackpepper.halclient.EmbeddedChildDeserializer;
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

@RemoteResource("/embedded-bidi-parents")
public class EmbeddedBidiParentEntity {

	private URI id;
	
	private String name;

	private Set<EmbeddedBidiChildEntity> children = new LinkedHashSet<>();
	
	private EmbeddedBidiChildEntity child;
	
	@ResourceId
	@JsonIgnore
	public URI getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonDeserialize(using = EmbeddedChildDeserializer.class)
	public EmbeddedBidiChildEntity getChild() {
		return child;
	}

	public void setChild(EmbeddedBidiChildEntity child) {
		this.child = child;
	}

	@JsonDeserialize(contentUsing = EmbeddedChildDeserializer.class)
	public Set<EmbeddedBidiChildEntity> getChildren() {
		return children;
	}
}
