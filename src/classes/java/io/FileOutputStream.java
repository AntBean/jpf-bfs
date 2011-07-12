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
 * Implementation of FileOuptutStream that can write data to a BFS.
 * 
 * @author Ivan Mushketik
 */
public class FileOutputStream extends OutputStream {  

  private FileDescriptor fd;

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

      FileState fileState = file.getFileInfo().getFileState();      
      checkPermissions(file, fileState);
      fd = fileState.open();     

      if (append) {
        fd.seek(fd.length());
      } else {
        // If FileInputStream don't append it's output to a file it removes file's content
        fd.setLength(0);
      }
      

    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }
  }
  
  private void checkPermissions(File file, FileState fileState) throws FileNotFoundException {
    try {
      if (!fileState.isWritableForSUT()) {
        throw new FileNotFoundException(file.getCanonicalPath() + " (Permission denied)");
      }
    } catch (IOException ex) {
      throw new FileNotFoundException(ex.getMessage());
    }
  }
  
  public FileOutputStream (FileDescriptor fdObj) {
    this.fd = fdObj;
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
  
  public void write (int b) throws IOException {
    fd.write(b);
  }

  public void write (byte[] buf) throws IOException {
    write(buf, 0, buf.length);
  }

  public void write (byte[] buf, int off, int len) throws IOException {
    fd.write(buf, off, len);
  }

  public void flush () throws IOException {
    fd.sync();
  }
}
