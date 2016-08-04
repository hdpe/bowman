package uk.co.blackpepper.sdrclient.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class FileParserTest {

	@Test
	public void parseReturnsAnnotatedClasses() throws Exception {
		Collection<JavaClassSource> source = new FileParser()
				.parse(FileParserTest.class.getResourceAsStream("Source.java"));

		assertThat(getClassNames(source), contains("AnnotatedClass"));
	}

	private static List<String> getClassNames(Collection<JavaClassSource> source) {
		List<String> result = new ArrayList<String>();
		for (JavaClassSource clazz : source) {
			result.add(clazz.getName());
		}
		return result;
	}
}
