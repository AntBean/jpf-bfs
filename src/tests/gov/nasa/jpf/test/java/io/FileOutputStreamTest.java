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
package gov.nasa.jpf.test.java.io;

import java.io.FileInputStream;
import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class FileOutputStreamTest extends TestJPF {
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
  public void testOpenNonExistingFile() throws Exception {
    if (verifyNoPropertyViolation()) {
      FileOutputStream fos = null;
      File nonExisting = new File("fileSandbox/nonExistingFile");

      Verify.getBoolean();
      assertFalse(nonExisting.exists());

      fos = new FileOutputStream("fileSandbox/nonExistingFile");
      fos.close();

      assertTrue(nonExisting.exists());
    }
  }

  @Test
  public void testOpenFileInNonExistingDirectory() throws FileNotFoundException {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      FileOutputStream fos = new FileOutputStream("fileSandbox/nonExistingDir/someFile");
    }
  }

  @Test
  public void testWriteWToClosedStream() throws IOException {
    if (verifyUnhandledException("java.io.IOException")) {
      FileOutputStream fos = new FileOutputStream("fileSandbox/testFile");
      fos.close();
      fos.write(123);
    }
  }

  @Test
  public void testWriteByte() throws IOException {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
      raf.close();
    }

    if (verifyNoPropertyViolation()) {      
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read;

      byte[] expectedBeforeWrite = {1, 2, 3, 4, 5, 6, 7};
      read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      FileOutputStream fos = new FileOutputStream("fileSandbox/testFile");
      fos.write(42);
      fos.close();
      byte[] expectedAfterWrite = {42};

      raf.seek(0);
      read = raf.read(buffer);
      assertEquals(1, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testAppendByte() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read;

      byte[] expectedBeforeWrite = {1, 2, 3, 4, 5, 6, 7};
      read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      FileOutputStream fos = new FileOutputStream("fileSandbox/testFile", true);
      fos.write(42);
      fos.close();
      byte[] expectedAfterWrite = {1, 2, 3, 4, 5, 6, 7, 42};

      raf.seek(0);
      read = raf.read(buffer);
      assertEquals(8, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testWriteSeveralBytes() throws IOException {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read;

      byte[] expectedBeforeWrite = {1, 2, 3, 4, 5, 6, 7};
      read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      FileOutputStream fos = new FileOutputStream("fileSandbox/testFile");
      fos.write(new byte[] {42, 41, 40});
      fos.close();
      byte[] expectedAfterWrite = {42, 41, 40};

      raf.seek(0);
      read = raf.read(buffer);
      assertEquals(3, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testAppendSeveralBytes() throws IOException {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[15];
      int read;

      byte[] expectedBeforeWrite = {1, 2, 3, 4, 5, 6, 7};
      read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      FileOutputStream fos = new FileOutputStream("fileSandbox/testFile", true);
      fos.write(new byte[] {42, 41, 40});
      fos.close();
      byte[] expectedAfterWrite = {1, 2, 3, 4, 5, 6, 7, 42, 41, 40};

      raf.seek(0);
      read = raf.read(buffer);
      assertEquals(10, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }
  
  @Test
  public void testOpenFileWithoutPermissionToWrite() throws Exception {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      File testFile = new File("fileSandbox/testFile");
      testFile.setWritable(false);
      
      FileOutputStream fis = new FileOutputStream(testFile);
    }
  }
  
  @Test
  public void testWriteToFileWithNoPermissionsToWrite() throws Exception {
    if (verifyUnhandledException("java.io.IOException")) {
     File f = new File("fileSandbox/testFile");
     f.setWritable(false);     
     
     FileInputStream fis = new FileInputStream(f);
     FileOutputStream fos = new FileOutputStream(fis.getFD());      
     
     fos.write(new byte[] {1, 2, 3});
    }
  }
}
