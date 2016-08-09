# Spring Data REST Client #

A client-side data model generator and client framework for a JPA Spring Data REST-provided API.

Primarily intended to provide a convenient means for the setup and tear-down of data for use in automated acceptance tests.

## Quickstart ##

### Creating an application ###

Create a new folder `greeting/` for your project.

Create a new Maven `pom.xml` with the following content:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>

	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.4.0.RELEASE</version>
	</parent>

	<groupId>greeting</groupId>
	<artifactId>app</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<properties>
		<sdr-client.version>0.0.1-SNAPSHOT</sdr-client.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>uk.co.blackpepper.sdrclient</groupId>
			<artifactId>sdr-client</artifactId>
			<version>${sdr-client.version}</version>
		</dependency>
		<dependency>
			<groupId>uk.co.blackpepper.sdrclient</groupId>
			<artifactId>sdr-client-annotation</artifactId>
			<version>${sdr-client.version}</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-jpa</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-data-rest</artifactId>
		</dependency>

		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
		</dependency>
	</dependencies>

</project>
```

Add the following source files:

`src/main/java/greeting/App.java`:

```java
@SpringBootApplication
public class App extends RepositoryRestConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}

	@Override
	public void configureJacksonObjectMapper(ObjectMapper objectMapper) {
		objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE)
			.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
	}
}
```

`src/main/java/greeting/model/Greeting.java`:

```java
@Entity
@uk.co.blackpepper.sdrclient.annotation.RemoteResource("/greetings")
public class Greeting {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String message;
}
```

`src/main/java/greeting/repository/GreetingRepository.java`:

```java
public interface GreetingRepository extends CrudRepository<Greeting, Integer> {
}
```

and run the `App` class to verify your application starts successfully.

`2016-08-08 14:40:09.479  INFO 24824 --- [           main] greeting.App                             : Started App in 3.737 seconds (JVM running for 3.95)`

### Generating the client data model ###

Add the following to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>uk.co.blackpepper.sdrclient</groupId>
      <artifactId>sdr-client-gen-maven-plugin</artifactId>
      <version>${sdr-client.version}</version>
      <executions>
        <execution>
          <goals>
            <goal>generate</goal>
          </goals>
          <configuration>
            <packageName>greeting.model</packageName>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

Rebuild your application and you should see the following file has been generated in `target/generated-sources/sdrclient/greeting/model/client`:

```java
package greeting.model.client;

import uk.co.blackpepper.sdrclient.gen.annotation.RemoteResource;
import java.net.URI;
import com.fasterxml.jackson.annotation.JsonIgnore;

@RemoteResource("/greetings")
public class Greeting {

	private URI id;
	private String message;

	@JsonIgnore
	public URI getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
```

### Invoke the client ###

Add JUnit to your pom's dependencies section:

```xml
<dependency>
  <groupId>junit</groupId>
  <artifactId>junit</artifactId>
  <scope>test</scope>
</dependency>
```

And add the following test at `src/test/java/greeting/GreetingTest.java`:

```java
public class GreetingTest {

	@Test
	public void testClient() {
		ClientFactory factory = new ClientFactory(URI.create("http://localhost:8080"));
		Client<Greeting> client = factory.create(Greeting.class);

		Greeting entity = new Greeting();
		entity.setMessage("hello world!");

		URI location = client.post(entity);

		assertThat(client.get(location).getMessage(), is("hello world!"));
	}
}
```

(ensuring you import the *generated* `Greeting` class from `greeting.model.client`.) Ensure the server application is running, and run the test to verify it passes!

## But, in real life... ##

Obviously the above example is pretty contrived, as the same result could be achieved just by using the original `@Entity` class and Spring's `RestTemplate`. See the tests in `sdr-client-test-it` for the more worthwhile demonstrations of this tool described below.

### Assocations ###

 The power comes when associations between entities are introduced - when navigating entity associations via the client data model accessors, HTTP requests will be issued to lazily retrieve the associated objects, transparently navigating the HATEOAS links in the returned HAL JSON.

### Data model artifact ###

The other primary motivation for this tool is so that a data model can be generated for use by a client application that does *not* have a dependency on the server application code. For this, it is suggested that you generate the data model into a separate artifact, which imports the server project as an *optional* dependency so it is not resolved by the client application transitively. See the `sdr-client-test-client` module for an example of this.

## Usage ##

### Entity assocations ###

#### Class level ####

* `@RemoteResource(path)` - the path to the remote repository: *Required* for entities with repositories

#### Field level ####

* `@LinkedResource` - mark a collection- or single-valued assocation to be a linked assocation, that is, to an entity that has a repository
* `@EmbeddedResource` - mark a single-valued association to be an embedded association, that is, to an entity that does not have a repository
* `@EmbeddedResources` - mark a collection-valued assocation to be an embedded assocation

### Client API ###

The client supports `get`, `post` and `delete` operations - it is not intended to support `put`/`patch` at this time.

## Limitations ##

ID fields (PKs) must be generated in the database (annotated with `@GeneratedValue`). Currently ID fields must also be named `id`.

The tool assumes that it is the *fields* of the server-side entities that should be the basis of the generated artifacts, not the properties, so Spring Data REST must have its `ObjectMapper` configured to use field- rather than property-level access. This is an intentional design decision - the fields comprise the state of the entity, and accessors/mutators may not be present or not directly pass through to the underlying fields.

## Roadmap ##

There are plenty of things still to do with this:

* support ID fields with any name 
* back-references to embedded associations' contexts should be supported
* transient fields should be supported
* support generation from more than one package
* test coverage needs to be hardened up significantly
* investigate whether it's possible to do away with the user annotations in `sdr-client-annotation` and derive this data from the Spring Data REST repository model
* improve flaky m2e integration
* ...
