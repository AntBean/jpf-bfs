//
// Copyright  (C) 2006 United States Government as represented by the
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

import gov.nasa.jpf.FileInterface;
import gov.nasa.jpf.FileOperations;
import gov.nasa.jpf.FileState;

/**
 * FileDescriptor - interface to a FileInterface instance that implements
 * methods to access files on a file system. *
 * 
 */
public class FileDescriptor {

  private FileInterface fileInterface;
  private FileState fileState;
  private boolean isOpened;
  
  public FileDescriptor(FileInterface fi, FileState fileState) {
    fileInterface = fi;
    this.fileState = fileState;
    isOpened = true;
  }
  
  public boolean valid () {
    return isOpened;
  }
  
  public void close () throws IOException {
    if (isOpened) {
      fileInterface.close();
      isOpened = false;
    }
  }

  public void sync() throws SyncFailedException {
    if (isOpened) {
      fileInterface.sync();
    } else {
      throw new SyncFailedException("Attempt to sync closed descriptor");
    }
  }
  
  int read () throws IOException {
    if (isOpened) {
      if (fileState.isReadableForSUT()) {
        fileState.markRead();
        return fileInterface.read();
      } else {
        throw new IOException("No rights to read file");
      }
    } else {
      throw new IOException("Attempt to read with closed descriptor");
    }
  }

  int read (byte[] buf, int off, int len) throws IOException {
    if (isOpened) {
      if (fileState.isReadableForSUT()) {
        fileState.markRead();
        return fileInterface.read(buf, off, len);
      } else {
        throw new IOException("No rights to read file");
      }
    } else {
      throw new IOException("Attempt to read with closed descriptor");
    } 
  }

  long skip(long n) throws IOException {
    if (isOpened) {
      return fileInterface.skip(n);
    } else {
      throw new IOException("Attempt to skip bytes with closed descriptor");
    }
  }

  int available () throws IOException {
    if (isOpened) {
      return fileInterface.available();
    } else {
      throw new IOException("Attempt to get number of available bytes with closed descriptor");
    }
  }
  
  void write (int b) throws IOException {
    if (isOpened) {
      if (fileState.isWritableForSUT()) {
        fileState.markWrite(FileOperations.WRITE);
        fileInterface.write(b);
        
        fileState.updateLastModified();
      } else {
        throw new IOException("No rights to write to file");
      }
    } else {     
      throw new IOException("Attempt to write with closed descriptor");
    }    
  }

  void write (byte[] buf, int off, int len) throws IOException {
    if (isOpened) {
      if (fileState.isWritableForSUT()) {
        fileState.markWrite(FileOperations.WRITE);
        fileInterface.write(buf, off, len);
        
        fileState.updateLastModified();
      } else {
        throw new IOException("No rights to write to file");
      }
    } else {
      throw new IOException("Attempt to read with closed descriptor");
    }
  }

  void setLength(long newLength) throws IOException {
    if (isOpened) {
      fileState.markWrite(FileOperations.WRITE);
      fileInterface.setLength(newLength);
      
      fileState.updateLastModified();
    } else {
      throw new IOException("Attempt to set file length with closed descriptor");
    }
  }

  void seek(long pos) throws IOException {
    if (isOpened) {
      fileInterface.seek(pos);
    } else {
      throw new IOException("Attempt to seek in file with closed descriptor");
    }
  }

  long length() throws IOException {
    if (isOpened) {
      return fileInterface.length();
    } else {
      throw new IOException("Attempt to get file length with closed descriptor");
    }
  }

  long filePointer() throws IOException {
    if (isOpened) {
      return fileInterface.getFilePointer();
    } else {
      throw new IOException("Attempt to get file pointer with closed descriptor");
    }
    
  }
}
