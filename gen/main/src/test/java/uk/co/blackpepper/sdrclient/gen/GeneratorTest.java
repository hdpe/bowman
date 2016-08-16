package uk.co.blackpepper.sdrclient.gen;

import java.io.IOException;
import java.net.URI;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import uk.co.blackpepper.sdrclient.annotation.IdField;
import uk.co.blackpepper.sdrclient.annotation.LinkedResource;
import uk.co.blackpepper.sdrclient.annotation.RemoteResource;
import uk.co.blackpepper.sdrclient.gen.annotation.RestIgnore;

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

		generator = new Generator(mock(Logger.class));
	}

	@Test
	public void generateWithoutEntityAnnotationDoesNothing() throws IOException {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class)
			.setPackage("test")
			.setName("Entity");

		generator.generate(new RoasterClassSourceAdapter(javaClass), "target.pkg", classWriter);

		verifyZeroInteractions(classWriter);
	}
	
	@Test
	public void generateWritesContent() throws IOException {
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class)
			.setName("Entity");
		javaClass.addAnnotation(Entity.class);
		javaClass.addAnnotation(uk.co.blackpepper.sdrclient.gen.annotation.RestRepository.class)
			.setStringValue("/path/to/resource");
		javaClass.addField()
			.setName("id")
			.addAnnotation(javax.persistence.Id.class);
		javaClass.addField()
			.setName("name")
			.setType(String.class);

		JavaClassSource output = generateAndParseContent(javaClass, "target.pkg");
		
		assertThat("qualifiedName", output.getQualifiedName(), is("target.pkg.Entity"));
		assertThat("has class annotation", output.hasAnnotation(RemoteResource.class), is(true));
		assertThat("has javax.persistence.Id annotation",
			output.getField("id").hasAnnotation(javax.persistence.Id.class), is(false));
		assertThat("has id annotation", output.getField("id").hasAnnotation(IdField.class), is(true));
		assertThat("id field type", output.getField("id").getType().getQualifiedName(), is("java.net.URI"));
		assertThat("id getter", output.getMethod("getId"), is(notNullValue()));
		assertThat("id setter", output.getMethod("setId", URI.class), is(nullValue()));
		assertThat("name getter", output.getMethod("getName"), is(notNullValue()));
		assertThat("name setter", output.getMethod("setName", String.class), is(notNullValue()));
	}
	
	@Test
	public void generateWithGeneratesClientAddsAnnotation() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource();
		javaClass.addAnnotation(uk.co.blackpepper.sdrclient.gen.annotation.RestRepository.class)
			.setStringValue("/path/to/resource");
		
		JavaClassSource output = generateAndParseContent(javaClass);
		
		assertThat("class annotation value", output.getAnnotation(RemoteResource.class).getStringValue(),
				is("/path/to/resource"));
	}

	@Test
	public void generateWritesToRelativePath() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource("pkg.X");
		
		generator.generate(new RoasterClassSourceAdapter(javaClass), "target.pkg", classWriter);
		
		verify(classWriter).write(eq("target/pkg/X.java"), anyString());
	}

	@Test
	public void generateWithAssociationFieldAddsAnnotation() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource("sourcepackage.X");
		javaClass.addField()
			.setName("field")
			.setType("sourcepackage.Y");

		JavaClassSource output = generateAndParseContent(javaClass);
		
		assertThat("field getter has @LinkedResource", output.getMethod("getField").hasAnnotation(LinkedResource.class),
				is(true));
	}

	@Test
	public void generateWithOtherAnnotationDiscardsAnnotation() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource();
		javaClass.addField()
			.setName("field")
			.addAnnotation(Deprecated.class);

		JavaClassSource output = generateAndParseContent(javaClass);
		
		assertThat("field getter has @Deprecated", output.getMethod("getField").hasAnnotation(Deprecated.class),
				is(false));
	}
	
	@Test
	public void generateWithClientResourceTypeConvertsType() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource("sourcepackage.X");
		javaClass.addField()
			.setName("field")
			.setType("sourcepackage.Y");
		
		JavaClassSource output = generateAndParseContent(javaClass, "target.pkg");
		
		assertThat("field type", output.getField("field").getType().getQualifiedName(), is("target.pkg.Y"));
	}
	
	@Test
	public void generateWithGenericClientResourceTypeConvertsType() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource("sourcepackage.X");
		javaClass.addField()
			.setName("field")
			.setType("java.util.Set<sourcepackage.Y>");
		
		JavaClassSource output = generateAndParseContent(javaClass);
		
		assertThat("field type", output.getField("field").getType().getQualifiedNameWithGenerics(),
				is("java.util.Set<Y>"));
		assertThat("import sourcepackage.Y", output.getImport("sourcepackage.Y"), is(nullValue()));
		assertThat("import sourcepackage.client.Y", output.getImport("sourcepackage.client.Y"), is(nullValue()));
	}
	
	@Test
	public void generateWithRestIgnoreFieldIgnoresField() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource();
		javaClass.addField()
			.setName("x")
			.addAnnotation(RestIgnore.class);
		
		JavaClassSource output = generateAndParseContent(javaClass);
		
		assertThat("field x", output.getField("x"), is(nullValue()));
	}
	
	@Test
	public void generateWithTransientFieldIgnoresField() throws IOException {
		JavaClassSource javaClass = createValidJavaClassSource();
		javaClass.addField()
			.setName("x")
			.addAnnotation(Transient.class);
		
		JavaClassSource output = generateAndParseContent(javaClass);
		
		assertThat("field x", output.getField("x"), is(nullValue()));
	}
	
	private JavaClassSource generateAndParseContent(JavaClassSource in) throws IOException {
		return generateAndParseContent(in, "_target._package");
	}
	
	private JavaClassSource generateAndParseContent(JavaClassSource in, String targetPackageName) throws IOException {
		ArgumentCaptor<String> content = ArgumentCaptor.forClass(String.class);

		generator.generate(new RoasterClassSourceAdapter(in), targetPackageName, classWriter);

		verify(classWriter).write(anyString(), content.capture());

		return (JavaClassSource) Roaster.parse(content.getValue());
	}
	
	private static JavaClassSource createValidJavaClassSource() {
		return createValidJavaClassSource("packageName.ClassName");
	}

	private static JavaClassSource createValidJavaClassSource(String fqClassName) {
		String packageName = fqClassName.substring(0, fqClassName.lastIndexOf("."));
		String className = fqClassName.substring(fqClassName.lastIndexOf(".") + 1);
		
		JavaClassSource javaClass = Roaster.create(JavaClassSource.class)
			.setPackage(packageName)
			.setName(className);
		
		javaClass.addAnnotation(Entity.class);
		
		javaClass.addField()
			.setName("id")
			.addAnnotation(IdField.class);
		
		return javaClass;
	}
}
