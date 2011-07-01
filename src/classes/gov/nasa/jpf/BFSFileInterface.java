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
public class BFSFileInterface extends FileInterface {

  private FileState fileState;

  BFSFileInterface(FileState fileState) {
    this.fileState = fileState;
  }

  public void sync() {
    // Nothing to do. BFS is always sync
  }

  public int read(byte[] buffer, int off, int len) throws IOException {
    if (filePos < fileState.getLength()) {
      int read = fileState.read(filePos, buffer, off, len);
      filePos += read;

      return read;
    }

    return -1;
  }

  public int available() throws IOException {
    return (int) (fileState.getLength() - filePos);
  }

  public void write(byte[] buf, int off, int len) throws IOException {
      int written = fileState.write(filePos, buf, off, len);
      filePos += written;
  }

  public void close() throws IOException {
    fileState.close();
  }

  // <2do> If new length is far more then current length this will create
  // huge array of zeros.
  // Maybe it's better to move setLength to peer side and avoid this array creation
  public void setLength(long newLength) throws IOException {
    long fileLength = fileState.getLength();
    if (fileLength < newLength) {
       int delta = (int) (newLength - fileLength);
       byte[] zeros = new byte[delta];
       fileState.write(fileLength, zeros, 0, delta);
    }

    fileState.setLength(newLength);
  }

  public void seek(long pos) throws IOException {
    filePos = pos;
  }

  public long length() {
    return fileState.getLength();
  }

  public long getFilePointer() {
    return filePos;
  }
}
