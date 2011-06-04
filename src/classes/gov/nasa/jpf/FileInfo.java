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


  public boolean delete() {

    if (fileState.isExists()) {
      fileState.setIsExists(false);

      return true;
    }

    return false;
  }

  public boolean exists() {
    return fileState.isExists();
  }

  public static FileInfo getFileInfo(String filename) {
    FileInfo fi = getFileInfoByCannonicalPath(filename);

    if (fi != null) {
      return fi;
    }

    fi = createNewFileInfo(filename);
    if (fi != null) {      
      fileInfos.add(fi);
    }

    return fi;
  }

  private static native FileInfo createNewFileInfo(String fileName);

  private static FileInfo getFileInfoByCannonicalPath(String fileName) {

    for (FileInfo fi : fileInfos) {
      if (fi.cannonicalPath.equals(fileName)) {
        return fi;
      }
    }

    return null;
  }

  @Override
  public String toString() {
    String result = "CP: " + cannonicalPath + "; ";
    result += "FS: " + fileState;


    return result;

  }

}
