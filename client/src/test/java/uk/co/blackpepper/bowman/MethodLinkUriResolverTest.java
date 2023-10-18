package uk.co.blackpepper.bowman;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MethodLinkUriResolverTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void resolveForMethodWithNoMatchingLinkThrowsException() {
		EntityModel<Object> resource = EntityModel.of(new Object(), Links.of(Link.of("http://www.example.com",
			"other")));
		
		thrown.expect(NoSuchLinkException.class);
		thrown.expect(hasProperty("linkName", is("link1")));
		
		new MethodLinkUriResolver().resolveForMethod(resource, "link1", new Object[0]);
	}
	
	@Test
	public void resolveForMethodReturnsUriWithParamsExpanded() {
		EntityModel<Object> resource = EntityModel.of(new Object(),
			Links.of(Link.of("http://www.example.com/{?x,y}",
				"link1")));
		
		URI uri = new MethodLinkUriResolver().resolveForMethod(resource, "link1", new Object[] {"1", 2});
		
		assertThat(uri, is(URI.create("http://www.example.com/?x=1&y=2")));
	}
}
