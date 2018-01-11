package uk.co.blackpepper.bowman;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HalSupportTest {
	
	private ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
	}
	
	@Test
	public void toLinkNameWithIsMethodReturnsDecapitalizedString() {
		assertThat(HalSupport.toLinkName("isTheProperty"), is("theProperty"));
	}
	
	@Test
	public void toLinkNameWithGetMethodReturnsDecapitalizedString() {
		assertThat(HalSupport.toLinkName("getTheProperty"), is("theProperty"));
	}
	
	@Test
	public void toLinkNameWithNonAccessorMethodThrowsException() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("not a bean property getter: nonAccessor");
		
		HalSupport.toLinkName("nonAccessor");
	}
}
