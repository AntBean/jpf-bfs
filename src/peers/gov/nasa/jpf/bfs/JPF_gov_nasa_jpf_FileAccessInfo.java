//
// Copyright  (C) 2011 United States Government as represented by the
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
package gov.nasa.jpf.bfs;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.StringSetMatcher;

/**
 *
 * @author Ivan Mushketik
 */
public class JPF_gov_nasa_jpf_FileAccessInfo {

  public static final int BFS_INCLUDE = 1;
  public static final int BFS_EXCLUDE = 2;
  public static final int BFS_IGNORE_WRITE = 3;

  public static final String BFS_EXCLUDE_KEY = "jpf-bfs.bfs.exclude";
  public static final String BFS_IGNORE_WRITE_KEY = "jpf-bfs.bfs.ignore_write";

  private static StringSetMatcher bfsExcludeMatcher;
  private static StringSetMatcher bfsIgnoreWriteMatcher;

  public static void init (Config config) {
    String[] bfsExclude = config.getStringArray(BFS_EXCLUDE_KEY);
    String[] bfsIgnoreWrite = config.getStringArray(BFS_IGNORE_WRITE_KEY);

    if (bfsExclude != null) {
      /** @jpfoption jpf-bfs.bfs.exclude : List<RegExp> - if file's canonical path matches 
       * any of specified here regular expressions, all read/write operations with this file
       * will be performed natively in unbacktrackable way. */
      bfsExcludeMatcher = new StringSetMatcher(bfsExclude);
    }

    if (bfsIgnoreWrite != null) {
      /** @jpfoption jpf-bfs.bfs.ignore_write : List<RegExp> - if file's canonical path matches 
       * any of specified here regular expressions, all write operations will be ignored. Result
       * of read operations is specified by @jpfoption jpf-bfs.opened_delete */
      bfsIgnoreWriteMatcher = new StringSetMatcher(bfsIgnoreWrite);
    }
  }

  public static int getFileAccessMode__Ljava_lang_String_2__I(MJIEnv env, int classRef, int canonicalPathRef) {
    String canonicalPath = env.getStringObject(canonicalPathRef);

    if (bfsExcludeMatcher != null && bfsExcludeMatcher.matchesAny(canonicalPath)) {
      return BFS_EXCLUDE;
    } else if (bfsIgnoreWriteMatcher != null && bfsIgnoreWriteMatcher.matchesAny(canonicalPath)) {
      return BFS_IGNORE_WRITE;
    } else {
      return BFS_INCLUDE;
    }

  }
}
