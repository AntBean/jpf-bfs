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

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class FileTest extends TestJPF {

  public FileTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    // Creating sandbox for java.io.File testing
    File subdirs = new File("fileSandbox/parent/child");
    if (!subdirs.mkdirs())
      throw new RuntimeException("Unable to create sandbox directories");
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    if (!FileUtils.removeRecursively(new File("fileSandbox")))
      throw new RuntimeException("Unable to remove sandbox directories");
  }

  @Test
  public void testBacktrackableRemove() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/child");

      Verify.getBoolean();
      assertTrue("File file/Sandbox/parent/child should exists before deletion", file.exists());
      assertTrue("File.delete() on existing file should return true", file.delete());
      assertFalse( "After a deletion of file File.exists() should return false", file.exists());
    }
  }

  @Test
  public void testBacktrackableFileCreation() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/child/file");

      Verify.getBoolean();
      assertFalse("File.exists() should return false if file doesn't exist", file.exists());
      assertTrue("File.create() should return true", file.createNewFile());
      assertTrue("File.exists() should return true on a created file", file.exists());
    }
  }

  @Test
  public void testRemoveParentDir() {
    if (verifyNoPropertyViolation()) {

      File parent = new File("fileSandbox/parent");
      File child = new File("fileSandbox/parent/child");

      assertTrue("File.exists() should return true for existing files", parent.exists());
      assertTrue("File.exists() should return true for existing files", child.exists());

      assertTrue("File.delete() on existing file should return true", parent.delete());
      assertFalse("File.exists() should return false for deleted files", parent.exists());
      assertFalse("File.exists() should return false for deleted files", child.exists());
    }
  }
}
