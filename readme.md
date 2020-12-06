# d3ad social

> A small, lightweight, Java-based alternative to Twitter.

## Building

You'll need:

* Ant (build script)
* Java JRE (runtime)
* Java JDK (development kit)

To build you should then be able to run `ant` in the root directory.

## Running

This can be achieved by simply running:

    java -jar dist/d3ad.jar -c cfg/default.json

**Note:** You should set your own 512 bit (64 character hex) salt in the
configuration for the server. This of course is a secret and should not be
added to git.

## Contributing

**Source:** Please fork the project and submit a Merge Request.

**Issues:** Please open a ticket with the relevant details for recreating the
issue.
