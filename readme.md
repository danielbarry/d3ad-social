# d3ad social

> A small, lightweight, Java-based alternative to Twitter.

## Building

You'll need:

* Ant (build script)
* Java JRE (runtime - currently tested with Java 11)
* Java JDK (development kit - currently tested with Java 11)

To build you should then be able to run `ant` in the root directory.

## Running

This can be achieved by simply running:

    java -jar dist/d3ad.jar -c cfg/default.json

### Configuration

You should copy `cfg/default.json` to `cfg/custom.cfg` and make your changes
there.

Firstly, you should set your own 512 bit (64 character hex) salt in the
configuration for the server. This of course is a secret and should not be
added to git.

### Auto-update

For running on a server, you may also use the bash script contain in
`cycle.sh`, please read this small script to understand how it works.

Essentially it monitors the head of a given branch and auto-pulls in changes,
rebooting the server. It is recommended that you fork the main repository and
pull in changes after testing them.

### Backups

By default all the data is stored in `dat/`. All the files in here are human
readable JSON. A simple form of backup you can perform is to simply copy all of
these files to another location.

### Roles

To set a user to the `ADMIN` role, currently you need to change this manually
in the configuration and reboot the server to load the user into cache.

## Contributing

**Source:** Please fork the project and submit a Merge Request.

**Issues:** Please open a ticket with the relevant details for recreating the
issue.

## TODO

These items are yet to be completed:

### Security

* [ ] Implement HashMap that is more robust to collisions.
* [ ] Check hash Strings are valid before using them for disk lookups
* [ ] Define ASCII only in HTML document header
* [ ] Converted URLs should only be HTTPS
* [ ] Test for hash collision
* [ ] Token timeout renew on usage

### Performance

* [ ] Post cool-down (should take some seconds at least between posts)
* [ ] JSON parser should use a global HashMap to save on memory - this approach
already failed once.
* [ ] JSON set should offer the ability to add objects in the place of values
* [ ] Use JSON data structure in RAM instead of classes for quicker saving, etc
* [ ] Compress the JSON files saved on disk (https://github.com/lz4/lz4-java)
* [ ] String class
  * [ ] 7-bit ASCII look-up table parse
  * [ ] Single parse in-place formatter

### Usability

* [ ] Self moderation:
  * [ ] Post flag (any)
* [ ] Admin:
  * [ ] View list of flags
  * [ ] Disable user account
