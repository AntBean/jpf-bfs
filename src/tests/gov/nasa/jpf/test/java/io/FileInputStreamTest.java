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

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class FileInputStreamTest extends TestJPF {
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
  public void testOpenNonExistingFile() throws FileNotFoundException {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      FileInputStream fis = new FileInputStream("IDontExist");
    }
  }

  @Test
  public void testAvailableForNativeFile() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[7]);
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      FileInputStream fis = new FileInputStream("fileSandbox/testFile");
      assertEquals("FIS.available should return length of a file when just created",
                   7, fis.available());
    }
  }

  @Test
  public void testAvailableForCreatedFile() throws Exception {
    if (verifyNoPropertyViolation()) {
      FileInputStream fis = new FileInputStream("fileSandbox/testFile");
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      assertEquals(0, fis.available());

      raf.write(new byte[10]);
      raf.close();
      assertEquals(10, fis.available());
    }
  }

  @Test
  public void testReadFromFile() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      FileInputStream fis = new FileInputStream("fileSandbox/testFile");
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[5];
      assertEquals(5, fis.read(buffer));
      assertReadResult(new byte[] {1,2,3,4,5}, buffer, 5);

      assertEquals(2, fis.read(buffer));
      assertReadResult(new byte[] {6,7}, buffer, 2);

      assertEquals(-1, fis.read(buffer));
    }
  }

  @Test
  public void testSkip() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7} );
      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      FileInputStream fis = new FileInputStream("fileSandbox/testFile");
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[3];
      assertEquals(3, fis.read(buffer));
      assertReadResult(new byte[] {1,2,3}, buffer, 3);

      assertEquals(2, fis.skip(2));

      assertEquals(2, fis.read(buffer));
      assertReadResult(new byte[] {6,7}, buffer, 2);

      assertEquals(-1, fis.read(buffer));
    }
  }

  // <2do> code duplication. Same methods exist in RandomAccessFile.
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
