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
      assertTrue("File.isFile() should return true on a file", file.isFile());
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

  @Test
  public void testBacktrackableMkDir() {
    if (verifyNoPropertyViolation()) {
      File parent1 = new File("fileSandbox/parent1");

      Verify.getBoolean();
      assertFalse("File.exists() should return false if a directory not exists", parent1.exists());
      assertTrue("File.mkdir() should return true when a directory is created", parent1.mkdir());

      assertTrue("File.isDirectory() should return true when called on a directory", parent1.isDirectory());
      assertTrue("File.exist() should return true when called on an existing directory", parent1.exists());
    }
  }

  @Test
  public void testDeleteCreateDirWithCreatedFiles() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent1 = new File("fileSandbox/parent1");
      File child1 = new File("fileSandbox/parent1/child1");

      Verify.getBoolean();
      assertFalse("File.exists() should return false if a directory not exists", parent1.exists());
      assertFalse("File.exists() should return false if a file not exists", child1.exists());

      assertTrue("File.mkdir() should return true when a directory is created", parent1.mkdir());
      assertTrue("File.createNewFile() should return true when a file is created", child1.createNewFile());

      assertTrue("File.exists() should return true if a directory exists", parent1.exists());
      assertTrue("File.exists() should return true if a file exists", child1.exists());

      assertTrue("File.delete() should return true when a directory exists", parent1.delete());

      assertFalse("File.exists() should return false if a directory not exists", parent1.exists());
      assertFalse("File.exists() should return false if a file not exists", child1.exists());
    }
  }

  @Test
  public void testCreateFileInNotExistingDirectory() throws IOException {
    if (verifyNoPropertyViolation()) {
      File child1 = new File("fileSandbox/parent1/child1");

      assertFalse("File.createNewFile() should return false if file's parent doesn't exist", child1.createNewFile());
      assertFalse("File.exists() should return false if file wasn't created", child1.exists());
    }
  }

  @Test
  public void testCreateFileInDeletedDirectory() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      File file = new File("fileSandbox/parent/file");

      Verify.getBoolean();
      assertTrue("File.delete() should return true when a directory exists", parent.delete());

      assertFalse("File.create() should return false if file's parent dir was deleted", file.createNewFile());
      assertFalse("File.exists() should return false if file wasn't created", file.exists());

    }
  }

  @Test
  public void testCreateDirectoryInNotExistingDirectory() throws IOException {
    if (verifyNoPropertyViolation()) {
      File child1 = new File("fileSandbox/parent1/child1");

      assertFalse("File.mkdir() should return false if file's parent doesn't exist", child1.mkdir());
      assertFalse("File.exists() should return false if directory wasn't created", child1.exists());
    }
  }

  @Test
  public void testCreateDirectoryInDeletedDirectory() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      File dir = new File("fileSandbox/parent/dir");

      Verify.getBoolean();
      assertTrue("File.delete() should return true when a directory exists", parent.delete());

      assertFalse("File.create() should return false if dir's parent dir was deleted", dir.mkdir());
      assertFalse("File.exists() should return false if directory wasn't created", dir.exists());

    }
  }
}
