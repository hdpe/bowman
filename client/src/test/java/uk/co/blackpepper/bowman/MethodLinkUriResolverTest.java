package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;
import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.Resource;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class MethodLinkUriResolverTest {
	
	private interface Content {
		
		@LinkedResource(rel = "simple")
		void linked();
		
		@LinkedResource(rel = "params")
		void linked(String x, int y);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void resolveForMethodWithNoMatchingLinkThrowsException() throws Exception {
		Resource<Object> resource = new Resource<>(new Object(), new Links(new Link("http://www.example.com",
			"other")));
		
		Method linked = Content.class.getMethod("linked");
		
		thrown.expect(NoSuchLinkException.class);
		thrown.expect(hasProperty("linkName", is("simple")));
		
		new MethodLinkUriResolver().resolveForMethod(resource, linked, new Object[0]);
	}
	
	@Test
	public void resolveForMethodReturnsUriWithParamsExpanded() throws Exception {
		Resource<Object> resource = new Resource<>(new Object(), new Links(new Link("http://www.example.com/{?x,y}",
			"params")));
		
		Method linked = Content.class.getMethod("linked", String.class, int.class);
		
		URI uri = new MethodLinkUriResolver().resolveForMethod(resource, linked, new Object[] {"1", 2});
		
		assertThat(uri, is(URI.create("http://www.example.com/?x=1&y=2")));
	}
}
