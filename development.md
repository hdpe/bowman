## Clone & IDE Setup (Eclipse) ##

If you use Eclipse, clone the project into a named or subdirectory of your workspace to work around [this](https://bugs.eclipse.org/bugs/show_bug.cgi?id=375073) [limitation](https://bugs.eclipse.org/bugs/show_bug.cgi?id=40493):

`git clone git@github.com:BlackPepperSoftware/hal-client.git hal-client-parent/`

Then import with `File -> Import -> Maven -> Existing Maven Projects`.

## Building ##

To build and install into the local Maven repository:

`mvn install`

To run the integration tests:

`mvn verify -PrunITs`