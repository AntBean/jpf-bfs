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

import gov.nasa.jpf.jvm.MJIEnv;
import java.io.File;

/**
 *
 * @author Ivan Mushketik
 */
public class JPF_gov_nasa_jpf_FileInfo {
  public static int createNewFileInfo__Ljava_lang_String_2__Lgov_nasa_jpf_FileInfo_2(MJIEnv env, int clsRef, int fileNameRef) {
    
    String fileName = env.getStringObject(fileNameRef);

    System.out.println("Searching for info for " + fileName + " on native FS");
    File file = new File(fileName);

    if (file.exists()) {
      int fiRef = env.newObject("gov.nasa.jpf.FileInfo");
      int cpRef = env.newString(fileName);
      env.setReferenceField(fiRef, "cannonicalPath", cpRef);

      int fsRef = env.newObject("gov.nasa.jpf.FileState");
      env.setLongField(fsRef, "length", file.length());
      env.setBooleanField(fsRef, "isDir", file.isDirectory());
      env.setBooleanField(fsRef, "isExist", true);
      env.setIntField(fsRef, "openCnt", 0);
      env.setReferenceField(fsRef, "nativeFSFileName", cpRef);

      env.setReferenceField(fiRef, "fileState", fsRef);

      return fiRef;
    }
    return MJIEnv.NULL;
  }

  public static int getParentCP__Ljava_lang_String_2__Ljava_lang_String_2(MJIEnv env, int clsRef, int fileNameRef) {
    String fileName = env.getStringObject(fileNameRef);
    File file = new File(fileName);

    return env.newString(file.getParent());
  }
}
