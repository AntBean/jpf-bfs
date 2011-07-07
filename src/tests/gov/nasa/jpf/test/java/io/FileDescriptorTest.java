//
//Copyright (C) 2011 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.
//
//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.
//
//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.test.java.io;

import java.io.FileDescriptor;
import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class FileDescriptorTest extends TestJPF {
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

  @Test
  public void testUsingOneDescriptor() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
     RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
     FileInputStream fis = new FileInputStream(raf.getFD());
     FileOutputStream fos = new FileOutputStream(raf.getFD());

     Verify.getBoolean();
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
  public void testWriteWhenDescriptorIsClosed() throws Exception {
    if (verifyUnhandledException("java.io.IOException")) {
      FileInputStream fis = new FileInputStream("fileSandbox/testFile");
      FileOutputStream fos = new FileOutputStream(fis.getFD());

      fis.close();
      fos.write(10);
    }
  }

  @Test
  public void testReadWhenDescriptorIsClosed() throws Exception {
    if (verifyUnhandledException("java.io.IOException")) {
      FileInputStream fis = new FileInputStream("fileSandbox/testFile");
      FileOutputStream fos = new FileOutputStream(fis.getFD());

      fos.close();
      fis.read();
    }
  }

  @Test
  public void testReadWhenFileDeleted() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation("+jpf-bfs.opened-delete = warning")) {
      File testFile = new File("fileSandbox/testFile");
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");
      
      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);

      assertTrue(testFile.delete());

      raf.seek(2);
      raf.write(new byte[] {42, 42, 42});

      raf.seek(0);
      read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 42, 42, 42, 6, 7}, buffer, read);
    }
  }

  @Test
  public void testDeleteOpenedFileWhenAwaitError() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyUnhandledException("java.io.IOException",
                           "+jpf-bfs.opened-delete = error")) {
      File testFile = new File("fileSandbox/testFile");
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);

      testFile.delete();
    }
  }

  @Test
  public void testDeleteClosedFileWhenAwaitError() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation("+jpf-bfs.opened-delete = error")) {
      File testFile = new File("fileSandbox/testFile");
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);
      raf.close();

      testFile.delete();
    }
  }

  @Test
  public void testReadWhenFileRenamed() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation("+jpf-bfs.opened-rename = warning")) {
      File testFile = new File("fileSandbox/testFile");
      File newFile = new File("fileSandbox/newFile");
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);

      assertTrue(testFile.renameTo(newFile));

      raf.seek(2);
      raf.write(new byte[] {42, 42, 42});

      raf.seek(0);
      read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 42, 42, 42, 6, 7}, buffer, read);
    }
  }

  @Test
  public void testRenameOpenedFileWhenAwaitError() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyUnhandledException("java.io.IOException",
                           "+jpf-bfs.opened-rename = error")) {
      File testFile = new File("fileSandbox/testFile");
      File newFile = new File("fileSandbox/newFile");
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);

      testFile.renameTo(newFile);
    }
  }

  @Test
  public void testRenameClosedFileWhenAwaitError() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation("+jpf-bfs.opened-rename = error")) {
      File testFile = new File("fileSandbox/testFile");
      File newFile = new File("fileSandbox/newFile");
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);
      raf.close();

      testFile.renameTo(newFile);
    }
  }

  @Test
  public void testIsDescriptorValid() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "r");
      FileDescriptor fd1 = raf.getFD();
      assertTrue("If file wasn't closed descriptor should be valid", fd1.valid());

      raf.close();
      assertFalse("If file was closed descriptor shouldn't be valid", fd1.valid());

      FileDescriptor fd2 = raf.getFD();
      assertFalse("If RandomAccessFile should return not valid descriptor for a closed file",
                  fd2.valid());
    }
  }
  
  @Test
  public void testLastModifiedTimeSetting() throws Exception {
    if (verifyNoPropertyViolation()) {
     File testFile = new File("fileSandbox/testFile");
     
     RandomAccessFile raf = new RandomAccessFile(testFile, "rws");
     long t1 = testFile.lastModified();
     raf.setLength(0);
     
     long t2 = testFile.lastModified();
     Thread.sleep(10000);
     raf.write(new byte[] {1, 2, 3, 4, 5, 6});
     
     long t3 = testFile.lastModified();
     
     assertTrue(t1 < t2);
     assertTrue(t2 < t3);
    }
  }
}
