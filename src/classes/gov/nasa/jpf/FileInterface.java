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
 * Interface to file that is independent from underlying file access implementation
 * @author Ivan Mushketik
 */
public interface FileInterface {

  /**
   * Sync buffers with underlying file system.
   */
  public void sync();

  /**
   * Read single byte from a file
   * @return byte's value if it can be read or -1 otherwise.
   * @throws IOException
   */
  public int read () throws IOException;

  /**
   * Read file content to a buffer
   * @param buf - buffer to fill
   * @param off - offset in a buffer
   * @param len - length of a space in a buffer
   * @return number of read bytes
   */
  public int read (byte[] buf, int off, int len) throws IOException;

  /**
   * Skip bytes in a file
   * @param n - number of bytes to skip
   * @return number of skiped bytes
   * @throws IOException
   */
  public long skip(long n) throws IOException;

  /**
   * Return number of bytes that can be read from a file
   * @return number of bytes that can be read
   * @throws IOException
   */
  public int available () throws IOException;

  /**
   * Write single byte to a file
   * @param b - byte to write
   * @throws IOException
   */
  public void write (int b) throws IOException;

  /**
   * Write buffer's content to a file
   * @param buf - buffer with data
   * @param off - offset in a buffer
   * @param len - length of a data in buffer
   * @throws IOException
   */
  public void write (byte[] buf, int off, int len) throws IOException;

  /**
   * Close file
   * @throws IOException
   */
  public void close () throws IOException;

  public boolean valid();

  /**
   * Set new file length
   * @param newLength - new file length
   */
  public void setLength(long newLength);

  /**
   * Set file pointer.
   * @param pos - new file pointer position
   */
  public void seek(long pos);

  /**
   * Get file's length
   * @return
   */
  public long length();

  /**
   * Get current position of file pointer
   * @return file pointer position
   */
  public long getFilePointer();
}
