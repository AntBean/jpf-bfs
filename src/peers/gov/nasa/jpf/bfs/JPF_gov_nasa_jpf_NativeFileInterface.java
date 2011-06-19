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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.util.JPFLogger;

import java.io.*;
import java.util.HashMap;

/**
 * @author Ivan Mushketik
 */

public class JPF_gov_nasa_jpf_NativeFileInterface {

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.NativeFileInterface");
  static final HashMap<Integer, RandomAccessFile> rafs = new HashMap<Integer, RandomAccessFile>();

  public static void $init__Lgov_nasa_jpf_FileState_2__V(MJIEnv env, int objref, int fileStateRef) {
    try {
      String canonicalPath = env.getStringField(fileStateRef, "nativeFSFileName");
      RandomAccessFile raf = new RandomAccessFile(canonicalPath, "rws");
      rafs.put(objref, raf);

      env.setBooleanField(objref, "isOpened", true);
      env.setReferenceField(objref, "fileState", fileStateRef);

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

  public static int read____I (MJIEnv env, int objref) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        long filePos = env.getLongField(objref, "filePos");
        raf.seek(filePos);

        int read = raf.read();
        env.setLongField(objref, "filePos", raf.getFilePointer());
        return read;
      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
        return -1;
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
      return -1;
    }
  }
  
  public static int read___3BII__I (MJIEnv env, int objref, int bufferRef, int off, int len) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {

      try {
        long filePos = env.getLongField(objref, "filePos");
        raf.seek(filePos);
        byte[] buffer = env.getByteArrayObject(bufferRef);

        int read = raf.read(buffer, off, len);
        env.setLongField(objref, "filePos", raf.getFilePointer());
        return read;
      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
        return -1;
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
      return -1;
    }
  }

  public static long skip__J__J(MJIEnv env, int objref, long shift){
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        long filePos = env.getLongField(objref, "filePos");
        long fileLength = raf.length();

        if (shift + filePos > fileLength) {
          filePos = fileLength;

        } else {
          filePos = filePos + shift;
        }

        env.setLongField(objref, "filePos", raf.getFilePointer());
        return shift;
      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
        return -1;
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
      return -1;
    }
  }
 
  public static int available____I (MJIEnv env, int objref) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {


        long filePos = env.getLongField(objref, "filePos");
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
  
  public static void write__I__V (MJIEnv env, int objref, int b) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        long filePos = env.getLongField(objref, "filePos");
        raf.seek(filePos);


        raf.write(b);
        env.setLongField(objref, "filePos", raf.getFilePointer());

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());

      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
    }

  }

  public static void write___3BII__V (MJIEnv env, int objref, int bufferRef, int off, int len) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        long filePos = env.getLongField(objref, "filePos");
        raf.seek(filePos);
        byte[] buffer = env.getByteArrayObject(bufferRef);

        raf.write(buffer, off, len);
        env.setLongField(objref, "filePos", raf.getFilePointer());

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());

      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
    }
  }

  public static void nativeClose____V (MJIEnv env, int objref) {
    boolean isOpened = env.getBooleanField(objref, "isOpened");
    if (isOpened) {
      try {
        RandomAccessFile raf = rafs.get(objref);
        raf.close();
        rafs.remove(objref);
        env.setBooleanField(objref, "isOpened", false);
      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
      }
    }
  }

  public static boolean valid____Z(MJIEnv env, int objref) {
    return false;
  }

  public static void setLength__J__V(MJIEnv env, int objref, long newLength) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {

        long filePos = env.getLongField(objref, "filePos");
        raf.seek(filePos);

        raf.setLength(newLength);
        env.setLongField(objref, "filePos", raf.getFilePointer());

      } catch (IOException ex) {
        env.throwException("java.io.IOException", ex.getMessage());
      }
    } else {
      env.throwException("java.io.IOException", "Bad file descriptor");
    }
  }
  
  public static void seek__J__V(MJIEnv env, int objref, long pos) {
    RandomAccessFile raf = rafs.get(objref);

    if (raf != null) {
      try {
        raf.seek(pos);

        env.setLongField(objref, "filePos", raf.getFilePointer());
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
    return env.getLongField(objref, "filePos");
  }
}
