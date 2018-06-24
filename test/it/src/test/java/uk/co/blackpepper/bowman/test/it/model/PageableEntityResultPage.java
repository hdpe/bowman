package uk.co.blackpepper.bowman.test.it.model;

import java.util.List;

import org.springframework.hateoas.hal.Jackson2HalModule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import uk.co.blackpepper.bowman.InlineAssociationDeserializer;
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;

@RemoteResource("/pageable-entities")
public abstract class PageableEntityResultPage {
	
	private List<PageableEntity> content;
	
	@JsonProperty("_embedded")
	@JsonDeserialize(
		using = Jackson2HalModule.HalResourcesDeserializer.class,
		contentUsing = InlineAssociationDeserializer.class,
		contentAs = PageableEntity.class)
	public List<PageableEntity> getContent() {
		return content;
	}
	
	public void setContent(List<PageableEntity> content) {
		this.content = content;
	}
	
	@LinkedResource
	public abstract PageableEntityResultPage getNext();
}
