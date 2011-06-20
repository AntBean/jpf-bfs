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
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MJIEnv;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ivan Mushketik
 */
public class JPF_gov_nasa_jpf_FileInfo {

  private static final Logger logger = JPF.getLogger("gov.nasa.jpf.FileInfo");

  private static final int NOTHING = 0;
  private static final int WARNING = 1;
  private static final int ERROR = 2;

  private static final String OPENED_DELETE_KEY = "jpf-bfs.opened-delete";
  private static final String OPENED_RENAME_KEY = "jpf-bfs.opened-rename";

  private static final String DO_NOTHING = "nothing";
  private static final String REPORT_WARNING = "warning";
  private static final String THROW_ERROR = "error";

  private static int onOpenedDelete = NOTHING;
  private static int onOpenedRename = NOTHING;

  static {
    Config config = JVM.getVM().getConfig();
    onOpenedDelete = parseOnOpened(config, OPENED_DELETE_KEY);
    onOpenedRename = parseOnOpened(config, OPENED_RENAME_KEY);
  }

  private static int parseOnOpened(Config config, String key) {
    String value = config.getString(key);

    if (value == null || value.equals(DO_NOTHING)) {
      return NOTHING;
    } else if (value.equals(REPORT_WARNING)) {
      return WARNING;
    } else if (value.equals(THROW_ERROR)) {
      return ERROR;
    } else {
      throw new JPFConfigException("Unexpected value '" + value +
                                   "' for key '" + key + "' in config.");
    }
  }

  public static int createNewFileInfo__Ljava_lang_String_2__Lgov_nasa_jpf_FileInfo_2(MJIEnv env, int clsRef, int fileNameRef) {
    
    String fileName = env.getStringObject(fileNameRef);

    System.out.println("Searching for info for " + fileName + " on native FS");
    File file = new File(fileName);

    if (file.exists()) {
      int fiRef = env.newObject("gov.nasa.jpf.FileInfo");
      int cpRef = env.newString(fileName);
      env.setReferenceField(fiRef, "canonicalPath", cpRef);

      int fsRef = env.newObject("gov.nasa.jpf.FileState");
      env.setLongField(fsRef, "length", file.length());
      env.setBooleanField(fsRef, "isDir", file.isDirectory());
      env.setBooleanField(fsRef, "doesExist", true);
      env.setIntField(fsRef, "openCnt", 0);
      env.setReferenceField(fsRef, "nativeFSFileName", cpRef);
      env.setLongField(fsRef, "lastModified", file.lastModified());

      byte sutRWX = 0;

      sutRWX = (byte) ((file.canRead()) ? (sutRWX | 1) : sutRWX); sutRWX <<= 1;
      sutRWX = (byte) ((file.canWrite()) ? (sutRWX | 1) : sutRWX); sutRWX <<= 1;
      sutRWX = (byte) ((file.canExecute()) ? (sutRWX | 1) : sutRWX);

      env.setByteField(fsRef, "sutRights", sutRWX);

      env.setReferenceField(fiRef, "fileState", fsRef);

      return fiRef;
    }
    return MJIEnv.NULL;
  }

  public static int getParent__Ljava_lang_String_2__Ljava_lang_String_2(MJIEnv env, int clsRef, int fileNameRef) {
    String fileName = env.getStringObject(fileNameRef);
    File file = new File(fileName);

    return env.newString(file.getParent());
  }

  public static boolean isFSRoot__Ljava_lang_String_2__Z(MJIEnv env, int clsRef, int fileNameRef) {
    String fileName = env.getStringObject(fileNameRef);
    File file = new File(fileName);

    return file.getParent() == null;
  }

  public static int getChildrenCPs__Ljava_lang_String_2___3Ljava_lang_String_2(MJIEnv env, int clsRef, int cpRef) {
    String cp = env.getStringObject(cpRef);

    File dir = new File(cp);
    String[] childs = dir.list();
    String separator = System.getProperty("file.separator");

    for (int i = 0; i < childs.length; i++) {
      childs[i] = cp + separator + childs[i];
    }

    return env.newStringArray(childs);
  }

  public static void checkDeleteConfig____V(MJIEnv env, int objRef) {
    int fileStateRef = env.getReferenceField(objRef, "fileState");
    int openCnt = env.getIntField(fileStateRef, "openCnt");

    // File is going to be deleted when it's opened
    if (openCnt > 0) {
      String fileCP = env.getStringField(objRef, "canonicalPath");

      if (onOpenedDelete == WARNING) {
        logger.log(Level.WARNING, "File {0} deleted while opened", fileCP);
      } else if (onOpenedDelete == ERROR) {
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

      if (onOpenedRename == WARNING) {
        logger.log(Level.WARNING, "File {0} renamed while opened", fileCP);
      } else if (onOpenedRename == ERROR) {
        throw new JPFException("File " + fileCP + " renamed while opened");
      }
    }
  }
}
