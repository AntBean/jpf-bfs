//
// Copyright  (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
//  (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
//  (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package java.io;

import gov.nasa.jpf.FileInterface;
import gov.nasa.jpf.FileOperations;
import gov.nasa.jpf.FileState;

/**
 * FileDescriptor - interface to a FileInterface instance that implements
 * methods to access files on a file system. *
 * 
 */
public class FileDescriptor {

  private FileInterface fileInterface;
  private FileState fileState;
  
  public FileDescriptor(FileInterface fi, FileState fileState) {
    fileInterface = fi;
    this.fileState = fileState;
  }
  
  public boolean valid () {
    return fileInterface.valid();
  }
  
  public void close () throws IOException {
    fileInterface.close();
  }

  public void sync() {
    fileInterface.sync();
  }
  
  int read () throws IOException {
    if (fileState.getOpenCnt() > 1) {
      System.out.println("[MY LISTENER] Read when openCnt > 1");
    }
    fileState.markRead();
    return fileInterface.read();
  }

  int read (byte[] buf, int off, int len) throws IOException {
    fileState.markRead();
    return fileInterface.read(buf, off, len);
  }

  long skip(long n) throws IOException {
    return fileInterface.skip(n);
  }

  int available () throws IOException {
    return fileInterface.available();
  }
  
  void write (int b) throws IOException {
    if (fileState.getOpenCnt() > 1) {
      System.out.println("[MY LISTENER] Write when openCnt > 1");
    }
    fileState.markWrite(FileOperations.WRITE);
    fileInterface.write(b);
  }

  void write (byte[] buf, int off, int len) throws IOException {
    fileState.markWrite(FileOperations.WRITE);
    fileInterface.write(buf, off, len);
  }

  void setLength(long newLength) {
    fileState.markWrite(FileOperations.WRITE);
    fileInterface.setLength(newLength);
  }

  void seek(long pos) {
    fileInterface.seek(pos);
  }

  long length() {
    return fileInterface.length();
  }

  long filePointer() {
    return fileInterface.getFilePointer();
  }
}
