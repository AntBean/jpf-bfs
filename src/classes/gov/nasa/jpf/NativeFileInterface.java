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
public class NativeFileInterface implements FileInterface {

  public native void sync();

  public native int read() throws IOException;

  public native int read(byte[] buf, int off, int len) throws IOException;

  public native long skip(long n) throws IOException;

  public native int available() throws IOException;

  public native void write(int b) throws IOException;

  public native void write(byte[] buf, int off, int len) throws IOException;

  public native void close() throws IOException;

  public boolean valid() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void setLength(long newLength) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void seek(long pos) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public long length() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public long getFilePointer() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

}
