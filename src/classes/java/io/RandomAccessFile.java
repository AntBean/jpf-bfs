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
package java.io;

import gov.nasa.jpf.FileState;
import java.nio.channels.FileChannel;

/**
 * Implementation of RandomAccessFile that can read/write data from/to BFS.
 *
 * @author Ivan Mushketik
 */
public class RandomAccessFile implements DataInput, DataOutput {

  private FileDescriptor fd;
  private boolean readOnly;

  public RandomAccessFile(File file, String mode) throws FileNotFoundException {
    readOnly = parseMode(mode);

    try {

      if (!file.exists()) {
        if (readOnly) {
          throw new FileNotFoundException(file.getCanonicalPath() + " (No such file or directory)");
        } else {
          if (!file.createNewFile()) {
            throw new FileNotFoundException(file.getCanonicalPath() + " (No such file or directory)");
          }
        }
      }     
      
      if (file.isDirectory()) {
        throw new FileNotFoundException(file.getCanonicalPath() + " (is directory)");
      }
      
      FileState fileState = file.getFileInfo().getFileState();
      checkFilePermissions(file, fileState);
      
      fd = fileState.open();

    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }
  }
  
  private void checkFilePermissions(File file, FileState fileState) throws IOException, FileNotFoundException {
    if (readOnly) {
      if (!fileState.isReadableForSUT()) {
        throw new FileNotFoundException(file.getCanonicalPath() + " (Permission denied)");
      }
    } else {
      if (!fileState.isReadableForSUT() || !fileState.isWritableForSUT()) {
        throw new FileNotFoundException(file.getCanonicalPath() + " (Permission denied)");
      }
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
    return fd;
  }

  public long getFilePointer() throws IOException {
    return fd.filePointer();
  }

  public void close() throws IOException {
    fd.close();
  }

  public long length() throws IOException {
    return fd.length();
  }

  public void seek(long pos) throws IOException {
    fd.seek(pos);
  }

  public void setLength(long newLength) throws IOException {
    fd.setLength(newLength);
  }

  public int read() throws IOException {
    return fd.read();
  }

  public int read(byte [] bytes, int off, int len) throws IOException {
    return fd.read(bytes, off, len);
  }

  public int read(byte [] bytes) throws IOException {
    return read(bytes, 0, bytes.length);
  }

  public void readFully(byte[] bytes) throws IOException {
    readFully(bytes, 0, bytes.length);
  }

  public void readFully(byte[] bytes, int off, int len) throws IOException {
    int read = read(bytes, off, len);
    if (read != len) {
      throw new EOFException("End of file during read");
    }
  }

  public int skipBytes(int toSkip) throws IOException {
    return (int) fd.skip(toSkip);
  }

  public boolean readBoolean() throws IOException {
    int ch = read();
    if (ch < 0) {
      throw new EOFException("End of file");
    }
    
    return (ch != 0);
  }

  public byte readByte() throws IOException {
    int b = read();
    if (b < 0) {
      throw new EOFException("End of file");
    }
    
    return (byte) b;
  }

  public int readUnsignedByte() throws IOException {
    int b = read();
    if (b < 0) {
      throw new EOFException("End of file");
    }
    
    return b;
  }

  public short readShort() throws IOException {
    int ch1 = read();
    int ch2 = read();
    
    if ((ch1 | ch2) < 0) {
      throw new EOFException("End of file");
    }
    
    return (short) ((ch1 << 8) + ch2);
  }

  public int readUnsignedShort() throws IOException {    
    int ch1 = read();
    int ch2 = read();
    
    if ((ch1 | ch2) < 0) {
      throw new EOFException("End of file");
    }
    
    return ((ch1 << 8) + ch2);
  }

  public char readChar() throws IOException {
    int ch1 = read();
    int ch2 = read();
    
    if ((ch1 | ch2) < 0) {
      throw new EOFException("End of file");
    }
    
    return (char) ((ch1 << 8) + ch2);
  }

  public int readInt() throws IOException {
    int b1 = read();
    int b2 = read();
    int b3 = read();
    int b4 = read();
    
    if ((b1 | b2 | b3 | b4) < 0) {
      throw new EOFException("End of file");
    }
    return (b1 << 24) + (b2 << 16) + (b3 << 8) + b4;
  }

  public long readLong() throws IOException {
    return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
  }

  public float readFloat() throws IOException {
    return Float.intBitsToFloat(readInt());
  }

  public double readDouble() throws IOException {
    return Double.longBitsToDouble(readLong());
  }

  public String readLine() throws IOException {
    StringBuilder sb = new StringBuilder();
    int c = -1;
    boolean eol = false;
    
    while (!eol) {
      c = read();
      
      switch(c) {
        case -1:
        case '\n':          
          sb.append(c);
          eol = true;
          break;
        
        case '\r':
          sb.append(c);
          
          long oldPos = getFilePointer();
          c = read();
          if (c != '\n') {
            seek(oldPos);
          }
          
          eol = true;
          break;
          
        default:
          sb.append(c);
          break;
      }
    }
    
    if (c == -1 && sb.length() == 0) {
      return null;
    }
    
    return sb.toString();
  }

  public String readUTF() throws IOException {
    return DataInputStream.readUTF(this);
  }

  public void write(int i) throws IOException {
    if (!readOnly) {
      fd.write(i);
    } else {
      throw new IOException("Attempt to write to a file that was opened for read-only");
    }
  }

  public void write(byte[] bytes) throws IOException {
    write(bytes, 0, bytes.length);
  }

  public void write(byte[] bytes, int offset, int length) throws IOException {
    if (!readOnly) {
      fd.write(bytes, offset, length);
    } else {
      throw new IOException("Attempt to write to a file that was opened for read-only");
    }
  }

  public void writeBoolean(boolean bln) throws IOException {
    write(bln ? 1 : 0);
  }

  public void writeByte(int i) throws IOException {
    write(i);
  }

  public void writeShort(int i) throws IOException {
    write((i >>> 8) & 0xFF);
    write(i & 0xFF);
  }

  public void writeChar(int i) throws IOException {
    write((i >>> 8) & 0xFF);
    write(i & 0xFF);
  }

  public void writeInt(int i) throws IOException {
    write((i >>> 24) & 0xFF);
    write((i >>> 16) & 0xFF);
    write((i >>> 8) & 0xFF);
    write((i >>> 0) & 0xFF);
  }

  public void writeLong(long l) throws IOException {
    write((int)(l >>> 56) & 0xFF);
    write((int)(l >>> 48) & 0xFF);
    write((int)(l >>> 40) & 0xFF);
    write((int)(l >>> 32) & 0xFF);
    write((int)(l >>> 24) & 0xFF);
    write((int)(l >>> 16) & 0xFF);
    write((int)(l >>> 8) & 0xFF);
    write((int)(l >>> 0) & 0xFF);
  }

  public void writeFloat(float f) throws IOException {
    writeInt(Float.floatToIntBits(f));
     
  }

  public void writeDouble(double d) throws IOException {
    writeLong(Double.doubleToLongBits(d));
  }

  public void writeBytes(String string) throws IOException {
    byte[] buffer = string.getBytes(); 
    write(buffer);
  }

  public void writeChars(String string) throws IOException {
    int cLen = string.length();
    char[] chars = new char[cLen];
    string.getChars(0, string.length(), chars, 0);
    
    byte[] bytes = new byte[cLen * 2];
    
    int j = 0;
    for (int i = 0; i < cLen; i++) {
      char c = chars[i];
      bytes[j++] = (byte) (c >>> 8);      
      bytes[j++] = (byte) (c);
    }
    
    write(bytes);
  }

  public void writeUTF(String string) throws IOException {
    DataOutputStream.writeUTF(string, this);
  }

  
}

