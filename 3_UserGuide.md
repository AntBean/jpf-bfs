# Introduction #

This sections describe how to use jpf-bfs after you've [installed](2_InstallingJpfBfs.md) it.

# jpf-bfs examples #

jpf-bfs has several examples that shows what errors can be found by jpf-bfs and what output will it give in each case.

You can take a look at examples for jpf-bfs in:

```
src/examples 
```

Examples that can be found in jpf-bfs sources are:
  * DeleteFileWhenItsOpened - in this example file opened by FileInputStream is deleted. Because of example configuration deleting of opened file leads to an error, but user can specify desired outcome.
  * RenameFileWhenItsOpened - similar to DeleteFileWhenItsOpened, except opened file is renamed.
  * ReadReadFileRace - in this example a single file is read simultaneously without synchronization by two FileInputStreams that sharing single file descriptor. This leads to a non-determinism in a real program and considered as an error.
  * ReadWriteFileRace - in this example one file is simultaneously read and written from two different threads. This can be a source of non-determinism in real applications and considered as an error.
  * FileListenerExample - comparing with previous examples no new error is shown here. It just shows how to configure FileListener to see what file operations lead to an error.

# Running jpf-bfs example #

to run an DeleteFileWhenItsOpened.jpf example do:
```
jpf src/examples/DeleteFileWhenItsOpened.jpf 
```
jpf is a script that can be found in jpf-core/bin and should be in your $PATH variable.
If you run previous command you will receive:
```
JavaPathfinder v6.0 - (C) RIACS/NASA Ames Research Center


====================================================== system under test
application: DeleteFileWhenItsOpened.java

====================================================== search started: 7/20/11 3:17 PM

====================================================== error #1
gov.nasa.jpf.jvm.NoUncaughtExceptionsProperty
java.io.IOException: File /home/proger/Documents/Programming/Java/JPF/jpf-bfs/testFile deleted while opened
        at gov.nasa.jpf.FileInfo.checkDeleteConfig(gov.nasa.jpf.bfs.JPF_gov_nasa_jpf_FileInfo)
        at gov.nasa.jpf.FileInfo.delete(FileInfo.java:145)
        at java.io.File.delete(File.java:278)
        at DeleteFileWhenItsOpened.main(DeleteFileWhenItsOpened.java:39)


====================================================== snapshot #1
thread index=0,name=main,status=RUNNING,this=java.lang.Thread@0,target=null,priority=5,lockCount=0,suspendCount=0
  call stack:
        at gov.nasa.jpf.FileInfo.checkDeleteConfig(FileInfo.java:-1)
        at gov.nasa.jpf.FileInfo.delete(FileInfo.java:145)
        at java.io.File.delete(File.java:278)
        at DeleteFileWhenItsOpened.main(DeleteFileWhenItsOpened.java:39)


====================================================== results
error #1: gov.nasa.jpf.jvm.NoUncaughtExceptionsProperty "java.io.IOException: File /home/proger/Documents/P..."

====================================================== statistics
elapsed time:       0:00:01
states:             new=1, visited=0, backtracked=0, end=0
search:             maxDepth=1, constraints hit=0
choice generators:  thread=1 (signal=0, lock=1, shared ref=0), data=0
heap:               new=439, released=0, max live=0, gc-cycles=0
instructions:       4104
max memory:         47MB
loaded code:        classes=86, methods=1135

```

which shows that an error was found with help of jpf-bfs.

# Where jpf-bfs stores all written data #
To make all file operations backtrackable jpf-bfs, when write operation is performed no data is written into this file. Because of this, after ends it's work content of the files is unchanged. To save what data was written to what file, jpf-bfs stores metatadata for each write operation and a data that was written is saved to a directory specified in a config.

To specify a directory for storing written data chunks, config key jpf-bfs.writecache\_dir is used.

# How to detect races with jpf-bfs #

Race detection in JPF can be done with PreciseRaceDetector which check if at each state two threads can simultaneously perform read/write or write/write operations. jpf-bfs race detection is based on PreciseRaceDetector listener, and can be enabled by adding following lines to an application properties file:
```
# Add listener that finds races
listener+=;gov.nasa.jpf.listener.PreciseRaceDetector
# Add PreciseRaceDetector to properties, to report property violation if a race 
# was found
search.properties+=;gov.nasa.jpf.listener.PreciseRaceDetector
```

# FileListener #
FileListener is a JPF listener that stores all file operations that were performed by SUT from initial state to a current state.
To add FileListener you should add following lines to an application properties file:
```
# Add listener that logs all file system related method calls and report them
# in case of property violation
listener+=;gov.nasa.jpf.listener.FileListener
# Set if constructors calls should be logged
file-listener.log-constructors=false
# Set format of the report output (table or raw)
file-listener.output-format=table
```

# How to avoid jpf-bfs overhead #
Using jpf-bfs gives ability for perform all file operations in a backtrackable way, but it can add severe overhead if application performs a lot of read/write operations (logging for example). jpf-bfs has three categories of files:
  * backtackable files - for these files all read/write operations are performed in backtrackable way. By default all files are backtrackable.
  * bfs.exclude files - for a files in these groups all write operations are unbacktrackble (all data is written directly to a native file system), but file pointer position is backtrackable.
  * bfs.ignore-write - write operations for files in this mode are ignored, read operation can either produce an error or warning.

To add a file into exclude or ignore-write group you can use jpf-bfs.bfs.exclude and jpf-bfs.bfs.ignore-write. Value of any of these keys is an array of regular expessions.

For example, if you add following lines into configuration:
```
jpf-bfs.bfs.exclude = /home/user/myApp/files/*; /home/user/myApp/bigFile
jpf-bfs.bfs.ignore-write = /home/user/myApp/logs/*
```
all files with canonical pathes that starts with '/home/user/myApp/files/' and file '/home/user/myApp/bigFile' will have native write mode and for files with path that starts with '/home/user/myApp/logs/' all write operations will be ignored.

To configure JPF behavior on reading bfs.exclude file jpf-bfs.ignore\_write\_file\_read key should be used. It's values can be:
  * 'error' - produce an error
  * 'warning' - log a warning
  * 'nothing' - just do nothing

# Configuring renamed/deletion opened file #
OS respond to renaming/deletion of operation systems vary on different operation systems. On Windows this will end up with an error, but on Linux you can rename or delete a file and programs that opened it can still perform read/write operations.
To configure what should be jpf-bfs's behavior, you should use jpf-bfs.opened\_delete and jpf-bfs.opened\_rename. Values for these keys can be 'error', 'warning' or 'nothing'.