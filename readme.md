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
* [ ] Implement HashMap that is more robust to collisions.
* [ ] Check hash Strings are valid before using them for disk lookups
* [ ] Define ASCII only in HTML document header
* [ ] Converted URLs should only be HTTPS
* [ ] Test for hash collision
* [ ] Token timeout renew on usage

### Performance

* [ ] Post cool-down (should take some seconds at least between posts)
* [ ] Prevent double post (with something like refresh)
* [ ] JSON parser should use a global HashMap to save on memory - this approach
already failed once.
* [ ] JSON set should offer the ability to add objects in the place of values
* [ ] Use JSON data structure in RAM instead of classes for quicker saving, etc
* [ ] Compress the JSON files saved on disk (https://github.com/lz4/lz4-java)
* [ ] Integer class
  * [ ] Use BASE64 strings
* [ ] String class
  * [ ] 7-bit ASCII look-up table parse
  * [ ] Single parse in-place formatter

### Usability

* [ ] Label user inputs with input validation requirements
* [ ] Tagging other users should add the post to their timeline
* [ ] Self moderation:
  * [ ] Post edit (own)
  * [ ] Post delete (own)
  * [ ] Post flag (any)
* [ ] Admin role moderation:
  * [ ] Delete post (any)
* [ ] Single post view
  * [ ] Embed link
  * [ ] Embed code
