# Introduction #
This section describes jpf-bfs design.

# Storing state of BFS #
Every File object has a reference to a FileInfo object. FileInfo object stores state of a file on the BFS while File object just stores a path to a file and gives access to BFS. This separation is done because several File objects that refer to a same file can exists in an application simultaneously, but changing a BFS file from any of a File object (delete file, change file rights) should lead to a BFS state change.

![http://i55.tinypic.com/nzhv8l.png](http://i55.tinypic.com/nzhv8l.png)

State of a BFS is stored on a model side. Because of that restoration of a files state done automatically when JPF state is backtracked/restored.

FileState is a separate object that stores such info about file as:
  * Is it a file or a directory
  * Is it exists (wasn't deleted)
  * Access rights
  * Path to a file on the native file system with a content that was written to it file before JPF started
  * Metadata about data that was written to a file

FileState is separated from a FileInfo, because a deleted file can be still opened and used by FileXStream and RandomAccessFile objects. Even more, new file with a same canonical path can be created and used.


# Implementation of read/write operations #
Each FileState object stores a list of data chunks that was written from initial state to a current state. This list is represented by a WriteChunk object.

![http://i52.tinypic.com/sovhuc.png](http://i52.tinypic.com/sovhuc.png)

WriteChunk has following fields:
  * startPos - offset of a written data from the beginning of a file
  * length - length of a written data chunk
  * fileName - name of the file on the native file system where written data chunk was saved.
  * prevChunk - reference to a data chunk that was written before this WriteChunk

List of write chunks is shown of the following image:
![http://i52.tinypic.com/nvthew.png](http://i52.tinypic.com/nvthew.png)

Because all write chunks are stored on the model side, in every state we can get list of data chunks that was written from the initial state to a current state. If other state is restored, SUT will see another data chunks list.

Read implementation is simple. During read operation list of data chunks is iterated. If current data chunk was written to the area of the file that should be read to the buffer and if part of buffer where it's data should be written isn't filled yet, data chunk is written to the buffer. Otherwise next data chunk is examined.
![http://i54.tinypic.com/syl2q9.png](http://i54.tinypic.com/syl2q9.png)


# Implementation of FileDescritor #
FileDescriptor is an interface for implementing file operations FileInputStream, FileOutputStream and RandomAccessFile. There are two ways to get FileDescriptor for a file:
  * call method FileState.open() - this will create a new FileDescriptor
  * call getFD() method in RandomAccessFile, FileInputStream, FileOuputStrea - in this way a FileDescriptor object will be shared (single file pointer, which position can be changed by all objects that use FileDescriptor) and not synchronized.

![http://i51.tinypic.com/9as5lc.png](http://i51.tinypic.com/9as5lc.png)

Because of different file access modes, several different implementations of methods that performs file operations are needed. FileInterface abstract class was created for this purpose. Certain implementation of this class is given to FileDescriptor constructor in FileState.open() method.

# Implementation of race detection #
JPF has a listener (PreciseRaceDetector) to detect states in which race is possible. PreciseRaceDetector works in the following way: when new ChoiceGenerator is set it checks what bytecode instructions will be executed by each running thread, if two or more of these instructions are read/write  or write/write single field a race is found and property is violated.

jpf-bfs uses this implementation to find file operations races, such as: threads write to the same file without synchronization, two threads read/write from the shared descriptor without synchronization.

The implementation is simple. Every FileState object has an int field. When write operation is performed on a file that is represented by a FileState object, this filed is incremented, when read operation in implemented, this field is read. Because of this PreciseRaceDetector can find a race.

Race can be found by PreciseRaceDetector when two threads are reading from file using same FileDescriptor,   because both read operations will change single filePointer field.

# Coding Conventions #
jpf-bfs coding conventions are identical to [JPF coding conventions](http://babelfish.arc.nasa.gov/trac/jpf/wiki/devel/coding_conventions)