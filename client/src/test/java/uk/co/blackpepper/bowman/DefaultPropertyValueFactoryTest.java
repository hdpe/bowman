package uk.co.blackpepper.bowman;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class DefaultPropertyValueFactoryTest {

	private DefaultPropertyValueFactory factory;
	
	private ExpectedException thrown = ExpectedException.none();
	
	@Rule
	public ExpectedException getThrown() {
		return thrown;
	}
		
	@Before
	public void setUp() {
		factory = new DefaultPropertyValueFactory();
	}

	@Test
	public void createCollectionForCollectionReturnsArrayList() {
		assertThat(factory.createCollection(Collection.class), instanceOf(ArrayList.class));
	}
	
	@Test
	public void createCollectionForListReturnsArrayList() {
		assertThat(factory.createCollection(List.class), instanceOf(ArrayList.class));
	}
	
	@Test
	public void createCollectionForSortedSetReturnsTreeSet() {
		assertThat(factory.createCollection(SortedSet.class), instanceOf(TreeSet.class));
	}
	
	@Test
	public void createCollectionForSetReturnsLinkedHashSet() {
		assertThat(factory.createCollection(Set.class), instanceOf(LinkedHashSet.class));
	}
	
	@Test
	public void createCollectionForConcreteCollectionReturnsCollection() {
		assertThat(factory.createCollection(HashSet.class), instanceOf(HashSet.class));
	}
	
	@Test
	public void createCollectionForUnknownCollectionThrowsException() {
		thrown.expect(ClientProxyException.class);
		thrown.expectMessage("Unsupported Collection type: java.util.Queue");
		
		factory.createCollection(Queue.class);
	}
	
	@Test
	public void createCollectionForNonCollectionThrowsException() {
		thrown.expect(ClientProxyException.class);
		thrown.expectMessage("Unsupported Collection type: java.lang.Object");
		
		factory.createCollection(Object.class);
	}
}
