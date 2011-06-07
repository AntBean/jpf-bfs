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
 *
 * @author Ivan Mushketik
 */
public class FileState {
  private long length;

  private boolean isDir;

  private int openCnt;

  private String nativeFSFileName;

  private boolean isExist;

  private ArrayList<FileInfo> childs = new ArrayList<FileInfo>();

  private FileInfo parent;

  private byte sutRights;

  private byte ownerRights;

  private byte groupRights;

  private byte allRights;


  private static final byte READ_FLAG = 4;
  private static final byte WRITE_FLAG = 2;
  private static final byte EXECUTE_FLAG = 1;

  public long getLength() {
    return length;
  }

  public boolean isDir() {
    return isDir;
  }

  public void setIsDir(boolean isDir) {
    this.isDir = isDir;
  }

  public int getOpenCnt() {
    return openCnt;
  }

  public String getNativeFSFileName() {
    return nativeFSFileName;
  }

  public boolean exists() {
    return isExist;
  }

  public void setIsExists(boolean exist) {
    this.isExist = exist;
  }

  public void addChild(FileInfo child) {
    childs.add(child);
  }

  public ArrayList<FileInfo> getChilds() {
    return childs;
  }

  void setChilds(ArrayList<FileInfo> childs) {
    this.childs = childs;
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
