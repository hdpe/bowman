package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;
import java.net.URI;

import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MethodLinkUriResolverTest {
	
	private interface Content {
		
		@LinkedResource(rel = "rel")
		void linked(String x, int y);
	}
	
	@Test
	public void resolveForMethodReturnsUriWithParamsExpanded() throws Exception {
		Resource<Object> resource = new Resource<>(new Object(), new Links(new Link("http://www.example.com/{?x,y}",
			"rel")));
		
		Method linked = Content.class.getMethod("linked", String.class, int.class);
		
		URI uri = new MethodLinkUriResolver(resource).resolveForMethod(linked, new Object[] {"1", 2});
		
		assertThat(uri, is(URI.create("http://www.example.com/?x=1&y=2")));
	}
}
