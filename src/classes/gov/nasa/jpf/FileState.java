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

import java.io.FileDescriptor;

/**
 * This class stores state of a file in BFS
 * @author Ivan Mushketik
 */
public class FileState {
  // Length of a file
  private long length;
  // True if this is a directory, false otherwise
  private boolean isDir;
  // nO - nC; nO - number of open() operations nC - number of close() operations
  private int openCnt;
  // File/directory that represents this file/directory content on a native FS
  private String nativeFSFileName;
  // True if file/directory exists
  private boolean doesExist;
  
  // Time of last modification
  private long lastModified;
  // Access mode to a file
  private int fileMode;

  private int lastOperation = FileOperations.READ;

  private WriteChunk lastWriteChunk;
  
  private boolean isReadableForSUT;
  private boolean isWritableForSUT;
  private boolean isExecutableForSUT;
    
  private boolean isReadableForAll;
  private boolean isWritableForAll;
  private boolean isExecutableForAll;
  
  private boolean isReadableForGroup;
  private boolean isWritableForGroup;
  private boolean isExecutableForGroup;  
  
  private boolean isReadalbeForOwner;
  private boolean isWritableForOwner;
  private boolean isExecutableForOwner;
  
  public FileState(boolean isDir) { 
    this.isDir = isDir;
  }
  
  public FileState(FileState fs) {
    length = fs.length;
    isDir = fs.isDir;
    openCnt = fs.openCnt;
    nativeFSFileName = fs.nativeFSFileName;
    doesExist = fs.doesExist;
    
    isReadableForSUT = fs.isReadableForSUT;
    isWritableForSUT = fs.isWritableForSUT;
    isExecutableForSUT = fs.isExecutableForSUT;
    // <2do> Add other rights coping
    
    lastWriteChunk = fs.lastWriteChunk;
    lastModified = fs.lastModified;
    fileMode = fs.fileMode;
  }

  /**
   * Get file's length
   * @return file's length
   */
  public long getLength() {
    return length;
  }

  public void setLength(long newLength) {
    length = newLength;
  }

  /**
   * Check if this is a directory
   * @return true if this is a directory, false otherwise
   */
  public boolean isDir() {
    return isDir;
  }

  /**
   * Set if this is a directory
   * @param isDir true if this is a directory now.
   */
  public void setIsDir(boolean isDir) {
    this.isDir = isDir;
  }

  /**
   * Get number of open operations minus number of close operations
   * @return
   */
  public synchronized  int getOpenCnt() {
    return openCnt;
  }

  /**
   * Get canonical path of a file that saves content of a file/directory before
   * SUT run
   * @return
   */
  public String getNativeFSFileName() {
    return nativeFSFileName;
  }

  /**
   *
   * @param path
   */
  public void setNativeFSFileName(String path) {
    nativeFSFileName = path;
  }

  /**
   * Check if file/directory exists
   * @return true if file exists
   */
  public boolean exists() {
    return doesExist;
  }

  /**
   * Set if file exists
   * @param exists - true if file exists
   */
  public void setDoesExist(boolean exists) {
    this.doesExist = exists;
  }

  public boolean isReadableForSUT() {
    return isReadableForSUT;
  }

  public void setReadableForSUT(boolean readableForSUT) {
    isReadableForSUT = readableForSUT;
  }

  public boolean isWritableForSUT() {
    return isWritableForSUT;
  }

  public void setWritableForSUT(boolean writableForSUT) {
    isWritableForSUT = writableForSUT;
  }

  public boolean isExecutableForSUT() {
    return isExecutableForSUT;
  }

  public void setExecutableForSUT(boolean executableForSUT) {
    isExecutableForSUT = executableForSUT;
  }

  public boolean isReadableForOwner() {
    return isReadalbeForOwner;
  }

  public void setReadableForOwner(boolean readableForOwner) {
    isReadalbeForOwner = readableForOwner;
  }

  public boolean isWritableForOwner() {
    return isWritableForOwner;
  }

  public void setWritableForOwner(boolean writableForOwner) {
    isWritableForOwner = writableForOwner;
  }

  public boolean isExecutableForOwner() {
    return isExecutableForOwner;
  }

  public void setExecutableForOwner(boolean executableForOwner) {
    isExecutableForOwner = executableForOwner;
  }

  public boolean isReadableForGroup() {
    return isReadableForGroup;
  }

  public void setReadableForGroup(boolean readableForGroup) {
    isReadableForGroup = readableForGroup;
  }

  public boolean isWritableForGroup() {
    return isWritableForGroup;
  }

  public void setWritableForGroup(boolean writableForGroup) {
    isWritableForGroup = writableForGroup;
  }

  public boolean isExecutableForGroup() {
    return isExecutableForGroup;
  }

  public void setExecutableForGroup(boolean executableForGroup) {
    isExecutableForGroup = executableForGroup;
  }

  public boolean isReadableForAll() {
    return isReadableForAll;
  }

  public void setReadableForAll(boolean readableForAll) {
    isReadableForAll = readableForAll;
  }

  public boolean isWritableForAll() {
    return isWritableForAll;
  }

  public void setWritableForAll(boolean writableForAll) {
    isWritableForAll = writableForAll;
  }

  public boolean isExecutableForAll() {
    return isExecutableForAll;
  }

  public void setExecutableForAll(boolean executableForAll) {
    isExecutableForAll = executableForAll;
  }

  /**
   * Get time of last modification of a file/directory
   * @return
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Set time of last modification of a file/directory
   * @param time
   */
  public void setLastModified(long time) {
    lastModified = time;
  }

  public int getFileAccessMode() {
    return fileMode;
  }

  void setFileAccessMode(int fileMode) {
    this.fileMode = fileMode;
  }
  
  public void updateLastModified() {
    lastModified = System.currentTimeMillis();
  }

  public synchronized FileDescriptor open() {
    if (exists() && !isDir()) {
      openCnt++;

      FileInterface fi;
      // If file was created during SUT run it's imposible to return
      // NativeFileInterface
      if (fileMode == FileAccessMode.BFS_FILE_ACCESS) {
        fi = new BFSFileInterface(this);

      } else if (fileMode == FileAccessMode.NATIVE_FILE_ACCESS) {
        fi = new NativeFileInterface(this, false);

      } else if (fileMode == FileAccessMode.BFS_IGNORE_WRITE) {
        fi = new NativeFileInterface(this, true);

      } else {
        throw new UnsupportedOperationException("Not supported file access mode " + fileMode);
      }

      return new FileDescriptor(fi, this);
    }

    return null;
  }

  public synchronized void close() {
    openCnt--;
  }

  // We need markRead/markWrite to create race with access to lastOperation field
  // if this file is read/written or written/written from several threads at the
  // same time with no synchronization this will be detected by
  // PreciseRaceDetector

  public void markWrite(int operation) {
    lastOperation = operation;
  }

  public int markRead() {
    return lastOperation;
  }
  
  /**
   * Write data to a BFS file.
   * @param startPos - offset from the beginning of a file
   * @param data - buffer with data
   * @param offset - offset in a data buffer
   * @param length - number of bytes to write
   * @return number of bytes that was written
   */  
  native int write(long startPos, byte[] data, int offset, int length);

  /**
   * Read data from a BFS file
   * @param startPos - offset from the beginning of a file
   * @param data - buffer to read data
   * @param offset - offset in a data buffer
   * @param length - number of bytes to read
   * @return number of bytes that was read
   */ 
  native int read(long startPos, byte[] data, int offset, int length);

  @Override
  public synchronized String toString() {
    String result;
    result = "length = " + length;
    result += "; isDir = " + isDir;
    result += "; exists = " + doesExist;
    result += "; openCnt = " + openCnt;
    result += "; nativeFSName = " + nativeFSFileName;
    result += "; canRead = " + isReadableForSUT;
    result += "; canWrite = " + isWritableForSUT;
    result += "; canExecute = " + isExecutableForSUT;
    result += "; file mode = " + fileMode;
    result += "; last modified = " + lastModified;

    return result;
  }

  
}
