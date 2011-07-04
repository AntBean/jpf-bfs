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

package gov.nasa.jpf.bfs;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MJIEnv;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan Mushketik
 */
public class JPF_gov_nasa_jpf_FileInfo {

  private static final Logger logger = JPF.getLogger("gov.nasa.jpf.FileInfo");

  private static final String OPENED_DELETE_KEY = "jpf-bfs.opened-delete";
  private static final String OPENED_RENAME_KEY = "jpf-bfs.opened-rename";

  private static int onOpenedDelete = FSMode.NOTHING;
  private static int onOpenedRename = FSMode.NOTHING;

  private static File cacheDir;

  static {
    Config config = JVM.getVM().getConfig();
    onOpenedDelete = FSMode.parseOnOpened(config, OPENED_DELETE_KEY);
    onOpenedRename = FSMode.parseOnOpened(config, OPENED_RENAME_KEY);

    cacheDir = BFSUtils.getCacheDir();
  }
  
  private static final int INITIAL_SIZE = 1;

  public static int createNewFileInfo__Ljava_lang_String_2__Lgov_nasa_jpf_FileInfo_2(MJIEnv env, int clsRef, int fileNameRef) {
    
    String fileName = env.getStringObject(fileNameRef);

    System.out.println("Searching for info for " + fileName + " on native FS");
    File file = new File(fileName);

    if (file.exists()) {
      int fiRef = env.newObject("gov.nasa.jpf.FileInfo");
      int cpRef = env.newString(fileName);
      env.setReferenceField(fiRef, "canonicalPath", cpRef);
      int fsRef = createFileState(env, file, cpRef);

      env.setReferenceField(fiRef, "fileState", fsRef);

      return fiRef;
    }
    return MJIEnv.NULL;
  }

  private static int createFileState(MJIEnv env, File file, int cpRef) {
    int fsRef = env.newObject("gov.nasa.jpf.FileState");
    
    boolean isDir = file.isDirectory();
    if (isDir) {
      int fis = env.newObjectArray("gov.nasa.jpf.FileInfo", INITIAL_SIZE);
      env.setReferenceField(fsRef, "children", fis);
    }
    
    env.setBooleanField(fsRef, "isDir", isDir);
    env.setLongField(fsRef, "length", file.length());
    env.setBooleanField(fsRef, "doesExist", true);
    env.setIntField(fsRef, "openCnt", 0);
    env.setReferenceField(fsRef, "nativeFSFileName", cpRef);
    env.setLongField(fsRef, "lastModified", file.lastModified());
    
    env.setBooleanField(fsRef, "isReadableForSUT", file.canRead());
    env.setBooleanField(fsRef, "isWritableForSUT", file.canWrite());
    env.setBooleanField(fsRef, "isExecutableForSUT", file.canExecute());
    
    return fsRef;
  }

  
  
  //  public boolean delete() {
  //    System.out.println("FileInfo.delete() " + this);    
  //    
  //    // File can be deleted if it exists or it's not a file system root
  //    if (fileState.exists() && !isFSRoot(canonicalPath)) {  
  //      String parentCP = getParent(canonicalPath);
  //      FileInfo parentFI = getFileInfoByCanonicalPath(parentCP);
  //      
  //      // <2do> Not sure how to check rights for removing file withouth knowledge
  //      // about if SUT is a file owner (requires JDK7)
  //      if (parentFI.fileState.isWritableForSUT()) {
  //        boolean toDelete = false;
  //        
  //        // If it's a file we need only to check if deleting opened files is
  //        // permited
  //        if (!fileState.isDir()) { 
  //          checkDeleteConfig();
  //          toDelete = true;
  //        } else {
  //          // Only empty directories can be deleted
  //          String[] childs = list();
  //          if (childs.length == 0) {
  //            toDelete = true;
  //          }
  //        }
  //        
  //        if (toDelete) {
  //          fileState.setDoesExist(false);
  //          return true;
  //        }        
  //      }
  //    }
  //
  //    return false;
  //  }
  // <2do> This native method isn't finished. It's broken.
  public boolean delete____Z(MJIEnv env, int objRef) {         
    int fsRef = env.getReferenceField(objRef, "fileState");    
    String canonicalPath = env.getStringField(objRef, "canonicalPath"); 
    
    System.out.println("FileInfo.delete() " + canonicalPath);   
    
    //    // File can be deleted if it exists or it's not a file system root
    //    if (fileState.exists() && !isFSRoot(canonicalPath)) {  
    if (fileExists(env, fsRef) && !isFSRoot(canonicalPath)) {         
      String parentCP = getParent(canonicalPath);
      // FileInfo parentFI = getFileInfoByCanonicalPath(parentCP);
      int parentFIRef = getFileInfoByCanonicalPath__Ljava_lang_String_2__Lgov_nasa_jpf_FileInfo_2(env, objRef, env.newString(parentCP));
      int parentFIFSRef = env.getReferenceField(parentFIRef, "fileState");
      
      if (isWrittableForSUT(env, parentFIFSRef)) {
        boolean toDelete = false;
        
        if (!isDir(env, fsRef)) {
          checkDeleteConfig____V(env, objRef);
          toDelete = true;
        } else  {
          String[] children = list(env, objRef);
          if (children.length == 0) {
            toDelete = true;
          }
        }
         
        if (toDelete) {
          deleteFile(env, fsRef);
          return true;
        }        
      }
    }
    
    return false;    
  }
  
  private static void deleteFile(MJIEnv env, int fsRef) {
    env.setBooleanField(fsRef, "doesExist", false);
  }
  
  private static boolean isReadableForSUT(MJIEnv env, int fiRef) {
    return env.getBooleanField(fiRef, "isReadableForSUT");
  }
  
  private static boolean isWrittableForSUT(MJIEnv env, int fiRef) {
    return env.getBooleanField(fiRef, "isWritableForSUT");
  }
  
  private static boolean isExecutableForSUT(MJIEnv env, int fiRef) {
    return env.getBooleanField(fiRef, "isExecutableForSUT");
  }
  
  private static boolean isDir(MJIEnv env, int fiRef) {
    return env.getBooleanField(fiRef, "isDir");
  }
    
  private static boolean fileExists(MJIEnv env, int fsRef) {
    return env.getBooleanField(fsRef, "doesExist");
  }
  
  private static boolean nativeFileExists(MJIEnv env, int fsRef) {
    return env.getReferenceField(fsRef, "nativeFSFileName") != MJIEnv.NULL;
  }
  
  //  public String[] list() {
  //    System.out.println("FileInfo.list() for " + this);
  //
  //    if (fileState.isDir() && fileState.exists()) {
  //      if (fileState.isReadableForSUT()) {// && fileState.isExecutableForSUT()) {
  //        String[] nativeFSChildren = {};
  //
  //        // If directory existed on a filesystem before SUT run, we can read child files
  //        // that exist on a native FS
  //        if (fileState.getNativeFSFileName() != null) {
  //          nativeFSChildren = getChildrenCPs(fileState.getNativeFSFileName());
  //        }
  //
  //        System.out.println("Native FS children: ");
  //        for (String childName : nativeFSChildren) {
  //          System.out.print(childName + ", ");
  //        }
  //        System.out.println(";");
  //
  //        HashSet<String> set = new HashSet<String>();
  //
  //        for (String fsChild : nativeFSChildren) {
  //          set.add(fsChild);
  //        }
  //        
  //        FileInfo[] children = fileState.getChildren();
  //        int numberOfChidren = fileState.numberOfChildren();
  //        // Add new children to child's set remove children that were deleted by SUT
  //        for (int i = 0; i < numberOfChidren; i++) {
  //          FileInfo child = children[i];
  //          
  //          if (child.fileState.exists()) {
  //            set.add(child.canonicalPath);
  //            System.out.println("Found new existing child " + child.canonicalPath);
  //          } else {
  //            set.remove(child.canonicalPath);
  //            System.out.println("Found new deleted child " + child.canonicalPath);
  //          }
  //        }
  //
  //        System.out.println("Current children: ");
  //        for (String childName : set) {
  //          System.out.print(childName + ", ");
  //        }
  //        System.out.println(";");
  //
  //        String[] currentChildren = new String[set.size()];
  //        return set.toArray(currentChildren);
  //      }
  //    }
  //
  //    // FileInfo represents not a directory, or doesn't exist, or SUT has no rights
  //    return null;
  //  }
  public static int list_____3Ljava_lang_String_2(MJIEnv env, int objRef) {
    String[] children = list(env, objRef);
    
    if (children != null) {
      return env.newStringArray(children);
    }
    
    return MJIEnv.NULL;
  }
    
  public static String[] list(MJIEnv env, int objRef) {
    int fsRef = env.getReferenceField(objRef, "fileState");
    String canonicalPath = env.getStringField(objRef, "canonicalPath"); 
    // if (fileState.isDir() && fileState.exists()) {
    if (isDir(env, fsRef) && fileExists(env, fsRef)) {
      // if (fileState.isReadableForSUT()) {// && fileState.isExecutableForSUT()) {
      if (isReadableForSUT(env, fsRef)) {
        String[] nativeFSChildren = {};

        // If directory existed on a filesystem before SUT run, we can read child files
        // that exist on a native FS
        if (nativeFileExists(env, fsRef)) {
          nativeFSChildren = getChildrenCPs(canonicalPath);
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

        // FileInfo[] children = fileState.getChildren();
        int childrenRef = env.getReferenceField(fsRef, "children");
        // int numberOfChidren = fileState.numberOfChildren();
        int numberOfChildren = env.getIntField(fsRef, "numberOfChildren");
        // Add new children to child's set remove children that were deleted by SUT
        for (int i = 0; i < numberOfChildren; i++) {
          // FileInfo child = children[i];
          int childRef = env.getReferenceArrayElement(childrenRef, i);

          int childFSRef = env.getReferenceField(childRef, "fileState");
          String childCanonicalPath = env.getStringField(childRef, "canonicalPath");

          if (fileExists(env, childFSRef)) {
            set.add(childCanonicalPath);
            System.out.println("Found new existing child " + childCanonicalPath);
          } else {
            set.remove(childCanonicalPath);
            System.out.println("Found new deleted child " + childCanonicalPath);
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
    }
    // FileInfo represents not a directory, or doesn't exist, or SUT has no rights
    return null;
  }

  public static int getParent__Ljava_lang_String_2__Ljava_lang_String_2(MJIEnv env, int clsRef, int fileNameRef) {
    String fileName = env.getStringObject(fileNameRef);
    File file = new File(fileName);

    return env.newString(file.getParent());
  }
  
  private static String getParent(String canonicalPath) {
    File file = new File(canonicalPath);

    return file.getParent();
  }

  public static boolean isFSRoot(String canonicalPath) {
    File file = new File(canonicalPath);

    return file.getParent() == null;
  }
  
  public static boolean isFSRoot__Ljava_lang_String_2__Z(MJIEnv env, int clsRef, int fileNameRef) {
    String fileName = env.getStringObject(fileNameRef);
    File file = new File(fileName);

    return file.getParent() == null;
  }

  public static String[] getChildrenCPs(String canonicalPath) {    

    File dir = new File(canonicalPath);
    String[] childs = dir.list();
    String separator = System.getProperty("file.separator");

    for (int i = 0; i < childs.length; i++) {
      childs[i] = canonicalPath + separator + childs[i];
    }

    return childs;
  }
  
  public static void checkDeleteConfig____V(MJIEnv env, int objRef) {
    int fileStateRef = env.getReferenceField(objRef, "fileState");
    int openCnt = env.getIntField(fileStateRef, "openCnt");

    // File is going to be deleted when it's opened
    if (openCnt > 0) {
      String fileCP = env.getStringField(objRef, "canonicalPath");

      if (onOpenedDelete == FSMode.WARNING) {
        logger.log(Level.WARNING, "File {0} deleted while opened", fileCP);
      } else if (onOpenedDelete == FSMode.ERROR) {
        throw new JPFException("File " + fileCP + " deleted while opened");
      }
    }
  }

  public static void checkRenameConfig____V(MJIEnv env, int objRef) {
    int fileStateRef = env.getReferenceField(objRef, "fileState");
    int openCnt = env.getIntField(fileStateRef, "openCnt");

    // File is going to be deleted when it's opened
    if (openCnt > 0) {
      String fileCP = env.getStringField(objRef, "canonicalPath");

      if (onOpenedRename == FSMode.WARNING) {
        logger.log(Level.WARNING, "File {0} renamed while opened", fileCP);
      } else if (onOpenedRename == FSMode.ERROR) {
        throw new JPFException("File " + fileCP + " renamed while opened");
      }
    }
  }

  public static int createFileForNativeAccess____Ljava_lang_String_2(MJIEnv env, int classRef) throws IOException {
    File tempFile = File.createTempFile("file", "forNativeAccess", cacheDir);
    int tempFileCPRef = env.newString(tempFile.getCanonicalPath());

    return tempFileCPRef;
  }
  
  public static int getFileInfoByCanonicalPath__Ljava_lang_String_2__Lgov_nasa_jpf_FileInfo_2(MJIEnv env, int classRef, int canonicalPathRef) {
    int pos = findFileInfo__Ljava_lang_String_2__I(env, classRef, canonicalPathRef);
    if (pos >= 0) {
      int fileInfosArrayRef = env.getStaticReferenceField(classRef, "fileInfos");
      return env.getReferenceArrayElement(fileInfosArrayRef, pos);
    } else {
      return MJIEnv.NULL;
    }
    
  }
  
//  static int findFileInfo(String cannonicalPath) {    
//    int left = 0;
//    int right = numberOfFileInfos - 1;
//    
//    while (left <= right) {
//      int m = (left + right) /2;
//      
//      int sign = fileInfos[m].canonicalPath.compareTo(cannonicalPath);
//      
//      if (sign > 0) {
//        right = m - 1;
//      } else if (sign < 0) {
//        left = m + 1;
//      } else {
//        return m;
//      }
//    }
//    
//    return -left - 1;
//  }
  public static int findFileInfo__Ljava_lang_String_2__I(MJIEnv env, int classRef, int canonicalPathRef) {
    String canonicalPath = env.getStringObject(canonicalPathRef);
    int left = 0;
    //    int right = numberOfFileInfos - 1;
    int numberOfFileInfos = env.getStaticIntField(classRef, "numberOfFileInfos");
    int right = numberOfFileInfos - 1;
   
    while (left <= right) {
      int m = (left + right) / 2;    
    //      int sign = fileInfos[m].canonicalPath.compareTo(cannonicalPath);
      int fileInfosArrayRef = env.getStaticReferenceField(classRef, "fileInfos");
      int mFileInfo = env.getReferenceArrayElement(fileInfosArrayRef, m);
      String mCanonicalPath = env.getStringField(mFileInfo, "canonicalPath") ;
      
      int sign = mCanonicalPath.compareTo(canonicalPath);
   
      if (sign > 0) {
        right = m - 1;
      } else if (sign < 0) {
        left = m + 1;
      } else {
        return m;
      }
    }
    
    return -left - 1;
  }

  

 

}
