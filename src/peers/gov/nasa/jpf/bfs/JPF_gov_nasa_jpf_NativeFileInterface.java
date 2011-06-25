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
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.JPFLogger;

import java.io.*;
import java.util.HashMap;

/**
 * @author Ivan Mushketik
 */

public class JPF_gov_nasa_jpf_NativeFileInterface {
  private static final String IGNORE_WRITE_MODE_FIELD = "ignoreWriteMode";
  private static final String FILEPOS_FIELD = "filePos";
  private static final String FILE_STATE_FIELD = "fileState";

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.NativeFileInterface");
  static final HashMap<Integer, RandomAccessFile> rafs = new HashMap<Integer, RandomAccessFile>();


  private static final String IGNORE_WRITE_FILE_READ_KEY = "jpf-bfs.ignore-write-file-read";
  private static int onIgnoreWriteFileRead = FSMode.NOTHING;

  static {
    Config config = JVM.getVM().getConfig();
    onIgnoreWriteFileRead = FSMode.parseOnOpened(config, IGNORE_WRITE_FILE_READ_KEY);
  }

  public static void $init__Lgov_nasa_jpf_FileState_2Z__V(MJIEnv env, int objref, int fileStateRef, boolean ignoreWriteMode) {
    try {
      String canonicalPath = env.getStringField(fileStateRef, "nativeFSFileName");
      RandomAccessFile raf = new RandomAccessFile(canonicalPath, "rws");
      rafs.put(objref, raf);

      env.setReferenceField(objref, FILE_STATE_FIELD, fileStateRef);
      env.setBooleanField(objref, IGNORE_WRITE_MODE_FIELD, ignoreWriteMode);

    } catch (FileNotFoundException ex) {
      throw new JPFException(ex);
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

    if (!ignoreWriteMode || onIgnoreWriteFileRead != FSMode.ERROR) {
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
      throw new JPFException("Attempt to read file with ignore write mode");
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
