package uk.co.blackpepper.bowman;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import uk.co.blackpepper.bowman.annotation.LinkedResource;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MethodLinkAttributesResolverTest {
	
	private interface Content {
		
		@LinkedResource
		Object getNoRel();
		
		@LinkedResource(rel = "custom")
		void withRel();
	}
	
	private MethodLinkAttributesResolver resolver;
	
	@Before
	public void setUp() {
		resolver = new MethodLinkAttributesResolver();
	}
	
	@Test
	public void resolveForMethodWithNoRelReturnsMethodNameAsLinkName() throws Exception {
		Method linked = Content.class.getMethod("getNoRel");
		
		MethodLinkAttributes attribs = resolver.resolveForMethod(linked);
		
		assertThat(attribs.getLinkName(), is("noRel"));
	}
	
	@Test
	public void resolveForMethodWithRelReturnsRelAsLinkName() throws Exception {
		Method linked = Content.class.getMethod("withRel");
		
		MethodLinkAttributes attribs = resolver.resolveForMethod(linked);
		
		assertThat(attribs.getLinkName(), is("custom"));
	}
}
