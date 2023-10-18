package uk.co.blackpepper.bowman;

import java.net.URI;

import org.junit.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceIdMethodHandlerTest {
	
	@Test
	public void invokeWithResourceWithSelfLinkReturnsLinkUri() {
		EntityModel<Object> resource = EntityModel.of(new Object(), Links.of(Link.of("http://www.example.com/1",
			IanaLinkRelations.SELF)));
		
		Object result = new ResourceIdMethodHandler(resource).invoke(null, null, null, null);
		
		assertThat(result, is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void invokeWithResourceWithNoSelfLinkReturnsNull() {
		EntityModel<Object> resource = EntityModel.of(new Object(), Links.of(Link.of("http://www.example.com/1",
			"some-other-rel")));
		
		Object result = new ResourceIdMethodHandler(resource).invoke(null, null, null, null);
		
		assertThat(result, is(nullValue()));
	}
}
