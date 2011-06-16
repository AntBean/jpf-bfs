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
import javax.imageio.IIOException;

public class FileOutputStream extends OutputStream {  

  private FileState fileState;
  private long filePos;
  private boolean isOpened;

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
        boolean created = file.createNewFile();

        if (!created) {
          throw new FileNotFoundException(file.getPath() + " (No such file or director)");
        }
      }

      fileState = file.getFileInfo().getFileState();

      fileState.open();
      isOpened = true;

      if (append) {
        filePos = fileState.getLength();
      } else {
        // If FileInputStream don't append it's output to a file it removes file's content
        fileState.setLength(0);
      }
      

    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }
  }
  
  public FileOutputStream (FileDescriptor fdObj) {
    throw new RuntimeException("Not yet implemented");
  }

  public void close () throws IOException {
    fileState.close();
    isOpened = false;
  }

  public FileChannel getChannel() {
    throw new RuntimeException("Not yet implemented");
  }
  
  public FileDescriptor getFD() {
    throw new RuntimeException("Not yet implemented");
  }
  
  public void write (int b) throws IOException {
    byte toWrite = (byte) b;
    byte[] writeBuff = new byte[] {toWrite};

    write(writeBuff, 0, 1);
  }

  public void write (byte[] buf) throws IOException {
    write(buf, 0, buf.length);
  }

  public void write (byte[] buf, int off, int len) throws IOException {
    if (isOpened) {
      int written = fileState.write(filePos, buf, off, len);
      filePos += written;
    } else {
      throw new IOException("Attempt to write to closed stream");
    }
  }

  public void flush () throws IOException {
    // Nothing to do. BFS is always sync
  }
}
