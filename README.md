# HAL Client #

A Java library for accessing a JSON+HAL REST API, supporting the mapping of a client-side
model to HTTP resources with automatic link traversal into associated resources.

The motivation for this library was to make it easier to write clients for Spring Data
REST-exposed JPA repositories, supporting lazy-loading of associations in a similar style
to JPA.

## Building ##

To build and install into the local Maven repository:

`mvn install`

To run the integration tests:

`mvn verify -PrunITs`

## Add to your project ##

Add the Maven dependency:

```
<dependency>
	<groupId>uk.co.blackpepper</groupId>
	<artifactId>hal-client</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Usage ##

Given the following annotated model objects:

```java
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

@RemoteResource("/people")
public class Person {

	private URI id;	
	private String name;
	
	public Person() {}
	public Person(String name) { this.name = name; }

	@ResourceId	public URI getId() { return id; }
	public String getName() { return name; }
}
```

and

```java
import uk.co.blackpepper.halclient.annotation.LinkedResource;
import uk.co.blackpepper.halclient.annotation.RemoteResource;
import uk.co.blackpepper.halclient.annotation.ResourceId;

@RemoteResource("/greetings")
public class Greeting {
	
	private URI id;
	private Person recipient;
	private String message;

	public Greeting() {}
	public Greeting(String message, Person recipient)
		{ this.message = message; this.recipient = recipient; }
	
	@ResourceId public URI getId() { return id; }
	@LinkedResource public Person getRecipient() { return recipient; }
	public String getMessage() { return message; }
}
```

Client instances can be constructed and used as demonstrated below.

The HTTP requests/responses corresponding to each instruction are shown in a comment
beneath. 


```java
import uk.co.blackpepper.halclient.Client;
import uk.co.blackpepper.halclient.ClientFactory;
import uk.co.blackpepper.halclient.Configuration;

...

ClientFactory factory = Configuration.builder().setBaseUri("http://...").build()
		.buildClientFactory();

Client<Person> people = factory.create(Person.class);
Client<Greeting> greetings = factory.create(Greeting.class);

URI id = people.post(new Person("Bob"));
// POST /people {"name": "Bob"}
//  -> Location: http://.../people/1

Person recipient = people.get(id);
// GET /people/1
//  -> {"name": "Bob", "_links": {"self": {"href": "http://.../people/1"}}}

assertThat(recipient.getName(), is("Bob"));

id = greetings.post(new Greeting("hello", recipient));
// POST /greetings {"message": "hello", "recipient": "http://.../people/1"}}
//  -> Location: http://.../greetings/1

Greeting greeting = greetings.get(id);
// GET /greetings/1
//  -> {"message": "hello", "_links": {"self": {"href": "http://.../greetings/1"},
// 			"recipient": {"href": "http://.../people/1"}}}

assertThat(greeting.getMessage(), is("hello"));

recipient = greeting.getRecipient();
// GET /people/1
//  -> {"name": "Bob", "_links": {"self": {"href": {"http://.../people/1"}}}

assertThat(recipient.getName(), is("Bob"));
```

## API Usage ##

### Model associations ###

#### Class level ####

* `@RemoteResource(path)` - the path to the remote resource

#### Field level ####

* `@ResourceId` - mark a `java.net.URI` accessor as the resource ID. The underlying field will be populated with the resource URI on retrieval.
* `@LinkedResource` - mark an association accessor as *linked* rather than *inline*.

### Client API ###

The client supports `get`, `getAll`, `post` and `delete` operations. `put`/`patch` are not currently supported.