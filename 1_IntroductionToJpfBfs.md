# Introduction to jpf-bfs #

This section describes what is Java PathFinder and how (and most importantly for what purpose) it can be can be extended with jpf-bfs.


# What is Java PathFinder #
[Java PathFinder](http://babelfish.arc.nasa.gov/trac/jpf/wiki/WikiStart) undoubtedly is the Swiss army knife of Java software verification.
It is an implementations of Java Virtual Machine that was created in NASA Ames. JPF is extreamly extendable software. Although core JPF project (jpf-core) doesn't give a lot of abilities in the area of software verification, it has numerous extensions that makes possible to:
  * Check actor programs written in the Scala programming language (jpf-actor)
  * Check graphical applications that use java.awt and javax.swing (jpf-awt)
  * Check properties that are set with help of JSR-305 annotations (jpf-aprop)
  * Check for numeric properties like overflow, silent NaN propagation and catastrophic cancellation (jpf-numeric)
  * Check for races and deadlocks in programs that use java.util.concurrent (jpf-concurent)
  * and others


# What is jpf-bfs #

To explain what is jpf-bfs it is necessary to explain how JPF work.
When JPF runs a java program it executes bytecode instructions in it until it reach a state where several possible ways of execution are possible. Usually it's a state with a non-deterministic choice: what thread should be started next, what number should be returned by a method of a Random class. If JPF reaches such state it saves the state of the program, which it executes. This state can be later restored and JPF can try another way of execution.
Theoretically JPF can visit all possible states in program's state change, but during JPF restore operation it restores only JVM state (heap, stack, ...) but state of the file system in a certain state isn't restored.
Following picture show JPF execution.
![http://i54.tinypic.com/kda1iq.jpg](http://i54.tinypic.com/kda1iq.jpg)

jpf-bfs aim is to make all file operations (read, write, file flags changing, creation, deletion, renaming) backtrackable, so programs that operate with file system can be verified.

# jpf-bfs features #
  * File operations are backtrackable
  * User can specify what files for which read/write operations should be executed natively (to avoid jpf-bfs overhead) and for what files write operations should be ignored at all (to avoid overhead for log files)
  * Races during file operations (for example two files read simultaneously from one file descriptor without synchronization) can be found
  * Configurable File.rename(), File.delete() behavior if an opened file is renamed/deleted