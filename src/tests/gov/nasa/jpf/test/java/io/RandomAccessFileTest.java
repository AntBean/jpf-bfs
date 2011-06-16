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
package gov.nasa.jpf.test.java.io;

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class RandomAccessFileTest extends TestJPF {
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
  public void testIllegalMode() throws FileNotFoundException {
    if (verifyUnhandledException("java.lang.IllegalArgumentException")) {
      new RandomAccessFile(new File("fileSandbox/testFile"), "234");
    }
  }

  @Test
  public void testIllegalMode2() throws FileNotFoundException {
    if (verifyUnhandledException("java.lang.IllegalArgumentException")) {
      new RandomAccessFile("fileSandbox/testFile", "234");
    }
  }

  @Test
  public void testCreateFileWithConstructor() throws FileNotFoundException {
    if (verifyNoPropertyViolation()) {
      File newFile = new File("fileSandbox/newFile");

      Verify.getBoolean();
      assertFalse(newFile.exists());

      RandomAccessFile raf = new RandomAccessFile(newFile, "rw");
      assertTrue(newFile.exists());
    }
  }

  @Test
  public void testCreateFileWithConstructor2() throws FileNotFoundException {
    if (verifyNoPropertyViolation()) {
      File newFile = new File("fileSandbox/newFile");

      Verify.getBoolean();
      assertFalse(newFile.exists());

      RandomAccessFile raf = new RandomAccessFile("fileSandbox/newFile", "rw");
      assertTrue(newFile.exists());
    }
  }

  @Test
  public void testReadModeWithNotExistingFile() throws FileNotFoundException {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      File newFile = new File("fileSandbox/newFile");

      RandomAccessFile raf = new RandomAccessFile(newFile, "r");
    }
  }

  @Test
  public void testFileInNonExistingDirectory() throws FileNotFoundException {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      File nonExistingFile = new File("fileSandbox/nonExistingDir/file");

      new RandomAccessFile(nonExistingFile, "rw");
    }
  }

  @Test
  public void testFPMoveAfterWrite() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      assertEquals(0L, raf.getFilePointer());

      raf.write(new byte[10]);
      assertEquals(10L, raf.getFilePointer());
    }
  }

  @Test
  public void testReadBeyondFile() throws Exception {
    if (!isJPFRun()) {
      byte[] fileData = {1, 1, 1};
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(fileData);
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.seek(100);
      assertEquals(-1, raf.read(new byte[20]));
    }
  }

  @Test
  public void testReadTrunkedFile() throws Exception {
    if (!isJPFRun()) {
      byte[] data = {1,1,1,1,1};
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(data);
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "r");

      Verify.getBoolean();
      raf.seek(0);
      byte[] expectedBeforeTrunk = {1,1,1,1,1};
      byte[] buffer = new byte[20];
      int read = raf.read(buffer);
      assertEquals(5, read);
      assertReadResult(expectedBeforeTrunk, buffer, read);

      byte[] expectedAfterTrunk = {1,1,1};
      raf.setLength(3);
      read = raf.read(buffer);
      assertEquals(3, read);
      assertReadResult(expectedAfterTrunk, buffer, read);
    }
  }

  // Following tests are almost equal. They test reading from a file after
  // something was written to it by a SUT. Test differ in relative position of
  // read buffer and write chunk with respect to each other

  @Test
  public void testBacktrackableReadWrite() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1,1,1,1,1};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] expectedBeforeWrite = {1, 1, 1, 1, 1};
      byte[] buffer = new byte[10];

      int read = raf.read(buffer);
      assertEquals(5, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      byte[] toWrite = {2, 2, 2};
      raf.seek(1);
      raf.write(toWrite);

      raf.seek(0);
      byte[] expectedAfterWrite = {1, 2, 2, 2, 1};

      read = raf.read(buffer);
      assertEquals(5, read);
      assertReadResult(expectedAfterWrite, buffer, read);

      raf.seek(0);
      expectedAfterWrite = new byte[] {1, 2, 2, 2};

      read = raf.read(buffer, 0, 4);
      assertEquals(4, read);
      assertReadResult(expectedAfterWrite, buffer, read);

      raf.seek(1);
      expectedAfterWrite = new byte[] {2, 2, 2, 1};

      read = raf.read(buffer, 0, 4);
      assertEquals(4, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testBacktrackableReadWrite2() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1,1,1,1,1,1,1};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] expectedBeforeWrite = {1, 1, 1, 1, 1, 1, 1};
      byte[] buffer = new byte[10];

      int read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      byte[] toWrite = {2, 2, 2};
      raf.seek(2);
      raf.write(toWrite);

      raf.seek(0);
      byte[] expectedAfterWrite = {1, 1, 2, 2, 2};

      read = raf.read(buffer, 0, 5);
      assertEquals(5, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testBacktrackableReadWrite3() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1,1,1,1,1,1,1};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] expectedBeforeWrite = {1, 1, 1, 1, 1, 1, 1};
      byte[] buffer = new byte[10];

      int read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      byte[] toWrite = {2, 2, 2};
      raf.seek(2);
      raf.write(toWrite);

      raf.seek(3);
      byte[] expectedAfterWrite = {2, 2, 1, 1};

      read = raf.read(buffer, 0, 4);
      assertEquals(4, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testBacktrackableReadWrite4() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 1, 1, 1, 1, 1, 1, 1, 1};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] expectedBeforeWrite = {1, 1, 1, 1, 1, 1, 1, 1, 1};
      byte[] buffer = new byte[10];

      int read = raf.read(buffer);
      assertEquals(9, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      byte[] toWrite = {2, 2, 2, 2, 2};
      raf.seek(2);
      raf.write(toWrite);

      raf.seek(3);
      byte[] expectedAfterWrite = {2, 2, 2};

      read = raf.read(buffer, 0, 3);
      assertEquals(3, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  @Test
  public void testWriteSingleByte() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 2, 3, 4, 5, 6, 7};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] expectedBeforeWrite = {1, 2, 3, 4, 5, 6, 7};
      byte[] buffer = new byte[10];

      int read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedBeforeWrite, buffer, read);

      byte[] toWrite = {42};
      raf.seek(0);
      raf.write(toWrite);

      raf.seek(0);
      byte[] expectedAfterWrite = {42, 2, 3, 4, 5, 6, 7};

      read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(expectedAfterWrite, buffer, read);
    }
  }

  private void assertReadResult(byte[] expected, byte[] buffer, int bufferLength) {
    String expectedStr = byteArrayToStr(expected, expected.length);
    String bufferStr = byteArrayToStr(buffer, bufferLength);
    String errorMsg = "Expected " + expectedStr + " but read " + bufferStr;

    for (int i = 0; i < bufferLength; i++) {
      assertEquals(errorMsg, expected[i], buffer[i]);
    }
  }

  private String byteArrayToStr(byte[] array, int length) {
    String result = "[";

    for (int i = 0; i < length; i++) {
      result += array[i] + ", ";
    }

    return result + "]";
  }
}
