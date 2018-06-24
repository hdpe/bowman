package uk.co.blackpepper.bowman.test.it.model;

import java.net.URI;

import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

@RemoteResource("/pageable-entities")
public class PageableEntity {

	private URI id;
	
	private String name;
	
	private PageableEntity linked;
	
	@ResourceId
	public URI getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@LinkedResource
	public PageableEntity getLinked() {
		return linked;
	}
	
	public void setLinked(PageableEntity linked) {
		this.linked = linked;
	}
}
