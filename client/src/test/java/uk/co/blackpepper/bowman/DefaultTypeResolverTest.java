package uk.co.blackpepper.bowman;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceTypeInfo;

import static org.junit.Assert.assertThat;

public class DefaultTypeResolverTest {

	public static class TypeWithoutInfo {
		// no members
	}
	
	@ResourceTypeInfo(subtypes = {TypeWithInfoSubtype1.class, TypeWithInfoSubtype2.class})
	public static class TypeWithInfo {
		// no members
	}
	
	@RemoteResource("/1")
	public static class TypeWithInfoSubtype1 {
		// no members
	}
	
	@RemoteResource("/2")
	public static class TypeWithInfoSubtype2 {
		// no members
	}
	
	private DefaultTypeResolver resolver;
	
	@Before
	public void setUp() {
		resolver = new DefaultTypeResolver();
	}
	
	@Test
	public void resolveTypeWithNoSelfLinkReturnsDeclaredType() {
		Class<?> type = resolver.resolveType(TypeWithInfo.class, new Links(), Configuration.build());
		
		assertThat(type, Matchers.<Class<?>>equalTo(TypeWithInfo.class));
	}
	
	@Test
	public void resolveTypeWithNoResourceTypeInfoReturnsDeclaredType() {
		Class<?> type = resolver.resolveType(TypeWithoutInfo.class,
			new Links(new Link("http://x", Link.REL_SELF)), Configuration.build());
		
		assertThat(type, Matchers.<Class<?>>equalTo(TypeWithoutInfo.class));
	}
	
	@Test
	public void resolveTypeWithNoMatchingSubtypeReturnsDeclaredType() {
		Class<?> type = resolver.resolveType(TypeWithInfo.class,
			new Links(new Link("http://x", Link.REL_SELF)), Configuration.build());
		
		assertThat(type, Matchers.<Class<?>>equalTo(TypeWithInfo.class));
	}

	@Test
	public void resolveTypeWithMatchingSubtypeReturnsSubtype() {
		Configuration config = Configuration.builder().setBaseUri("http://x.com").build();
		
		Class<?> type = resolver.resolveType(TypeWithInfo.class,
			new Links(new Link("http://x.com/2/1", Link.REL_SELF)), config);
		
		assertThat(type, Matchers.<Class<?>>equalTo(TypeWithInfoSubtype2.class));
	}
}
