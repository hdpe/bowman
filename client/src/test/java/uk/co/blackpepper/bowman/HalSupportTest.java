package uk.co.blackpepper.bowman;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HalSupportTest {
	
	@Test
	public void toLinkNameWithIsMethodReturnsDecapitalizedMethodNameSubstring() {
		assertThat(HalSupport.toLinkName("isTheProperty"), is("theProperty"));
	}
	
	@Test
	public void toLinkNameWithGetMethodReturnsDecapitalizedMethodNameSubstring() {
		assertThat(HalSupport.toLinkName("getTheProperty"), is("theProperty"));
	}
	
	@Test
	public void toLinkNameWithOtherMethodReturnsMethodName() {
		assertThat(HalSupport.toLinkName("aMethod"), is("aMethod"));
	}
}
