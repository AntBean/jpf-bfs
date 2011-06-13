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

import java.util.ArrayList;

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
  private boolean isExist;
  // Children of this file
  private ArrayList<FileInfo> children = new ArrayList<FileInfo>();
  // Rights of SUT on this file
  private byte sutRights;
  // Rights of file's owner on this file
  private byte ownerRights;
  // Rights of file's group on this file
  private byte groupRights;
  // Rights of other on this file
  private byte allRights;
  // Time of last modification
  private long lastModified;

  private static final byte READ_FLAG = 4;
  private static final byte WRITE_FLAG = 2;
  private static final byte EXECUTE_FLAG = 1;


  public FileState() { }

  public FileState(FileState fs) {
    length = fs.length;
    isDir = fs.isDir;
    openCnt = fs.openCnt;
    nativeFSFileName = fs.nativeFSFileName;
    isExist = fs.isExist;
    sutRights = fs.sutRights;
    ownerRights = fs.ownerRights;
    groupRights = fs.groupRights;
    allRights = fs.allRights;
  }

  /**
   * Get file's length
   * @return file's length
   */
  public long getLength() {
    return length;
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
  public int getOpenCnt() {
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
    return isExist;
  }

  /**
   * Set if file exists
   * @param exist - true if file exists
   */
  public void setIsExists(boolean exist) {
    this.isExist = exist;
  }

  /**
   * Add child for this directory
   * @param child - new child to add
   */
  public void addChild(FileInfo child) {
    children.add(child);
  }

  /**
   * Get children of this directory
   * @return
   */
  public ArrayList<FileInfo> getChildren() {
    return children;
  }

  /**
   * Set children of this directory
   * @param children
   */
  void setChildren(ArrayList<FileInfo> children) {
    this.children = children;
  }

  public boolean isReadableForSUT() {
    return (sutRights & READ_FLAG) > 0;
  }

  public void setReadableForSUT(boolean readableForSUT) {
    if (readableForSUT) {
      sutRights |= READ_FLAG;
    }
    else {
      sutRights &= (~READ_FLAG);
    }
  }

  public boolean isWritableForSUT() {
    return (sutRights & WRITE_FLAG) > 0;
  }

  public void setWritableForSUT(boolean writableForSUT) {
    if (writableForSUT) {
      sutRights |= WRITE_FLAG;
    }
    else {
      sutRights &= (~WRITE_FLAG);
    }
  }

  public boolean isExecutableForSUT() {
    return (sutRights & EXECUTE_FLAG) > 0;
  }

  public void setExecutableForSUT(boolean executableForSUT) {
    if (executableForSUT) {
      sutRights |= EXECUTE_FLAG;
    }
    else {
      sutRights &= (~EXECUTE_FLAG);
    }
  }

  public boolean isReadableForOwner() {
    return (ownerRights & READ_FLAG) > 0;
  }

  public void setReadableForOwner(boolean readableForOwner) {
    if (readableForOwner) {
      ownerRights |= READ_FLAG;
    }
    else {
      ownerRights &= (~READ_FLAG);
    }
  }

  public boolean isWritableForOwner() {
    return (ownerRights & WRITE_FLAG) > 0;
  }

  public void setWritableForOwner(boolean writableForOwner) {
    if (writableForOwner) {
      ownerRights |= WRITE_FLAG;
    }
    else {
      ownerRights &= (~WRITE_FLAG);
    }
  }

  public boolean isExecutableForOwner() {
    return (ownerRights & EXECUTE_FLAG) > 0;
  }

  public void setExecutableForOwner(boolean executableForOwner) {
    if (executableForOwner) {
      ownerRights |= EXECUTE_FLAG;
    }
    else {
      ownerRights &= (~EXECUTE_FLAG);
    }
  }

  public boolean isReadableForGroup() {
    return (groupRights & READ_FLAG) > 0;
  }

  public void setReadableForGroup(boolean readableForGroup) {
    if (readableForGroup) {
      groupRights |= READ_FLAG;
    }
    else {
      groupRights &= (~READ_FLAG);
    }
  }

  public boolean isWritableForGroup() {
    return (groupRights & WRITE_FLAG) > 0;
  }

  public void setWritableForGroup(boolean writableForGroup) {
    if (writableForGroup) {
      groupRights |= WRITE_FLAG;
    }
    else {
      groupRights &= (~WRITE_FLAG);
    }
  }

  public boolean isExecutableForGroup() {
    return (groupRights & EXECUTE_FLAG) > 0;
  }

  public void setExecutableForGroup(boolean executableForGroup) {
    if (executableForGroup) {
      groupRights |= EXECUTE_FLAG;
    }
    else {
      groupRights &= (~EXECUTE_FLAG);
    }
  }

  public boolean isReadableForAll() {
    return (allRights & READ_FLAG) > 0;
  }

  public void setReadableForAll(boolean readableForAll) {
    if (readableForAll) {
      allRights |= READ_FLAG;
    }
    else {
      allRights &= (~READ_FLAG);
    }
  }

  public boolean isWritableForAll() {
    return (allRights & WRITE_FLAG) > 0;
  }

  public void setWritableForAll(boolean writableForAll) {
    if (writableForAll) {
      allRights |= WRITE_FLAG;
    }
    else {
      allRights &= (~WRITE_FLAG);
    }
  }

  public boolean isExecutableForAll() {
    return (allRights & EXECUTE_FLAG) > 0;
  }

  public void setExecutableForAll(boolean executableForAll) {
    if (executableForAll) {
      allRights |= EXECUTE_FLAG;
    }
    else {
      allRights &= (~EXECUTE_FLAG);
    }
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

  public void open() {
    openCnt++;
  }

  public void close() {
    openCnt--;
  }

  @Override
  public String toString() {
    String result;
    result = "length = " + length;
    result += "; isDir = " + isDir;
    result += "; exists = " + isExist;
    result += "; openCnt = " + openCnt;
    result += "; nativeFSName = " + nativeFSFileName;
    result += "; rights = " + ownerRights + "" + groupRights + "" + allRights;
    result += "; SUT rights = " + sutRights;

    return result;
  }




}
