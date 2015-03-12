# Installation of jpf-bfs #

This section describes, how to install jpf-bfs, how to run it's regression tests and how to run an example with it.

# Preparations #

jpf-bfs is an extension for Java PathFinder (JPF), so to install it, you first need to download and install JPF. JPF installation process is straightforward and explained in details [here](http://babelfish.arc.nasa.gov/trac/jpf/wiki/install/start) on the JPF official site.

# Installing from sources #

To install jpf-bfs from source you need to clone jpf-bfs repository and add jpf-bfs extension to your .jpf/site.properties file (it should be created during JPF installing process).

Clone repository:
```
hg clone https://code.google.com/p/jpf-bfs/ jpf-bfs 
```

Then we need to add jpf-bfs extension to .jpf/site.properties:
```
vim ~/.jpf/site.properties
```

This is how site.properties should look like:
```
jpf-home=/path/to/jpf-core

jpf-core = ${jpf-home}/jpf-core

jpf-bfs = ${jpf-home}/jpf-bfs
extensions+=,${jpf-core},${jpf-bfs}
```

To build jpf-bfs write:
```
bin/ant build 
```

To build and run unit-tests write:
```
bin/ant test 
```

In both cases you would see some output and then:
```
BUILD SUCCESSFUL
Total time: 1 minute 14 seconds
```

which means that jpf-bfs was successfully build.