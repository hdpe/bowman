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
package uk.co.blackpepper.bowman.test.it.model;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

@RemoteResource("/bidi-children")
public class BidiChildEntity {

	private URI id;
	
	private BidiParentEntity parent;
	
	private String name;
	
	@ResourceId
	@JsonIgnore
	public URI getId() {
		return id;
	}

	@LinkedResource
	public BidiParentEntity getParent() {
		return parent;
	}

	public void setParent(BidiParentEntity parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
