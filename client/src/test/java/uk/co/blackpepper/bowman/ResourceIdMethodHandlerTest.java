package uk.co.blackpepper.bowman;

import java.net.URI;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class ResourceIdMethodHandlerTest {
	
	@Test
	public void invokeWithResourceWithSelfLinkReturnsLinkUri() {
		Resource<Object> resource = new Resource<>(new Object(), new Links(new Link("http://www.example.com/1",
			Link.REL_SELF)));
		
		Object result = new ResourceIdMethodHandler(resource).invoke(null, null, null, null);
		
		assertThat(result, is(URI.create("http://www.example.com/1")));
	}
	
	@Test
	public void invokeWithResourceWithNoSelfLinkReturnsNull() {
		Resource<Object> resource = new Resource<>(new Object(), new Links(new Link("http://www.example.com/1",
			"some-other-rel")));
		
		Object result = new ResourceIdMethodHandler(resource).invoke(null, null, null, null);
		
		assertThat(result, is(nullValue()));
	}
}
