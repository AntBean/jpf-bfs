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

    if (fileState.isExists() && !isFSRoot(cannonicalPath)) {
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
    return fileState.isExists();
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

    while ((cp = getParentCP(cp))  != null) {
      FileInfo parentFI = getFileInfo(cp);

      if (!parentFI.fileState.isExists()) {
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

    if (fi == null || !fi.fileState.isExists()) {
      String parentCP = getParentCP(filename);
      FileInfo parentFI = getFileInfo(parentCP);

      if (parentFI != null && parentFI.fileState.isExists()) {
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

    if (fi == null || !fi.fileState.isExists()) {
      String parentCP = getParentCP(filename);
      FileInfo parentFI = getFileInfo(parentCP);

      if (parentFI != null && parentFI.fileState.isExists()) {
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

  @Override
  public String toString() {
    String result = "CP: " + cannonicalPath + "; ";
    result += "FS: " + fileState;


    return result;

  }
}
