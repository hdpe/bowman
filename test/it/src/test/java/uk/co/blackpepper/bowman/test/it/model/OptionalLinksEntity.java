package uk.co.blackpepper.bowman.test.it.model;

import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

@RemoteResource("/optional-links-entities")
public class OptionalLinksEntity {

	private URI id;

	private String name;
	
	private SimpleEntity optionalLinkItem;
	
	private List<SimpleEntity> optionalLinkCollection;
	
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
	
	@LinkedResource(optionalLink = true)
	public SimpleEntity getOptionalLinkItem() {
		return optionalLinkItem;
	}
	
	public void setOptionalLinkItem(SimpleEntity optionalLinkItem) {
		this.optionalLinkItem = optionalLinkItem;
	}
	
	@LinkedResource(optionalLink = true)
	public List<SimpleEntity> getOptionalLinkCollection() {
		return optionalLinkCollection;
	}
	
	public void setOptionalLinkCollection(List<SimpleEntity> optionalLinkCollection) {
		this.optionalLinkCollection = optionalLinkCollection;
	}
}
