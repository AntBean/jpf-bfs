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
package gov.nasa.jpf.test.bfs;

import gov.nasa.jpf.util.ClassSpec;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.RandomAccessFile;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static gov.nasa.jpf.test.java.io.BFSTestUtils.*;

/**
 *
 * @author Ivan Mushketik
 */
public class NativeFileInterfaceTest extends TestJPF {
  @BeforeClass
  public static void setUpFileSandbox() throws Exception {
    File fileSandbox = new File("fileSandbox");
    if (fileSandbox.exists()) {
      FileUtils.removeRecursively(fileSandbox);
    }

    // Creating sandbox for java.io.RandomAccessFile testing
    File subdirs = new File("fileSandbox");
    if (!subdirs.mkdir()) {
      throw new RuntimeException("Unable to create sandbox directory");
    }

    File testFile = new File("fileSandbox/testFile");

    if (!testFile.createNewFile()) {
      throw new RuntimeException("Unable to create file for java.io.RandomAccessFile testing");
    }
  }

  @AfterClass
  public static void removeFileSandbox() {
    File fileSandbox = new File("fileSandbox");
    if (fileSandbox.exists()) {
      FileUtils.removeRecursively(fileSandbox);
    }
  }

  @Before
  public void clearTestFileContent() throws Exception {
    File testFile = new File("fileSandbox/testFile");
    RandomAccessFile raf = new RandomAccessFile(testFile, "rws");
    raf.setLength(0);

    raf.close();
  }

  static final String EXCLUDE_SANDBOX = "+jpf-bfs.bfs.exclude = *fileSandbox/testFile";

  @Test
  public void testNotBacktrackableRead() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5});
      raf.close();
    }

    if (verifyNoPropertyViolation(EXCLUDE_SANDBOX)) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      // Starts from false
      boolean b = Verify.getBoolean(true);
      byte[] buffer = new byte[7];
      int read;
      read = raf.read(buffer);
      assertEquals(5, read);

      // First execution
      if (!b) {
         assertReadResult(new byte[] {1, 2, 3, 4, 5}, buffer, read);

      } else {
         // Second execution
         assertReadResult(new byte[] {1, 2, 3, 42, 42}, buffer, read);
      }

      raf.seek(3);
      raf.write(new byte[] {42, 42});
    }
  }

  @Test
  public void testSharedDescriptorUsage() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation(EXCLUDE_SANDBOX)) {
     RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
     FileInputStream fis = new FileInputStream(raf.getFD());
     FileOutputStream fos = new FileOutputStream(raf.getFD());

     byte buffer[] = new byte[20];
     int read = fis.read(buffer);
     assertEquals(7, read);
     assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);
     assertEquals(7, raf.getFilePointer());

     fos.write(new byte[] {42, 41, 40});
     assertEquals(10, raf.getFilePointer());
     raf.seek(0);
     read = fis.read(buffer);
     assertEquals(10, read);
     assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7, 42, 41, 40}, buffer, read);
    }
  }

  @Test
  public void testReadWhenDescriptorWasClosed() throws Exception {
    if (verifyUnhandledException("java.io.IOException", EXCLUDE_SANDBOX)) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "r");

      FileInputStream fis = new FileInputStream(raf.getFD());
      raf.close();

      fis.read();
    }
  }

  @Test
  public void testWriteWhenDescriptorWasClosed() throws Exception {
    if (verifyUnhandledException("java.io.IOException", EXCLUDE_SANDBOX)) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "r");

      FileOutputStream fis = new FileOutputStream(raf.getFD());
      raf.close();

      fis.write(42);
    }
  }

  @Test
  public void testReadWriteFromCreatedFile() throws Exception {
    if (verifyNoPropertyViolation("+jpf-bfs.bfs.exclude = *fileSandbox/*")) {
      File newFile = new File("fileSandbox/newFile");      
      newFile.createNewFile();
      RandomAccessFile raf = new RandomAccessFile(newFile, "rws");

      raf.write(new byte[] {1, 2, 3, 4, 5});
      raf.seek(0);

      // Starts from false
      boolean b = Verify.getBoolean(true);
      byte[] buffer = new byte[7];
      int read;
      read = raf.read(buffer);
      assertEquals(5, read);

      // First execution
      if (!b) {
         assertReadResult(new byte[] {1, 2, 3, 4, 5}, buffer, read);

      } else {
         // Second execution
         assertReadResult(new byte[] {1, 2, 3, 42, 42}, buffer, read);
      }

      raf.seek(3);
      raf.write(new byte[] {42, 42});
    }
  }

//  static final ClassSpec RACE_DETECTION_PROPERTY = new ClassSpec("gov.nasa.jpf.listener.PreciseRaceDetector");
//  static final String RACE_DETECTION_LISTENER = "+listener=gov.nasa.jpf.listener.PreciseRaceDetector";
//
//  @Test
//  public void testRaceDetectionWithShardDescriptorRead() throws Exception {
//    if (verifyPropertyViolation(RACE_DETECTION_PROPERTY, RACE_DETECTION_LISTENER)) {
//     final String fileName = "fileSandbox/testFile";
//     final FileInputStream fis1 = new FileInputStream(fileName);
//     final FileInputStream fis2 = new FileInputStream(fis1.getFD());
//
//      Runnable r1 = new Runnable() {
//        public void run() {
//          try {
//            fis1.read();
//          } catch (Exception ex) {
//            throw new RuntimeException(ex);
//          }
//        }
//      };
//
//
//      Runnable r2 = new Runnable() {
//        public void run() {
//          try {
//            fis2.read();
//          } catch (Exception ex) {
//            throw new RuntimeException(ex);
//          }
//        }
//      };
//
//      Thread t1 = new Thread(r1);
//      Thread t2 = new Thread(r2);
//
//      t1.start();
//      t2.start();
//    }
//  }
//
//  @Test
//  public void testRaceDetectionWithShardDescriptorReadWrite() throws Exception {
//    if (verifyPropertyViolation(RACE_DETECTION_PROPERTY, RACE_DETECTION_LISTENER)) {
//     final String fileName = "fileSandbox/testFile";
//     final FileInputStream fis = new FileInputStream(fileName);
//     final FileOutputStream fos = new FileOutputStream(fis.getFD());
//
//      Runnable r1 = new Runnable() {
//        public void run() {
//          try {
//            fis.read();
//          } catch (Exception ex) {
//            throw new RuntimeException(ex);
//          }
//        }
//      };
//
//
//      Runnable r2 = new Runnable() {
//        public void run() {
//          try {
//            fos.write(1);
//          } catch (Exception ex) {
//            throw new RuntimeException(ex);
//          }
//        }
//      };
//
//      Thread t1 = new Thread(r1);
//      Thread t2 = new Thread(r2);
//
//      t1.start();
//      t2.start();
//    }
//  }
}
