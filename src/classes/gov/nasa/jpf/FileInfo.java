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
 * FileInfo stores data about all created and deleted files in Backtrackable
 * FileSystem (BFS).
 * Real data about file state like last modified time, rights, lengths is stored in
 * a FileState class. This separation was made, because file can be renamed (moved)
 * and File class should "see" that old file was deleted, but such files like
 * FileXStream and RandomAccessFile should still be able to read/write to this file.
 * This class also stores all FileInfos that was created during SUT work.
 *
 * <2do> it's not optimized at all. Some methods can be easily moved to a peer side.
 * @author Ivan Mushketik
 */
public class FileInfo {

  private static ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();

  // Canonical path of a file
  private String canonicalPath;
  // Current state of a file
  private FileState fileState;

  // Create new file in BFS
  private FileInfo(String filename, boolean isDir) {
    canonicalPath = filename;
    
    fileState = new FileState();
    fileState.setIsDir(isDir);
    fileState.setDoesExist(true);
  }

  private FileInfo(String canonicalPath, FileState state) {
    this.canonicalPath = canonicalPath;
    fileState = state;
  }

  /**
   * Delete file/directory from BFS.
   * @return true if operation successfully finished, false otherwise.
   */
  public boolean delete() {

    // File can be deleted if it exists or it's not a file system root
    if (fileState.exists() && !isFSRoot(canonicalPath)) {
      checkDeleteConfig();
      fileState.setDoesExist(false);

      // Recursively delete all children
      for (FileInfo child : fileState.getChildren()) {
        child.delete();
      }

      return true;
    }

    return false;
  }

  /**
   * According to jpf-bfs configuration deleting of an opened file should either
   * be an error or produce warning. This method checks jpf-bfs config and either
   * log a warning or throw an exception.
   */
  private native void checkDeleteConfig();

  /**
   * Get file state.
   * @return FileState that stores data about file state.
   */
  public FileState getFileState() {
    return fileState;
  }

  /**
   * Check if file exists.
   * @return true if file exists, false otherwise.
   */
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
      String[] nativeFSChildren = {};

      // If directory existed on a filesystem before SUT run, we can read child files
      // that exist on a native FS
      if (fileState.getNativeFSFileName() != null) {
        nativeFSChildren = getChildrenCPs(fileState.getNativeFSFileName());
      }

      System.out.println("Native FS children: ");
      for (String childName : nativeFSChildren) {
        System.out.print(childName + ", ");
      }
      System.out.println(";");

      HashSet<String> set = new HashSet<String>();

      for (String fsChild : nativeFSChildren) {
        set.add(fsChild);
      }

      // Add new children to child's set remove children that were deleted by SUT
      for (FileInfo child : fileState.getChildren()) {
        if (child.fileState.exists()) {
          set.add(child.canonicalPath);
          System.out.println("Found new existing child " + child.canonicalPath);
        }
        else {
          set.remove(child.canonicalPath);
          System.out.println("Found new deleted child " + child.canonicalPath);
        }
      }

      System.out.println("Current children: ");
      for (String childName : set) {
        System.out.print(childName + ", ");
      }
      System.out.println(";");

      String[] currentChildren = new String[set.size()];      
      return set.toArray(currentChildren);
    }

    // FileInfo represents not a directory, or doesn't exist
    return null;
  }

  /**
   * Move a file represented by this FileInfo.
   * @param destCanonicalPath - new name of a file
   * @return - true if file was moved successfully, false otherwise.
   */
  public boolean renameTo(String destCanonicalPath) {
    System.out.println("Renaming " + canonicalPath + " to " + destCanonicalPath);

    // If file doesn't exist it can't be moved
    if (fileState.exists()) {
      FileInfo destFI = getFileInfo(destCanonicalPath);
      FileInfo destParentFI = getFileInfo(getParent(destCanonicalPath));

      // If file's parent doesn't exist or was deleted, file can't be moved
      if (destParentFI != null && destParentFI.fileState.exists()) {
        checkRenameConfig();
        // State of file to move
        FileState state = new FileState(fileState);

        // Destanation file doesn't exist
        if (destFI == null) {
          FileInfo newFI = new FileInfo(destCanonicalPath, state);
          newFI.fileState = state;
          addNewFI(newFI);

        } else {
          destFI.fileState = state;
        }

        // Move all child files
        if (fileState.isDir()) {
          String[] children = list();
          for (String child : children) {
            FileInfo childFI = getFileInfo(child);
            if (childFI.exists()) {
              String childCP = childFI.canonicalPath;
              String destChildCP = childCP.replace(canonicalPath, destCanonicalPath);

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

  /**
   * According to jpf-bfs configuration renaming of an opened file should either
   * be an error or produce warning. This method checks jpf-bfs config and either
   * log a warning or throw an exception.
   */
  private native void checkRenameConfig();

  /**
   * Get FileInfo with data about a file with specified canonical path. If FI was
   * already created it will be returned, otherwise info about this file will be read
   * from native FS.
   * @param canonicaPath - canonical path of file
   * @return FileInfo about a file if it was created during SUT run, or it exists on
   * a native FS and wasn't deleted. Otherwise return null.
   */
  public static synchronized FileInfo getFileInfo(String canonicaPath) {
    System.out.println("Request for " + canonicaPath + " FileInfo");
    FileInfo newFI = getFileInfoByCanonicalPath(canonicaPath);

    if (newFI != null) {
      System.out.println("Found in FileInfo DS");
      return newFI;
    }

    newFI = createNewFileInfo(canonicaPath);
    if (newFI != null) {
      System.out.println("Found in native FS");
      newFI.fileState.setChildren(new ArrayList<FileInfo>());
    }
    else {
      System.out.println("Found no FileInfo");
    }
    
    if (newFI == null) {
      newFI = new FileInfo(canonicaPath, true);
      newFI.fileState.setDoesExist(false);
    }

    int fileMode = FileAccessInfo.getFileAccessMode(canonicaPath);
    newFI.fileState.setFileAccessMode(fileMode);
    addNewFI(newFI);
    
    System.out.println("New FI is " + newFI);
    
    return newFI;

  }

  /**
   * Add new FileInfo
   * @param newFI - FileInfo to add
   */
  private static void addNewFI(FileInfo newFI) {
    String cp = newFI.canonicalPath;
    String parentCP = getParent(cp);

    if (parentCP != null) {
      FileInfo parentFI = getFileInfo(parentCP);

      /**
       * If parent doesn't exist or was deleted or removed current file should be
       * marked as deleted.
       */
      if (parentFI == null || !parentFI.exists()) {
        System.out.println(parentFI.canonicalPath + " was deleted, so " + newFI.canonicalPath + " is deleted too");
        newFI.fileState.setDoesExist(false);
      }
    }

    // Find this file's parent
    for (FileInfo potentialParent : fileInfos) {
      if (potentialParent.canonicalPath.equals(parentCP)) {
        System.out.println("Found parent " + potentialParent.canonicalPath + " for " + newFI.canonicalPath);

        potentialParent.fileState.addChild(newFI);
        break;
      }
    }

    // Find this file's children
    for (FileInfo potentialChild : fileInfos) {
      String potentialChildParentCP = getParent(potentialChild.canonicalPath);

      if (newFI.canonicalPath.equals(potentialChildParentCP)) {
        System.out.println("Found child " + potentialChild.canonicalPath + " for " + newFI.canonicalPath);
        newFI.fileState.addChild(potentialChild);
      }
    }

    fileInfos.add(newFI);
  }

  /**
   * Create new file with a specified canonical path
   * @param canonicalPath - canonical path of a new file
   * @return true if file was created, false otherwise.
   */
  public boolean createNewFile(String canonicalPath) {
    System.out.println("Attempt to create new FileInfo for a file " + canonicalPath);    

    if (!fileState.exists()) {
      String parentCP = getParent(canonicalPath);
      FileInfo parentFI = getFileInfo(parentCP);

      if (parentFI != null && parentFI.fileState.exists()) {        
          fileState.setIsDir(false);
          fileState.setDoesExist(true);
          fileState.setNativeFSFileName(null);        

        // We need to create file on native FS for read/write operations
        if (fileState.getFileAccessMode() != FileAccessMode.BFS_FILE_ACCESS) {
          String tempFile = createFileForNativeAccess();
          fileState.setNativeFSFileName(tempFile);
        }

        System.out.println("New file is " + this);
        
        return true;
      }
    }

    return false;
  }

  /**
   * Create FileInfo for a file with specified canonica path with data read from
   * a file on a native file system.
   * @param canonicalPath - canonical path of a file.
   * @return FileInfo for a specified file if one exists, null otherwise.
   */
  private static native FileInfo createNewFileInfo(String canonicalPath);

  /**
   * Get file name of a parent for a file with specified name of a file.
   * @param filename
   * @return file name of a parent or null.
   */
  private static native String getParent(String filename);

  /**
   *
   * @param canonicalPath
   */
  private static void createNewFileFI(String canonicalPath) {
    FileInfo fi = new FileInfo(canonicalPath, false);
    fileInfos.add(fi);
  }

  /**
   * Create a directory with specified canonical path.
   * @param canonicalPath - canonical path of a directory to create.
   * @return true if directory was created, false otherwise
   */
  public boolean mkdir() {
    System.out.println("Attempt to create new FileInfo for a dir " + canonicalPath);

    if (!fileState.exists()) {
      String parentCP = getParent(canonicalPath);
      FileInfo parentFI = getFileInfo(parentCP);

      if (parentFI != null && parentFI.fileState.exists()) {        
        fileState.setIsDir(true);
        fileState.setDoesExist(true);
        fileState.setNativeFSFileName(null);
        
        return true;
      }
    }

    return false;
  }

  /**
   * Create all directories that needed to be created for a directory with
   * given canonical path.
   * @param canonicalPath - canonical path of a directory that should be created.
   * @param firstCall - true if this call to this function is the first one, false
   * otherwise.
   * @return true if all directories were created, false otherwise.
   */
  public boolean mkdirs(boolean firstCall) {
    System.out.println("FileInfo.mkdirs " + this);
    
    // File not exists
    
    if (!fileState.exists()) {
      // File was deleted
      String parent = getParent(canonicalPath);
      FileInfo parentFI = getFileInfo(parent);
      
      if (parentFI.mkdirs(false)) {
        fileState.setDoesExist(true);
        fileState.setChildren(new ArrayList<FileInfo>());
        fileState.setIsDir(true);
        fileState.setNativeFSFileName(null);
        
        parentFI.fileState.addChild(this);

        return true;
      } else {
        return false;
      }     
    } else if (firstCall) {
      // If File.mkdirs() is called to create existing directory it should return
      // false
      return false;
    }  else if (!fileState.isDir()) {
      // FS object is a file
      return false;
    }

    return true;
  }

  /**
   * Find and get FileInfo for a file with specified canonical path.
   * @param canonicalPath - canonical path of a directory
   * @return FileInfo if one for a file with specified canonical path was created,
   * null otherwise.
   */
  private static FileInfo getFileInfoByCanonicalPath(String canonicalPath) {
    for (FileInfo fi : fileInfos) {
      if (fi.canonicalPath.equals(canonicalPath)) {
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

        FileInfo newFileFI = getFileInfoByCanonicalPath(newFileCP);

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

  // <2do> REMOVE WHEN DEVELOPING IS DONE
  private static void printCurrentFileInfos() {
    System.out.println("\nCurrent FIs:");
    for (FileInfo fileI : fileInfos) {
       System.out.println(fileI);
    }
  }

  /**
   * Create temp file for a native access mode. If a canonical path of a new file
   * matches native-access mode it should be created on a native FS. In order to 
   * not change FS during SUT run a temp file in special folder is created and 
   * canonical path of such a file is returned. 
   * 
   * @return canonical path of a temp file that can be used for a native access.
   */
  private static native String createFileForNativeAccess();

  /**
   * Check if specified canonical path is a file system root on this system.
   * @param canonicalPath - canonical path of a file.
   * @return true if it's a file system's root, false otherwise.
   */
  private static native boolean isFSRoot(String canonicalPath);

  /**
   * Get canonical paths of a file's children on a native FS
   * @param canonicalPath - canonical path of a file
   * @return array of canonical paths of file's children if file exists on native
   * FS. If file doesn't exists return null.
   */
  private static native String[] getChildrenCPs(String canonicalPath);

  @Override
  public String toString() {
    String result = "CP: " + canonicalPath + "; ";
    result += "FS: " + fileState;

    return result;
  }


}
