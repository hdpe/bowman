package uk.co.blackpepper.sdrclient.gen;

import java.io.IOException;
import java.net.URI;

import javax.persistence.Id;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class GeneratorTest {

	private GeneratedClassWriter classWriter;

	private Generator generator;

	@Before
	public void setup() {
		classWriter = mock(GeneratedClassWriter.class);

		generator = new Generator();
	}

	@Test
	public void generateWithoutRestResourceAnnotationDoesNothing() throws IOException {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage("test").setName("Entity");

		generator.generate(new RoasterClassSourceAdapter(javaClass), classWriter);

		verifyZeroInteractions(classWriter);
	}

	@Test
	public void generateWritesFile() throws IOException {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage("test").setName("Entity").addAnnotation(RemoteResource.class)
				.setStringValue("/path/to/resource");
		javaClass.addField().setName("id").addAnnotation(Id.class);
		javaClass.addField().setName("name").setType(String.class);

		ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);

		generator.generate(new RoasterClassSourceAdapter(javaClass), classWriter);

		verify(classWriter).write(eq("test/client/Entity.java"), content.capture());

		JavaClassSource output = (JavaClassSource) Roaster.parse(content.getValue());
		assertThat("qualifiedName", output.getQualifiedName(), is("test.client.Entity"));
		assertThat("has class annotation", output.hasAnnotation(RemoteResource.class), is(true));
		assertThat("class annotation value", output.getAnnotation(RemoteResource.class).getStringValue(),
				is("/path/to/resource"));
		assertThat("has id annotation", output.getField("id").hasAnnotation(Id.class), is(false));
		assertThat("id field type", output.getField("id").getType().getQualifiedName(), is("java.net.URI"));
		assertThat("id getter", output.getMethod("getId"), is(notNullValue()));
		assertThat("id setter", output.getMethod("setId", URI.class), is(nullValue()));
		assertThat("name getter", output.getMethod("getName"), is(notNullValue()));
		assertThat("name setter", output.getMethod("setName", String.class), is(notNullValue()));
	}

	@Test
	public void generateWithClientAnnotationPreservesAnnotation() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource();
		javaClass.addAnnotation(RemoteResource.class).setStringValue("/");
		javaClass.addField().setName("field").addAnnotation(LinkedResource.class);

		ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);

		generator.generate(new RoasterClassSourceAdapter(javaClass), classWriter);

		verify(classWriter).write(anyString(), content.capture());

		JavaClassSource output = (JavaClassSource) Roaster.parse(content.getValue());
		assertThat("field getter has @LinkedResource", output.getMethod("getField").hasAnnotation(LinkedResource.class),
				is(true));
	}

	@Test
	public void generateWithOtherAnnotationDiscardsAnnotation() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource();
		javaClass.addAnnotation(RemoteResource.class).setStringValue("/");
		javaClass.addField().setName("field").addAnnotation(Deprecated.class);

		ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);

		generator.generate(new RoasterClassSourceAdapter(javaClass), classWriter);

		verify(classWriter).write(anyString(), content.capture());

		JavaClassSource output = (JavaClassSource) Roaster.parse(content.getValue());
		assertThat("field getter has @Deprecated", output.getMethod("getField").hasAnnotation(Deprecated.class),
				is(false));
	}

	private static JavaClassSource createValidJavaClassSource() {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class).setName("X").setPackage("x");
		javaClass.addField().setName("id").addAnnotation(Id.class);
		return javaClass;
	}
}
