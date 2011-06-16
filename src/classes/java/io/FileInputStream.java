//
// Copyright  (C) 2011 United States Government as represented by the
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

import gov.nasa.jpf.FileState;
import java.nio.channels.FileChannel;

/**
 * a simple model to read data w/o dragging the file system content into
 * the JPF memory
 */
public class FileInputStream extends InputStream implements Closeable {
  
  private FileState fileState;
  private long filePos;
  
  public FileInputStream (String fileName) throws FileNotFoundException {
    this(new File(fileName));
  }
  
  public FileInputStream (File file) throws FileNotFoundException {
    if (!file.exists()) {
      throw new FileNotFoundException(file.getPath() + "(No such file or directory)");
    }

    fileState = file.getFileInfo().getFileState();
    fileState.open();
  }
  
  public FileInputStream (FileDescriptor fd) {
    throw new RuntimeException("Not yet implemented");
  }  

  public int available () throws IOException {
     return (int) (fileState.getLength() - filePos);
  }

  public void close () throws IOException {
    fileState.close();
  }

  public FileChannel getChannel() {
    throw new RuntimeException("Not yet implemented");
  }

  public FileDescriptor getFD() {
    throw new RuntimeException("Not yet implemented");
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

  public int read(byte buffer[]) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte buffer[], int off, int len) throws IOException {
    if (filePos < fileState.getLength()) {
      int read = fileState.read(filePos, buffer, off, len);
      filePos += read;

      return read;
    }

    return -1;
  }
  
  public long skip(long shift) throws IOException {
    if (shift + filePos > fileState.getLength()) {
      filePos = fileState.getLength();

    } else {
      filePos = filePos + shift;
    }

    return shift;
  }
}
