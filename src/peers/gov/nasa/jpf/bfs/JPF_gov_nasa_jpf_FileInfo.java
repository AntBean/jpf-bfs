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
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.ReferenceArrayFields;
import gov.nasa.jpf.util.JPFLogger;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 *
 * @author Ivan Mushketik
 */
public class JPF_gov_nasa_jpf_FileInfo {

  private static final JPFLogger logger = JPF.getLogger("gov.nasa.jpf.FileInfo");

  private static final String OPENED_DELETE_KEY = "jpf-bfs.opened_delete";
  private static final String OPENED_RENAME_KEY = "jpf-bfs.opened_rename";

  private static int onOpenedDelete = FSMode.NOTHING;
  private static int onOpenedRename = FSMode.NOTHING;

  private static File cacheDir;

  public static void init(Config config) {
    /** @jpfoption jpf-bfs.opened_delete : String {"error", "warning", "nothing"} - what to do if
     * if opened file is deleted. "error" - throw java.io.IOException; "warning" - log warning; 
     * "nothing" - just do nothing */
    onOpenedDelete = FSMode.parseOnOpened(config, OPENED_DELETE_KEY);
    /** @jpfoption jpf-bfs.opened_rename : String {"error", "warning", "nothing"} - what to do if
     * if opened file is renamed. "error" - throw java.io.IOException; "warning" - log warning; 
     * "nothing" - just do nothing */
    onOpenedRename = FSMode.parseOnOpened(config, OPENED_RENAME_KEY);

    cacheDir = BFSUtils.getCacheDir(config);
  }
  
  private static final int INITIAL_SIZE = 1;

  public static int createNewFileInfo__Ljava_lang_String_2__Lgov_nasa_jpf_FileInfo_2(MJIEnv env, int clsRef, int fileNameRef) {
    
    String fileName = env.getStringObject(fileNameRef);    
    logger.info("Searching for info for ", fileName, " on native FS");
    
    File file = new File(fileName);

    if (file.exists()) {
      logger.info("Found file on a native file system");
      int fiRef = env.newObject("gov.nasa.jpf.FileInfo");
      int cpRef = env.newString(fileName);
      env.setReferenceField(fiRef, "canonicalPath", cpRef);
      
      int fsRef = createFileState(env, file, cpRef);
      env.setReferenceField(fiRef, "fileState", fsRef);
      
      boolean isDir = file.isDirectory();
      if (isDir) {
        int fis = env.newObjectArray("gov.nasa.jpf.FileInfo", INITIAL_SIZE);
        env.setReferenceField(fiRef, "children", fis);
      }

      return fiRef;
    }
    return MJIEnv.NULL;
  }

  private static int createFileState(MJIEnv env, File file, int cpRef) {
    int fsRef = env.newObject("gov.nasa.jpf.FileState");
    
    env.setBooleanField(fsRef, "isDir", file.isDirectory());
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
  
  private static boolean isReadableForSUT(MJIEnv env, int fiRef) {
    return env.getBooleanField(fiRef, "isReadableForSUT");
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
  //          } else {
  //            set.remove(child.canonicalPath);
  //          }
  //        }
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
        
        HashSet<String> set = new HashSet<String>();

        for (String fsChild : nativeFSChildren) {
          set.add(fsChild);
        }

        // FileInfo[] children = fileState.getChildren();
        int childrenRef = env.getReferenceField(objRef, "children");
        // int numberOfChidren = fileState.numberOfChildren();
        int numberOfChildren = env.getIntField(objRef, "numberOfChildren");
        // Add new children to child's set remove children that were deleted by SUT
        for (int i = 0; i < numberOfChildren; i++) {
          // FileInfo child = children[i];
          int childRef = env.getReferenceArrayElement(childrenRef, i);

          int childFSRef = env.getReferenceField(childRef, "fileState");
          String childCanonicalPath = env.getStringField(childRef, "canonicalPath");

          if (fileExists(env, childFSRef)) {
            set.add(childCanonicalPath);
          } else {
            set.remove(childCanonicalPath);
          }          
        }

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
        logger.warning("File ", fileCP, " deleted while opened");
      } else if (onOpenedDelete == FSMode.ERROR) {
        env.throwException("java.io.IOException", "File " + fileCP + " deleted while opened");
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
        logger.warning("File ", fileCP, " renamed while opened");
      } else if (onOpenedRename == FSMode.ERROR) {
        env.throwException("java.io.IOException", "File " + fileCP + " renamed while opened");
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
    
//  private static void addFileInfoToArray(FileInfo fi) {
//    int pos = findFileInfo(fi.canonicalPath);   
//  
//    int insertPos = -(pos + 1);
//    
//    if (numberOfFileInfos == fileInfos.length) {
//      FileInfo[] newFileInfos = new FileInfo[fileInfos.length * 2];
//      System.arraycopy(fileInfos, 0, newFileInfos, 0, fileInfos.length);
//      
//      fileInfos = newFileInfos;
//    }
//    
//    for (int i = numberOfFileInfos; i != insertPos; i--) {
//      fileInfos[i] = fileInfos[i - 1];
//    }
//    
//    fileInfos[insertPos] = fi;
//    numberOfFileInfos++;
//  }
  public static void addFileInfoToArray__Lgov_nasa_jpf_FileInfo_2__V(MJIEnv env, int classRef, int fileInfoRef) {
    int canonicalPathRef = env.getReferenceField(fileInfoRef, "canonicalPath");
    
    int pos = findFileInfo__Ljava_lang_String_2__I(env, classRef, canonicalPathRef);
    
    if (pos < 0) {
      int insertPos = -(pos + 1);
      
      int fileInfosArrayRef = env.getStaticReferenceField(classRef, "fileInfos");
      Fields fields = env.getHeap().get(fileInfosArrayRef).getFields();
      int fileInfosFields[] = ((ReferenceArrayFields) fields).asReferenceArray();
      
      int fileInfosArrayLength = env.getArrayLength(fileInfosArrayRef);
      int numberOfFileInfos = env.getStaticIntField(classRef, "numberOfFileInfos");
      
      if (numberOfFileInfos == fileInfosArrayLength) {       
        
        int newFileInfosRef = env.newObjectArray("gov.nasa.jpf.FileInfo", numberOfFileInfos * 2);
        Fields newFields = env.getHeap().get(newFileInfosRef).getFields();
        int newFileInfosFields[] = ((ReferenceArrayFields) newFields).asReferenceArray();
        
        System.arraycopy(fileInfosFields, 0, newFileInfosFields, 0, numberOfFileInfos);
        env.setStaticReferenceField(classRef, "fileInfos", newFileInfosRef);
        
        fileInfosArrayRef = newFileInfosRef;
        fileInfosFields = newFileInfosFields;
      }
      
      for (int i = numberOfFileInfos; i != insertPos; i--) {
        fileInfosFields[i] = fileInfosFields[i - 1];
      }
      
      fileInfosFields[insertPos] = fileInfoRef;
      env.setStaticIntField(classRef, "numberOfFileInfos", numberOfFileInfos + 1);
      
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
