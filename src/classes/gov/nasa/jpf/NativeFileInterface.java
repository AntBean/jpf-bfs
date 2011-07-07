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
 * File interface that perform read/write operations with native file system
 * @author Ivan Mushketik
 */
public class NativeFileInterface extends FileInterface {

  // Canonical path of a file on which read/write should be performed
  private String canonicalPath;
  boolean ignoreWriteMode;

  public NativeFileInterface(FileState fileState, boolean ignoreWriteMode) {}

  public native void sync();

  public int read(byte[] buf, int off, int len) throws IOException {
    int read = readNative(buf, off, len);

    if (read > 0) {
      filePos += read;
    }

    return read;
  }

  private native int readNative(byte[] buf, int off, int len) throws IOException;

  public native int available() throws IOException;

  public void write(byte[] buf, int off, int len) throws IOException {
    int written = writeNative(buf, off, len);

    if (written > 0) {
      filePos += written;
    }
    fileState.updateLastModified();
  }

  private native int writeNative(byte[] buf, int off, int len) throws IOException;

  public void close() throws IOException {
    nativeClose();

    // Decrease open counter
    fileState.close();
  }

  private native void nativeClose() throws IOException;

  public native void setLength(long newLength);

  public void seek(long pos) throws IOException {
    filePos = pos;   
  }

  public native long length();

  // We can just return filePos field here, but if we move this method to the peer
  // side we will avoid race detection when one thread write/read and another
  // just read file pointer
  public native long getFilePointer();
}
