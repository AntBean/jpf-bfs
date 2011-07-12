//
// Copyright  (C) 20037 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
//  (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
//  (NOSA), version 1.3.  The NOSA has been approved by the Open Source
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

/**
 * Some file should be read/written from BFS, but operations with other should 
 * avoid BFS overhead and use native FS. 
 * This class returns file access mode that is set in JPF config
 *
 * @author Ivan Mushketik
 */
class FileAccessInfo {

  /**
   * Check config to find out what file access mode should be used to perform
   * operations with a file with specified canonical path.
   * 
   * @param canonicalPath - canonical path of a file
   * @return one of the constants from FileAccessMode
   * 
   * @see FileAccessMode
   */
  static native int getFileAccessMode(String canonicalPath);
}
