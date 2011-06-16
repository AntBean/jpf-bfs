//
// Copyright  (C) 2007 United States Government as represented by the
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

public class FileOutputStream extends OutputStream {  

  private FileState fileState;
  private long filePos;

  public FileOutputStream (String name) throws FileNotFoundException {
    this(new File(name));
  }

  public FileOutputStream (String name, boolean append) throws FileNotFoundException {
    this(new File(name), append);
  }
  
  public FileOutputStream (File file) throws FileNotFoundException {
    this(file, false);
  }

  public FileOutputStream (File file, boolean append) throws FileNotFoundException {
    try {
      if (!file.exists()) {
        if (!file.createNewFile()) {
          throw new FileNotFoundException(file.getPath() + "(No such file or director)");
        }
      }

      fileState = file.getFileInfo().getFileState();

      if (append) {
        filePos = fileState.getLength();
      }

      fileState.open();

    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }
  }
  
  public FileOutputStream (FileDescriptor fdObj) {
    throw new RuntimeException("Not yet implemented");
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
  
  public void write (int b) throws IOException {
    throw new RuntimeException("Not yet implemented");
  }

  public void write (byte[] buf) throws IOException {
    throw new RuntimeException("Not yet implemented");
  }

  public void write (byte[] buf, int off, int len) throws IOException {
    throw new RuntimeException("Not yet implemented");
  }

  public void flush () throws IOException {
    // Nothing to do. BFS is always sync
  }
}
