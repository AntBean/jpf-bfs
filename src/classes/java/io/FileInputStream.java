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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * a simple model to read data w/o dragging the file system content into
 * the JPF memory
 */
public class FileInputStream extends InputStream implements Closeable {
  
  private FileDescriptor fd;
  
  public FileInputStream (String fileName) throws FileNotFoundException {
    this(new File(fileName));
  }
  
  public FileInputStream (File file) throws FileNotFoundException {
    if (!file.exists()) {
      throw new FileNotFoundException(file.getPath() + "(No such file or directory)");
    }

    FileState fileState = file.getFileInfo().getFileState();
    
    checkPermissions(file, fileState);
    
    fd = fileState.open();
  }
  
  private void checkPermissions(File file, FileState fileState) throws FileNotFoundException {
    try {
      if (!fileState.isReadableForSUT()) {
        throw new FileNotFoundException(file.getCanonicalPath() + " (Permission denied)");
      }
    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }
  }
  
  public FileInputStream (FileDescriptor fd) {
    this.fd = fd;
  }  

  public int available () throws IOException {
     return fd.available();
  }

  public void close () throws IOException {
    fd.close();
  }

  public FileChannel getChannel() {
    throw new RuntimeException("Not yet implemented");
  }

  public FileDescriptor getFD() {
    return fd;
  }
  
  public int read() throws IOException {
    return fd.read();
  }

  public int read(byte buffer[]) throws IOException {
    return read(buffer, 0, buffer.length);
  }

  public int read(byte buffer[], int off, int len) throws IOException {
    return fd.read(buffer, off, len);
  }
  
  public long skip(long shift) throws IOException {
    return fd.skip(shift);
  }
}
