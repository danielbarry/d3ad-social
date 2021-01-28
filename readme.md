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

For running on a server, you may also use the bash script contain in
`cycle.sh`, please read this small script to understand how it works.

## Contributing

**Source:** Please fork the project and submit a Merge Request.

**Issues:** Please open a ticket with the relevant details for recreating the
issue.

## TODO

These items are yet to be completed:

### Security

* [ ] Login delay (prevent brute force approaches)
* [ ] Use thread pool to handle clients to prevent memory exhaustion

### Performance

* [ ] Post cool-down (should take some seconds at least between posts)
* [ ] Prevent double post (with something like refresh)
* [ ] JSON parser should use a global HashMap to save on memory
* [ ] Prepare StringBuilder with estimated size of output String

### Usability

* [ ] Tagging over users should add the post to their timeline
