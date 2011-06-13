//
// Copyright (C) 2006 United States Government as represented by the
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
package java.io;

import gov.nasa.jpf.FileState;
import java.nio.channels.FileChannel;

/**
 * MJI model class for java.io.RandomAccessFile
 *
 * @author Ivan Mushketik
 */
public class RandomAccessFile implements DataInput, DataOutput {

  private boolean readOnly;
  FileState fileState;
  long filePointer;

  public RandomAccessFile(File file, String mode) throws FileNotFoundException {
    readOnly = parseMode(mode);

    try {

      if (!file.exists()) {
        if (readOnly) {
          throw new FileNotFoundException(file.getCanonicalPath() + "(No such file or directory)");
        } else {
          if (!file.createNewFile()) {
            throw new FileNotFoundException(file.getCanonicalPath() + "(No such file or directory)");
          }
        }
      }

      fileState = file.getFileInfo().getFileState();
      fileState.open();

    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }

  }

  public RandomAccessFile(String name, String mode) throws FileNotFoundException {
    this(new File(name), mode);
  }

  private boolean parseMode(String mode) {
    if (mode.equals("r")) {
      return true;
    }
    if (mode.equals("rw") || mode.equals("rws") || mode.equals("rwd")) {
      return false;
    }

    throw new IllegalArgumentException("Illegal mode \"" + mode +"\" must be one of \"r\", \"rw\", \"rws\", or \"rwd\"");
  }

  public FileChannel getChannel() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public FileDescriptor getFD() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public long getFilePointer() {
    return filePointer;
  }

  public void close() {
    fileState.close();
  }

  public long length () throws IOException {
    return fileState.getLength();
  }

  public void seek (long pos) throws IOException {
    filePointer = pos;    
  }

  public void setLength ( long newLength )throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void readFully(byte[] bytes) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void readFully(byte[] bytes, int i, int i1) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int skipBytes(int i) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean readBoolean() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public byte readByte() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int readUnsignedByte() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public short readShort() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int readUnsignedShort() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public char readChar() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int readInt() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public long readLong() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public float readFloat() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public double readDouble() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String readLine() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public String readUTF() throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void write(int i) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void write(byte[] bytes) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void write(byte[] bytes, int i, int i1) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeBoolean(boolean bln) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeByte(int i) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeShort(int i) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeChar(int i) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeInt(int i) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeLong(long l) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeFloat(float f) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeDouble(double d) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeBytes(String string) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeChars(String string) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void writeUTF(String string) throws IOException {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}

