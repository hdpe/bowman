package uk.co.blackpepper.bowman;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ReflectionSupportTest {

	private ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
	}
	
	@Test
	public void getIdWhenNoIdAccessor() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("No @ResourceId found for java.lang.Object");

		ReflectionSupport.getId(new Object());
	}
}
