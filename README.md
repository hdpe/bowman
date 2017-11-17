# Bowman #

[![Build Status](https://travis-ci.org/BlackPepperSoftware/bowman.svg?branch=master)](https://travis-ci.org/BlackPepperSoftware/bowman)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.co.blackpepper.bowman/bowman-client/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.co.blackpepper.bowman/bowman-client)

A Java library for accessing a JSON+HAL REST API, supporting the mapping of a client-side
model to HTTP resources with automatic link traversal into associated resources.

The motivation for this library was to make it easier to write clients for Spring Data
REST-exposed JPA repositories, supporting lazy-loading of associations in a similar style
to JPA.

Built on [Spring HATEOAS](http://projects.spring.io/spring-hateoas/) and [Jackson](https://github.com/FasterXML/jackson).

## Add to your project ##

Add the Maven dependency:

```
<dependency>
	<groupId>uk.co.blackpepper.bowman</groupId>
	<artifactId>bowman-client</artifactId>
	<version>{release version}</version>
</dependency>
```

## Usage Example ##

Given the following annotated model objects:

```java
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

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
import uk.co.blackpepper.bowman.annotation.LinkedResource;
import uk.co.blackpepper.bowman.annotation.RemoteResource;
import uk.co.blackpepper.bowman.annotation.ResourceId;

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
import uk.co.blackpepper.bowman.Client;
import uk.co.blackpepper.bowman.ClientFactory;
import uk.co.blackpepper.bowman.Configuration;

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

### Client API ###

Clients are created through `ClientFactory.create(clazz)`. ClientFactory instances are created through `Configuration.builder().getClientFactory()` with the configuration builder allowing various further configuration.

Clients support:

* `get(URI id)` - GET the item with the given ID
* `getAll()` - GET all items from the collection resource
* `getAll(URI location)` - GET all items from the given endpoint
* `post(T object)` - POST the item to the collection resource
* `put(T object)` - PUT the item to its resource
* `delete(URI id)` - DELETE the item with the given ID

PUT is supported with caveats: there is currently a whole category of Spring Data REST limitations interacting via PUT/PATCH with JPA repositories due to attempts to replace persistent collections and state merge occurring outside of a transaction.

### Model Classes ###

Annotate your model classes with `@RemoteResource(path)`. `path` is the path of the class's collection resource, relative to the base URI set when building the `ClientFactory`.

```java
@RemoteResource("/things")
public class Thing { ... }
```

#### ID Property ####

Use `@ResourceId` to mark a `java.net.URI` accessor as the resource ID. This is the canonical URI for the resource - its 'self' link.

```java
private URI id;

@ResourceId public URI getId() { return id; }
```

#### Value Properties ####

Simple properties (Strings, primitives) will be mapped to JSON automatically.

#### Linked Resources ####

Mark a resource as *linked* with `@LinkedResource` on its accessor. Invoking this accessor will automatically query its associated linked remote resource to populate the model.

```java
private Related related;
private Set<Related> relatedSet = new HashSet<>();

@LinkedResource public Related getRelated() { return related; }
@LinkedResource public Set<Related> getRelatedSet() { return relatedSet; }
```

#### Inline Resources ####

Mark a resource as *inline* with the `InlineAssociationDeserializer` Jackson deserializer. Invoking this accessor will create and return a proxy that is aware of the inline object's links, and so is able to resolve nested linked resources.

```java
private Related related;
private Set<Related> relatedSet = new HashSet<>();

@JsonDeserialize(using = InlineAssociationDeserializer.class)
public Related getRelated() { return related; }

@JsonDeserialize(contentUsing = InlineAssociationDeserializer.class)
public Set<Related> getRelatedSet() { return relatedSet; }
```

#### Embedded Resources ####

Subresources are loaded from the `_embedded` property of a HAL response when querying a collection resource. For single-valued resources, embedded resources are currently disregarded: PRs welcome!

## Development ##

* [Development Guide](./development.md)
