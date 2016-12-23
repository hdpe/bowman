# HAL Client #

A generic client framework for accessing a JSON+HAL REST API, with automatic link traversal into associated resources.

Extracted from the `sdr-model-gen` Spring Data REST client generation tool.

## Building ##

To build and install into the local Maven repository:

`mvn install`

To run the integration tests:

`mvn verify -PrunITs`

## Usage ##

### Model associations ###

#### Class level ####

* `@RemoteResource(path)` - the path to the remote resource

#### Field level ####

* `@ResourceId` - mark a `java.net.URI` accessor as the resource ID. The underlying field will be populated with the resource URI on retrieval.
* `@LinkedResource` - mark an association accessor as *linked* rather than *inline*.

### Client API ###

The client supports `get`, `getAll`, `post` and `delete` operations. `put`/`patch` are not currently supported.
