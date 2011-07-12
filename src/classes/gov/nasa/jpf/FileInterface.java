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
 * This class defines interface to possible file operations, so they can be performed
 * by FileDescriptor in implementation independent way.
 * @author Ivan Mushketik
 */
public abstract class FileInterface {
  // File pointer position
  protected long filePos;
  // FileState object that represents a file that is used to perform file operations
  // by FileInterface implementations
  protected FileState fileState;
  
  /**
   * Sync buffers with underlying file system.
   */
  public abstract void sync();

  /**
   * Read single byte from a file
   * @return byte's value if it can be read or -1 otherwise.
   * @throws IOException
   */
  public int read () throws IOException {
    byte[] aByte = new byte[1];
    int read = read(aByte, 0, 1);

    if (read == 1) {
      return aByte[0]  & 0xFF;
    }

    return -1;
  }

  /**
   * Read file content to a buffer
   * @param buf - buffer to fill
   * @param off - offset in a buffer
   * @param len - length of a space in a buffer
   * @return number of read bytes
   */
  public abstract int read (byte[] buf, int off, int len) throws IOException;

  /**
   * Skip bytes in a file
   * @param n - number of bytes to skip
   * @return number of skiped bytes
   * @throws IOException
   */  
  public long skip(long shift) throws IOException {
    long oldFilePos = filePos;
    long fileLength = length();

    if (shift + filePos > fileLength) {
      filePos = fileLength;

    } else {
      filePos = filePos + shift;
    }

    return filePos - oldFilePos;
  }

  /**
   * Return number of bytes that can be read from a file
   * @return number of bytes that can be read
   * @throws IOException
   */
  public abstract int available () throws IOException;

  /**
   * Write single byte to a file
   * @param b - byte to write
   * @throws IOException
   */
  public void write(int b) throws IOException {
    byte[] aByte = new byte[1];
    aByte[0] = (byte) b;

    write(aByte, 0, 1);
  }

  /**
   * Write buffer's content to a file
   * @param buf - buffer with data
   * @param off - offset in a buffer
   * @param len - length of a data in buffer
   * @throws IOException
   */
  public abstract void write(byte[] buf, int off, int len) throws IOException;

  /**
   * Close file
   * @throws IOException
   */
  public abstract void close () throws IOException;

  /**
   * Set new file length
   * @param newLength - new file length
   */
  public abstract void setLength(long newLength) throws IOException;

  /**
   * Set file pointer.
   * @param pos - new file pointer position
   */
  public abstract void seek(long pos) throws IOException;

  /**
   * Get file's length
   * @return
   */
  public abstract long length();

  /**
   * Get current position of file pointer
   * @return file pointer position
   */
  public abstract long getFilePointer();
}
