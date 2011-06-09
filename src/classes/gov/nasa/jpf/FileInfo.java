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
import java.util.HashSet;
import java.util.Random;

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
   * @return if file represented by a FileInfo is a directory, it returns an array
   * of canonical paths of files in this directory. Otherwise it returns null.
   */
  public String[] list() {
    System.out.println("FileInfo.list()");

    if (fileState.isDir() && fileState.exists()) {
      String[] nativeFSChilds = {};

      if (fileState.getNativeFSFileName() != null) {
        nativeFSChilds = getChildsCPs(fileState.getNativeFSFileName());
      }

      System.out.println("Native FS childs: ");
      for (String childName : nativeFSChilds) {
        System.out.print(childName + ", ");
      }
      System.out.println(";");

      HashSet<String> set = new HashSet<String>();
      for (FileInfo child : fileState.getChilds()) {
        if (child.fileState.exists()) {
          set.add(child.cannonicalPath);
          System.out.println("Found new existing child " + child.cannonicalPath);
        }
        else {
          System.out.println("Found new deleted child " + child.cannonicalPath);
        }
      }

      for (String fsChild : nativeFSChilds) {
        set.add(fsChild);
      }

      System.out.println("Current childs: ");
      for (String childName : set) {
        System.out.print(childName + ", ");
      }
      System.out.println(";");

      String[] currentChildren = new String[set.size()];
      
      return set.toArray(currentChildren);
    }

    return null;
  }

  /**
   * Move a file represented by this FileInfo.
   * @param destCannnonicalPath - new name of a file
   * @return - true if file was moved successfully, false otherwise.
   */
  public boolean renameTo(String destCannnonicalPath) {
    System.out.println("Renaming " + cannonicalPath + " to " + destCannnonicalPath);

    // If file doesn't exist it can't be moved
    if (fileState.exists()) {
      FileInfo destFI = getFileInfo(destCannnonicalPath);
      FileInfo destParentFI = getFileInfo(getParentCP(destCannnonicalPath));

      // If file's parent doesn't exist or was deleted, file can't be moved
      if (destParentFI != null && destParentFI.fileState.exists()) {
        // State of file to move
        FileState state = new FileState(fileState);

        // Destanation file doesn't exist
        if (destFI == null) {
          FileInfo newFI = new FileInfo(destCannnonicalPath, state.isDir());
          newFI.fileState = state;
          addNewFI(newFI);

        } else {
          destFI.fileState = state;
        }

        // Move all child files
        if (fileState.isDir()) {
          String[] childs = list();
          for (String child : childs) {
            FileInfo childFI = getFileInfo(child);
            if (childFI.exists()) {
              String childCP = childFI.cannonicalPath;
              String destChildCP = childCP.replace(cannonicalPath, destCannnonicalPath);

              childFI.renameTo(destChildCP);
            }
          }
        }

        // Mark file that was renamed as deleted
        this.delete();
        return true;
      }
    }

    return false;
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

  /**
   * Create all directories that needed to be created for a directory with
   * given canonical path.
   * @param canonicalPath - canonial path of a directory that should be created.
   * @param firstCall - true if this call to this function is the first one, false
   * otherwise.
   * @return true if all directories were created, false otherwise.
   */
  public static boolean mkdirs(String canonicalPath, boolean firstCall) {
    FileInfo fi = getFileInfo(canonicalPath);
    System.out.println("FileInfo.mkdirs " + fi);
    
    // File not exists
    if (fi == null) {
      String parent = getParentCP(canonicalPath);
      if (mkdirs(parent, false)) {
        mkdir(canonicalPath);
        return true;
      }
      else {
        return false;
      }
    }
    // File was deleted
    else if (!fi.fileState.exists()) {
      String parent = getParentCP(canonicalPath);
      if (mkdirs(parent, false)) {
        fi.fileState.setIsExists(true);
        fi.fileState.setChilds(new ArrayList<FileInfo>());

        FileInfo parentFI = getFileInfoByCannonicalPath(parent);
        parentFI.fileState.addChild(fi);

        return true;
      }
      else {
        return false;
      }
    }
    // FS object is a file
    else if (!fi.fileState.isDir()) {
      return false;
    }
    // File exists
    else if (firstCall) {
      // If File.mkdirs() is called to create existing directory it should return
      // false
      return false;
    }

    return true;
  }
  
  private static FileInfo getFileInfoByCannonicalPath(String fileName) {

    for (FileInfo fi : fileInfos) {
      if (fi.cannonicalPath.equals(fileName)) {
        return fi;
      }
    }

    return null;
  }

  /**
   * Create new temporary file.
   * @param tempDir - canonical path of a directory to create file in.
   * @param separatorChar - system's separator char.
   * @param prefix - prefix of a new file
   * @param suffix - suffix of a new file
   * @return canonical path of a new file. If file can't be created returns null.
   */
  public static String createTempFile(String tempDir, char separatorChar, String prefix, String suffix) {
    System.out.println("Creating tempFile in " + tempDir + "; with prefix '" + prefix + "' and suffix '" + suffix + "'");

    FileInfo fi = getFileInfo(tempDir);

    if (fi != null && fi.fileState.exists() && fi.fileState.isDir()) {
      
      Random rand = new Random();
      while(true) {
        // <2do> This should create new ChoiceGenerator. Maybe replace it with UUID?
        String uuid = Long.toString(rand.nextLong());
        String tempFileName = prefix + uuid + suffix;

        String newFileCP = tempDir + separatorChar + tempFileName;

        FileInfo newFileFI = getFileInfoByCannonicalPath(newFileCP);

        // No file with such filename exists.
        if (newFileFI == null) {
          // Create new file
          createNewFileFI(newFileCP);
          return newFileCP;
        }
      }
    }

    return null;
  }

  private static native boolean isFSRoot(String fileName);

  private static native String[] getChildsCPs(String cp);

  @Override
  public String toString() {
    String result = "CP: " + cannonicalPath + "; ";
    result += "FS: " + fileState;

    return result;
  }



}
