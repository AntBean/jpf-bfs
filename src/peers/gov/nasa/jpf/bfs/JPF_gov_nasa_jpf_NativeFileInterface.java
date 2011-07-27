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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.annotation.JPFOptions;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.JPFLogger;

import java.io.*;
import java.util.HashMap;

/**
 * @author Ivan Mushketik
 */
@JPFOptions({
  @JPFOption(type="String", key="jpf-bfs.ignore_write_file_read", defaultValue="nothing", 
        comment="what to do if a file in 'ignore_write' mode is read. 'error' - throw an java.io.IOException; "
        + "'warning' - log warning; 'nothing' - simply do nothing")
})
public class JPF_gov_nasa_jpf_NativeFileInterface {
  private static final String IGNORE_WRITE_MODE_FIELD = "ignoreWriteMode";
  private static final String FILEPOS_FIELD = "filePos";
  private static final String FILE_STATE_FIELD = "fileState";

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.NativeFileInterface");
  static final HashMap<Integer, RandomAccessFile> rafs = new HashMap<Integer, RandomAccessFile>();


  private static final String IGNORE_WRITE_FILE_READ_KEY = "jpf-bfs.ignore_write_file_read";
  private static FSMode onIgnoreWriteFileRead = FSMode.NOTHING;

  public static void init(Config config) {
     /** @jpfoption jpf-bfs.bfs.ignore_write_file_read : String {"nothing", "warning", "error"} - what to do if 
      * a file in "ignore_write" mode is read. "error" - throw an java.io.IOException; "warning" - log warning; 
      * "nothing" - simply do nothing*/
    onIgnoreWriteFileRead = config.getEnum(IGNORE_WRITE_FILE_READ_KEY, FSMode.values(), FSMode.NOTHING);
  }

  public static void $init__Lgov_nasa_jpf_FileState_2Z__V(MJIEnv env, int objref, int fileStateRef, boolean ignoreWriteMode) {
    try {
      String canonicalPath = env.getStringField(fileStateRef, "nativeFSFileName");
      RandomAccessFile raf = new RandomAccessFile(canonicalPath, "rws");
      rafs.put(objref, raf);

      env.setReferenceField(objref, FILE_STATE_FIELD, fileStateRef);
      env.setBooleanField(objref, IGNORE_WRITE_MODE_FIELD, ignoreWriteMode);

    } catch (FileNotFoundException ex) {
      env.throwException("java.io.IOException", ex.getMessage());
    }
  }

  public static void sync____V(MJIEnv env, int objref) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        raf.getFD().sync();

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
    }
  }
  
  public static int readNative___3BII__I (MJIEnv env, int objref, int bufferRef, int off, int len) {
    boolean ignoreWriteMode = env.getBooleanField(objref, IGNORE_WRITE_MODE_FIELD);

    if (!ignoreWriteMode || (ignoreWriteMode && onIgnoreWriteFileRead != FSMode.ERROR)) {
      if (onIgnoreWriteFileRead == FSMode.WARNING) {
        logger.warning("Attempt to read file with ignore write mode");
      }

      RandomAccessFile raf = rafs.get(objref);
      if (raf != null) {

        try {
          long filePos = env.getLongField(objref, FILEPOS_FIELD);
          raf.seek(filePos);
          byte[] buffer = env.getByteArrayObject(bufferRef);

          int read = raf.read(buffer, off, len);

          return read;
        } catch (IOException ex) {
          env.throwException("java.io.IOException", ex.getMessage());
          return -1;
        }
      } else {
        env.throwException("java.io.IOException", "Bad file descriptor");
        return -1;
      }

    } else {
      env.throwException("java.io.IOException", "Attempt to read file with ignore write mode");
      return -1;
    }
  }
 
  public static int available____I (MJIEnv env, int objref) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        long filePos = env.getLongField(objref, FILEPOS_FIELD);
        long fileLength = raf.length();

        return (int) (fileLength - filePos);

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
        return -1;
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
      return -1;
    }
  }

  public static int writeNative___3BII__I (MJIEnv env, int objref, int bufferRef, int off, int len) {
    boolean ignoreWriteMode = env.getBooleanField(objref, IGNORE_WRITE_MODE_FIELD);

    if (!ignoreWriteMode) {
      RandomAccessFile raf = rafs.get(objref);

      if (raf != null) {
        try {
          long filePos = env.getLongField(objref, FILEPOS_FIELD);
          raf.seek(filePos);
          byte[] buffer = env.getByteArrayObject(bufferRef);

          raf.write(buffer, off, len);

          long newFP = raf.getFilePointer();
          return (int) (newFP - filePos);

        } catch (IOException ex) {
          env.throwException("java.io.IOException", ex.getMessage());
          return -1;
        }
      } else {
        env.throwException("java.io.IOException", "Bad file descriptor");
        return -1;
      }
    }
    return 0;
  }

  public static void nativeClose____V (MJIEnv env, int objref) {
    try {
      RandomAccessFile raf = rafs.get(objref);
      raf.close();
      rafs.remove(objref);
    } catch (IOException ex) {
      env.throwException("java.io.IOException", ex.getMessage());
    }
  }

  public static void setLength__J__V(MJIEnv env, int objref, long newLength) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        long filePos = env.getLongField(objref, FILEPOS_FIELD);
        raf.seek(filePos);

        raf.setLength(newLength);
        env.setLongField(objref, FILEPOS_FIELD, raf.getFilePointer());
        
        // Update file length in BFS
        int fileStateRef = env.getReferenceField(objref, FILE_STATE_FIELD);
        env.setLongField(fileStateRef, "length", newLength);

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
    }
  }

  public static long length____J(MJIEnv env, int objref) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        return raf.length();

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
        return -1;
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
      return -1;
    }
  }
  
  public static long getFilePointer____J(MJIEnv env, int objref) {
    return env.getLongField(objref, FILEPOS_FIELD);
  }
}
