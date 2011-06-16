//
// Copyright (C) 2011 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
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

package gov.nasa.jpf;

import java.io.IOException;

/**
 *
 * @author Ivan Mushketik
 */
public class BFSFileInterface implements FileInterface {

  private FileState fileState;
  private boolean isOpened;
  private long filePos;

  BFSFileInterface(FileState fileState) {
    this.fileState = fileState;
    isOpened = true;
  }

  public void sync() {
    // Nothing to do. BFS is always sync
  }

  public int read() throws IOException {
    byte[] aByte = new byte[1];
    int read = fileState.read(filePos, aByte, 0, 1);

    if (read == 1) {
      filePos++;
      return aByte[0];
    }

    return -1;
  }

  public int read(byte[] buffer, int off, int len) {
    if (filePos < fileState.getLength()) {
      int read = fileState.read(filePos, buffer, off, len);
      filePos += read;

      return read;
    }

    return -1;
  }

  public long skip(long shift) throws IOException {
    System.out.println("BFSFI.skip()");
    if (shift + filePos > fileState.getLength()) {
      filePos = fileState.getLength();
      System.out.println("Over boundaries jump");

    } else {
      filePos = filePos + shift;
      System.out.println("In file boundaries");
    }

    return shift;
  }

  public int available() throws IOException {
    return (int) (fileState.getLength() - filePos);
  }

  public void write(int b) throws IOException {
    byte toWrite = (byte) b;
    byte[] writeBuff = new byte[] {toWrite};

    write(writeBuff, 0, 1);
  }

  public void write(byte[] buf, int off, int len) throws IOException {
    if (isOpened) {
      int written = fileState.write(filePos, buf, off, len);
      filePos += written;
    } else {
      throw new IOException("Attempt to write to closed stream");
    }
  }

  public void close() throws IOException {
    fileState.close();
    isOpened = false;
  }

  public boolean valid() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setLength(long newLength) {
    fileState.setLength(newLength);
  }

  public void seek(long pos) {
    filePos = pos;
  }

  public long length() {
    return fileState.getLength();
  }

  public long getFilePointer() {
    return filePos;
  }



}
