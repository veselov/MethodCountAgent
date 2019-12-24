# MethodCountAgent

## Purpose

This software offers a mechanism to count method invocation of certain classes. It is supposed to be useful for debugging
certain memory leak problems, like that discussed in https://stackoverflow.com/questions/14370738

The software is a JVM agent that must be loaded into a JVM to intercept the constructor invocations of classes
configured by being matched against a predefined set of regular expressions.

This software is not release ready, it's meant as a hacking tool, and haven't been used in a while
by its creator. It's not going to be easy to use.

## Building

After checkout, build the software using

```bash
mvn assembly:assembly -DdescriptorId=jar-with-dependencies
```

This will produce `target/MethodCountAgent-<ver>-jar-with-dependencies.jar`

## Preparation

To be able to use the method counting agent, a list of patterns for the classes to be instrumented
must be prepared in some file. Let's assume it's in some file at `<pattern_list>`. Each line of this file
needs to be a Java regex pattern. Make sure to not have any of Java system classes match the pattern,
it will cause JVM to crash.

## Staring the agent

The agent should be added to running JVM using ```-javaagent:/path/to/MethodCountAgent/target/MethodCountAgent-1.0-SNAPSHOT-jar-with-dependencies.jar=rf:XXX,lf:YYY,lp:ZZZ```

* rf:XXX - XXX is path to `<pattern_list>`, the file must exist, and `rf` parameter must be provided
* lf:YYY - YYY is path to a log file, useful to determining issues with the agent itself. File will be erased, parameter mast be provided
* lp:ZZZ - ZZZ is the TCP port number to listen to commands on. If not specified, default of 7455 is used.

## Using the agent

Check the log file to verify agent is successfully instrumenting the classes. List of instrumented and skipped classes
will be provided, along with any exceptions that the agent encounters.

The agent can be connected to using a simple TCP client on to the configured port (see above).
Using `telnet` or `nc` will work fine.

There are 3 commands that the agent will respond to:
* `print` - prints the current method invocation counts since latest reset
* `reset` - resets method invocation counts
* `flush` - print and reset method invocation counts right away (using consecutive flush will guarantee the counts to be exact)

Additionally, empty line will echo so you know that the JVM is still responding. A word "executed" is printed if no invocation were counted since last reset.

Example session:

```bash
$ nc localhost 7440
ready>
print
1   org/example/App.main([Ljava/lang/String;)V
--
flush
1   org/example/App.main([Ljava/lang/String;)V
--
flush 
executed
```