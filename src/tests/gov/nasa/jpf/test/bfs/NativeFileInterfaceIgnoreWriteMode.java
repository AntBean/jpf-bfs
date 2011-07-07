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
public class NativeFileInterfaceIgnoreWriteMode extends TestJPF {
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
  public void testReadWrite() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
      raf.close();
    }

    if (verifyNoPropertyViolation("+jpf-bfs.bfs.ignore-write = *fileSandbox/testFile")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);

      raf.seek(0);
      raf.write(new byte[] {42, 42, 42, 42, 42, 42});

      read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);
    }
  }

  @Test
  public void testReadWhenAwaitError() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.write(new byte[] {1, 2, 3, 4, 5, 6, 7});
      raf.close();
    }

    if (verifyUnhandledException("java.io.IOException",
            "+jpf-bfs.bfs.ignore-write = */testFile",
            "+jpf-bfs.ignore-write-file-read = error")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      Verify.getBoolean();
      byte[] buffer = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);

      raf.seek(0);
      raf.write(new byte[] {42, 42, 42, 42, 42, 42});
      
      raf.seek(0);
      read = raf.read(buffer);
    }
  }

}
