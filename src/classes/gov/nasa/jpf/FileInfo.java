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
public class FileInfo {

  private static ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
  private String cannonicalPath;
  private FileState fileState;

  private FileInfo(String filename, boolean isDir) {
    cannonicalPath = filename;
    
    fileState = new FileState();
    fileState.setIsDir(isDir);
    fileState.setIsExists(true);
  }


  public boolean delete() {

    if (fileState.exists() && !isFSRoot(cannonicalPath)) {
      fileState.setIsExists(false);

      for (FileInfo child : fileState.getChilds()) {
        child.delete();
      }

      return true;
    }

    return false;
  }

  public FileState getFileState() {
    return fileState;
  }

  public boolean exists() {
    return fileState.exists();
  }

  /**
   * This method returns array of child dirs and files of a file represented by
   * current file info in a current state.
   * Because java.io.File.list() returns list of names of files in a specified dir,
   * this methods returns names of files.
   * @return if file represented by a FileInfo is a directory, it returns an array
   * of names of files in this directory. Otherwise it returns null.
   */
  public String[] list() {
    System.out.println("FileInfo.list()");

    if (fileState.isDir() && fileState.exists()) {
      String[] nativeFSChilds = {};

      if (fileState.getNativeFSFileName() != null) {
        nativeFSChilds = childNamesList(fileState.getNativeFSFileName());
      }

      System.out.println("Native FS childs: ");
      for (String childName : nativeFSChilds) {
        System.out.print(childName + ", ");
      }
      System.out.println(";");

      int existsChilds = 0;
      for (FileInfo child : fileState.getChilds()) {
        if (child.fileState.exists()) {
          existsChilds++;
          System.out.println("Found new existing child " + child.cannonicalPath);
        }
        else {
          System.out.println("Found new deleted child " + child.cannonicalPath);
        }
      }

      String[] currentChilds;
      
      if (existsChilds > 0) {
        currentChilds = new String[nativeFSChilds.length + existsChilds];

        System.arraycopy(nativeFSChilds, 0, currentChilds, 0, nativeFSChilds.length);

        int currentChildPos = nativeFSChilds.length;
        int parentNameLength = cannonicalPath.length();
        for (FileInfo child : fileState.getChilds()) {
          if (child.fileState.exists()) {
            currentChilds[currentChildPos] = child.cannonicalPath.substring(parentNameLength + 1);
            currentChildPos++;
          }
        }
      }
      else {
        currentChilds = nativeFSChilds;
      }

      System.out.println("Current childs: ");
      for (String childName : currentChilds) {
        System.out.print(childName + ", ");
      }
      System.out.println(";");

      return currentChilds;
    }

    return null;
  }

  public static FileInfo getFileInfo(String filename) {
    System.out.println("Request for " + filename + " FileInfo");
    FileInfo newFI = getFileInfoByCannonicalPath(filename);

    if (newFI != null) {
      System.out.println("Found in FileInfo DS");
      return newFI;
    }

    newFI = createNewFileInfo(filename);
    if (newFI != null) {
      System.out.println("Found in native FS");
      newFI.fileState.setChilds(new ArrayList<FileInfo>());

      addNewFI(newFI);
    }
    else {
      System.out.println("Found no FileInfo");
    }
    
    return newFI;
  }
  
  private static void addNewFI(FileInfo newFI) {
    String cp = newFI.cannonicalPath;

    while ((cp = getParentCP(cp)) != null) {
      FileInfo parentFI = getFileInfo(cp);

      if (!parentFI.fileState.exists()) {
        System.out.println(parentFI.cannonicalPath + " was deleted, so " + newFI.cannonicalPath + " is deleted too");
        newFI.fileState.setIsExists(false);
        break;
      }
    }

    String parentCP = getParentCP(newFI.cannonicalPath);
    for (FileInfo potentialParent : fileInfos) {
      if (potentialParent.cannonicalPath.equals(parentCP)) {
        System.out.println("Found parent " + potentialParent.cannonicalPath + " for " + newFI.cannonicalPath);

        potentialParent.fileState.addChild(newFI);
        break;
      }
    }

    for (FileInfo potentialChild : fileInfos) {
      String potentialChildParentCP = getParentCP(potentialChild.cannonicalPath);

      if (newFI.cannonicalPath.equals(potentialChildParentCP)) {
        System.out.println("Found child " + potentialChild.cannonicalPath + " for " + newFI.cannonicalPath);
        newFI.fileState.addChild(potentialChild);
      }
    }

    fileInfos.add(newFI);
  }

  public static boolean createNewFile(String filename) {
    System.out.println("Attempt to create new FileInfo for a file " + filename);

    FileInfo fi = getFileInfo(filename);

    if (fi == null || !fi.fileState.exists()) {
      String parentCP = getParentCP(filename);
      FileInfo parentFI = getFileInfo(parentCP);

      if (parentFI != null && parentFI.fileState.exists()) {
        if (fi == null) {
          fi = new FileInfo(filename, false);
        }

        fi.fileState.setIsDir(false);

        addNewFI(fi);
        return true;
      }
    }

    return false;
  }

  private static native FileInfo createNewFileInfo(String fileName);

  private static native String getParentCP(String filename);

  public static void createNewFileFI(String filename) {
    FileInfo fi = new FileInfo(filename, true);
    fileInfos.add(fi);
  }
  
  public static boolean mkdir(String filename) {
    System.out.println("Attempt to create new FileInfo for a dir " + filename);

    FileInfo fi = getFileInfo(filename);

    if (fi == null || !fi.fileState.exists()) {
      String parentCP = getParentCP(filename);
      FileInfo parentFI = getFileInfo(parentCP);

      if (parentFI != null && parentFI.fileState.exists()) {
        if (fi == null) {
          fi = new FileInfo(filename, true);
        }

        fi.fileState.setIsDir(true);

        addNewFI(fi);
        return true;
      }
    }

    return false;
  }

  private static FileInfo getFileInfoByCannonicalPath(String fileName) {

    for (FileInfo fi : fileInfos) {
      if (fi.cannonicalPath.equals(fileName)) {
        return fi;
      }
    }

    return null;
  }

  private static native boolean isFSRoot(String fileName);

  private static native String[] childNamesList(String cp);

  @Override
  public String toString() {
    String result = "CP: " + cannonicalPath + "; ";
    result += "FS: " + fileState;


    return result;

  }
}
