/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nasa.jpf.test.bfs;

import gov.nasa.jpf.util.ClassSpec;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class NativeFileInterfaceRaceTests extends TestJPF {
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

  static final String EXCLUDE_SANDBOX = "+jpf-bfs.bfs.exclude = *fileSandbox/*";

  static final ClassSpec RACE_DETECTION_PROPERTY = new ClassSpec("gov.nasa.jpf.listener.PreciseRaceDetector");
  static final String RACE_DETECTION_LISTENER = "+listener=gov.nasa.jpf.listener.PreciseRaceDetector";

  @Ignore
  // <2do> this work if run with help of bin/jpf
  public void testRaceDetectionWithShardDescriptorRead() throws Exception {
    if (verifyPropertyViolation(RACE_DETECTION_PROPERTY, RACE_DETECTION_LISTENER, EXCLUDE_SANDBOX)) {
     final String fileName = "fileSandbox/testFile";
     final FileInputStream fis1 = new FileInputStream(fileName);
     final FileInputStream fis2 = new FileInputStream(fis1.getFD());

      Runnable r1 = new Runnable() {
        public void run() {
          try {
            fis1.read();
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };


      Runnable r2 = new Runnable() {
        public void run() {
          try {
            fis2.read();
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testRaceDetectionWithShardDescriptorReadWrite() throws Exception {
    if (verifyPropertyViolation(RACE_DETECTION_PROPERTY, RACE_DETECTION_LISTENER, EXCLUDE_SANDBOX)) {
     final String fileName = "fileSandbox/testFile";
     final FileInputStream fis = new FileInputStream(fileName);
     final FileOutputStream fos = new FileOutputStream(fis.getFD());

      Runnable r1 = new Runnable() {
        public void run() {
          try {
            fis.read();
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };


      Runnable r2 = new Runnable() {
        public void run() {
          try {
            fos.write(1);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testRaceDetectionWithShardDescriptorWriteSkip() throws Exception {
    if (verifyPropertyViolation(RACE_DETECTION_PROPERTY, RACE_DETECTION_LISTENER, EXCLUDE_SANDBOX)) {
     final String fileName = "fileSandbox/testFile";
     final FileInputStream fis = new FileInputStream(fileName);
     final FileOutputStream fos = new FileOutputStream(fis.getFD());

      Runnable r1 = new Runnable() {
        public void run() {
          try {
            fis.skip(3);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };


      Runnable r2 = new Runnable() {
        public void run() {
          try {
            fos.write(1);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testRaceDetectionWithShardDescriptorWriteSeek() throws Exception {
    if (verifyPropertyViolation(RACE_DETECTION_PROPERTY, RACE_DETECTION_LISTENER, EXCLUDE_SANDBOX)) {
     final String fileName = "fileSandbox/testFile";
     final RandomAccessFile raf = new RandomAccessFile(fileName, "rws");
     final FileOutputStream fos = new FileOutputStream(raf.getFD());

      Runnable r1 = new Runnable() {
        public void run() {
          try {
            raf.seek(3);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };


      Runnable r2 = new Runnable() {
        public void run() {
          try {
            fos.write(1);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testNoRaceWithShardDescriptorWriteGetFP() throws Exception {
    if (verifyNoPropertyViolation(EXCLUDE_SANDBOX)) {
     final String fileName = "fileSandbox/testFile";
     final RandomAccessFile raf = new RandomAccessFile(fileName, "rws");
     final FileOutputStream fos = new FileOutputStream(raf.getFD());

      Runnable r1 = new Runnable() {
        public void run() {
          try {
            // NO RACE HERE!!
            raf.getFilePointer();
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };

      Runnable r2 = new Runnable() {
        public void run() {
          try {
            fos.write(1);
          } catch (Exception ex) {
            throw new RuntimeException(ex);
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

}
