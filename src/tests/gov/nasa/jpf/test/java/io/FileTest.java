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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
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
    FileUtils.removeRecursively(new File("fileSandbox"));

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
  public void testParentChildCtor() {
    if (verifyNoPropertyViolation()) {
      File parentChild = new File("fileSandbox/parent", "child");
      
      Verify.getBoolean();
      assertTrue(parentChild.exists());
      assertTrue(parentChild.delete());
      assertFalse(parentChild.exists());
    }
  }
  
  @Test
  public void testParentSeparatorChildCtor() {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      File parentChild = new File(parent, "child");

      Verify.getBoolean();
      assertTrue(parentChild.exists());
      assertTrue(parentChild.delete());
      assertFalse(parentChild.exists());
    }
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
  public void testIsFileExistsInDeletedAndCreatedDirectory() {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      assertFalse("Directory can't be deleted if it is not empty", parent.delete());
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
  public void testBacktrackableMkdirs() {
    if (verifyNoPropertyViolation()) {
      File newDir = new File("fileSandbox/parent/child1/child2/child3");
      File child1 = new File("fileSandbox/parent/child1");
      File child2 = new File("fileSandbox/parent/child1/child2");

      Verify.getBoolean();
      assertTrue("None of new directories should exists in this state",
                 !newDir.exists() && !child1.exists() && !child2.exists());

      newDir.mkdirs();

      assertTrue("All new directories should be created by File.mkdirs()",
                 newDir.exists() && child1.exists() && child2.exists());
    }
  }

  @Test
  public void testMkdirsOfExistingDirectory() {
    if (verifyNoPropertyViolation()) {
      File sandbox = new File("fileSandbox");

      assertFalse(sandbox.mkdirs());
    }
  }

  @Test
  public void testMkdirsWhenParentWasDeleted() {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");

      parent.delete();

      File child = new File("fileSandbox/parent/child");
      File child1 = new File("fileSandbox/parent/child/child1");
      File child2 = new File("fileSandbox/parent/child/child1/child2");

      child2.mkdirs();

      assertTrue("All child directories should be created by File.mkdirs()",
                 parent.exists() && child.exists() && child1.exists() && child2.exists());
    }
  }

  @Test
  public void testMkdirsWhenParentIsAFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      File dir = new File("fileSandbox/file/dir1/dir2");
      assertFalse("File.mkdirs() should return false if one of new dirs' parents is a file",
                  dir.mkdirs());

      assertTrue("", file.exists());
      assertFalse("", new File("fileSandbox/file/dir1").exists());
      assertFalse("", new File("fileSandbox/file/dir1/dir2").exists());
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
      File child = new File("fileSandbox/parent/child");
      File file = new File("fileSandbox/parent/child/file");

      Verify.getBoolean();
      assertTrue("File.delete() should return true when a directory exists", child.delete());

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
      File child = new File("fileSandbox/parent/child");
      File dir = new File("fileSandbox/parent/child/dir");

      Verify.getBoolean();
      assertTrue("File.delete() should return true when a directory exists", child.delete());

      assertFalse("File.create() should return false if dir's parent dir was deleted", dir.mkdir());
      assertFalse("File.exists() should return false if directory wasn't created", dir.exists());
    }
  }

  @Test
  public void testFSRootCantBeDeleted() {
    if (verifyNoPropertyViolation()) {
      File[] roots = File.listRoots();

      for (File root : roots) {
        assertFalse("File.delete() on FS root should return false", root.delete());
        assertTrue("FS root can't be deleted", root.exists());
      }
    }
  }

  @Test
  public void testBacktrackableListChild() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");

      Verify.getBoolean();
      String[] beforeFileCreation = {"child"};
      assertSameStrings(beforeFileCreation, parent.list());

      new File("fileSandbox/parent/file1").createNewFile();
      new File("fileSandbox/parent/file2").createNewFile();

      String[] afterFileCreation = {"file1", "file2", "child"};
      assertSameStrings(afterFileCreation, parent.list());
    }
  }

  @Test
  public void testListChildsWhenOneOfThemWasDeleted() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      File child = new File("fileSandbox/parent/child");
      File child1 = new File("fileSandbox/parent/child1");

      child1.createNewFile();
      child.delete();

      String[] expectedChilds = {"child1"};
      assertSameStrings(expectedChilds, parent.list());
    }
  }

  @Test
  public void testListChildrenOfNotExistingDirectory() {
    if (verifyNoPropertyViolation()) {
      File notExists = new File("IDontExist");

      assertNull("File.list() should return null for a directory that doesn't exist",
                 notExists.list());

    }
  }

  @Test
  public void testListChildrenWithOfFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      String[] children = file.list();
      assertNull("File.listFiles(FilenameFilter) should return null when called for a file",
                 children);
    }
  }

  /**
   * Filename filter that accepts only files with name that starts with 'f'.
   */
  static class FF implements FilenameFilter {
    public boolean accept(File file, String filename) {
      if (filename.charAt(0) == 'f') {
        return true;
      }

      return false;
    }
  }

  @Test
  public void testListChildrenWithFilter() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox");

      new File("fileSandbox/file1").createNewFile();
      new File("fileSandbox/file2").createNewFile();
      new File("fileSandbox/otherFile").createNewFile();

      String[] expectedChilds = {"file1", "file2"};

      assertSameStrings(expectedChilds, parent.list(new FF()));
    }
  }

  @Test
  public void testListChildrenWithFilterOfFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      String[] children = file.list(new FF());
      assertNull("File.listFiles(FilenameFilter) should return null when called for a file",
                 children);
    }
  }

  /**
   * File.listFiles() returns File objects whose File.getPath() returns
   * canonical path of a file.
   * @throws IOException
   */
  @Test
  public void testListChildrenFiles() throws IOException {
    if (verifyNoPropertyViolation()) {
      File curDir = new File(".");
      String currentDir = curDir.getCanonicalPath();

      Verify.getBoolean();
      File dir = new File("fileSandbox");
      String[] beforeCreation = {currentDir + "/fileSandbox/parent"};

      String[] result = getPathFromFilesArray(dir.listFiles());
      assertSameStrings(beforeCreation, result);

      new File("fileSandbox/file1").createNewFile();
      new File("fileSandbox/file2").createNewFile();

      String[] afterCreation = {
        currentDir + "/fileSandbox/parent",
        currentDir + "/fileSandbox/file1",
        currentDir + "/fileSandbox/file2",
      };

      result = getPathFromFilesArray(dir.listFiles());
      assertSameStrings(afterCreation, result);
    }
  }

  @Test
  public void testListChildrenFilesOfFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      File[] children = file.listFiles();
      assertNull("File.listFiles(FilenameFilter) should return null when called for a file",
                 children);
    }
  }

  @Test
  public void testListChildrenFilesWithFilenameFilter() throws IOException {
    if (verifyNoPropertyViolation()) {
      File curDir = new File(".");
      String currentDir = curDir.getCanonicalPath();

      new File("fileSandbox/file1").createNewFile();
      new File("fileSandbox/file2").createNewFile();
      new File("fileSandbox/otherFile").createNewFile();

      String[] expected = {
        currentDir + "/fileSandbox/file1",
        currentDir + "/fileSandbox/file2",
      };

      File sandbox = new File("fileSandbox");
      String[] result = getPathFromFilesArray(sandbox.listFiles(new FF()));

      assertSameStrings(expected, result);
    }
  }

  @Test
  public void testListChildrenFilesWithFilenameFilterOfFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      File[] children = file.listFiles(new FF());
      assertNull("File.listFiles(FilenameFilter) should return null when called for a file",
                 children);
    }
  }

  /**
   * FileFilter that accepts file with a name that ends with 'a'
   */
  private static class FileF implements FileFilter {

    public boolean accept(File file) {
      String fileName = file.getPath();
      return fileName.charAt(fileName.length() - 1) == 'a';
    }
  }

  @Test
  public void testListChildrenFilesWithFileFilter() throws IOException {
    if (verifyNoPropertyViolation()) {
      File curDir = new File(".");
      String currentDir = curDir.getCanonicalPath();

      new File("fileSandbox/file1a").createNewFile();
      new File("fileSandbox/file2a").createNewFile();
      new File("fileSandbox/otherFile").createNewFile();

       String[] expected = {
        currentDir + "/fileSandbox/file1a",
        currentDir + "/fileSandbox/file2a",
      };

      File sandbox = new File("fileSandbox");
      String[] result = getPathFromFilesArray(sandbox.listFiles(new FileF()));

      assertSameStrings(expected, result);
    }
  }

  @Test
  public void testListChildrenFilesWithFileFilterOfFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      File[] children = file.listFiles(new FileF());
      assertNull("File.listFiles(FilenameFilter) should return null when called for a file",
              children);
    }
  }

  @Test
  public void testGetParent() throws IOException {
    if (verifyNoPropertyViolation()) {
      File curDir = new File(".");
      File notExists = new File("IDontExist/MeToo");

      assertEquals("File.getParent() should return filename of the file without last path element",
                   notExists.getParent(), "IDontExist");

      File parentFile = notExists.getParentFile();
      assertEquals("File.getParentFile() should return file with filename that equals to File.getParent()",
                   parentFile.getPath(), "IDontExist");
      assertEquals("File.getParentFile() should return file with complete cannonical path from FS root",
                   curDir.getCanonicalPath() + "/IDontExist", parentFile.getCanonicalPath());

      File notExists2 = new File("ISimplyDontExist");

      assertNull("If a filename consist of one path element, File.getParent() should return null",
                 notExists2.getParent());
      assertNull("If a filename consist of one path element, File.getParentFile() should return null",
                 notExists2.getParentFile());
    }
  }

  @Test
  public void testGetCannonicalPath() throws IOException {
    if (verifyNoPropertyViolation()) {
      File sandbox = new File("fileSandbox");
      File curDir = new File(".");

      String expectedPath = curDir.getCanonicalPath() + "/fileSandbox";
      assertEquals(expectedPath, sandbox.getCanonicalPath());
    }
  }

  @Test
  public void testGetCannonicalFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File sandbox = new File("fileSandbox");
      File curDir = new File(".");

      String expectedPath = curDir.getCanonicalPath() + "/fileSandbox";
      assertEquals(sandbox.getCanonicalFile().getPath(), expectedPath);
    }
  }

  @Test
  public void testGetName() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox");
      assertEquals("fileSandbox", file.getName());

      File dir = new File("fileSandbox/parent");
      assertEquals("parent", dir.getName());
    }
  }

  @Test
  public void testGetLengthOfNotExistingFile() {
    if (verifyNoPropertyViolation()) {
      File dontExist = new File("IDontExist");

      assertEquals("File.length() should return 0 for files/dirs that don't exist", 0, dontExist.length());
    }
  }

  @Test
  public void testCreateTempFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File sandbox = new File("fileSandbox");

      File temp = File.createTempFile("prefix", "suffix", sandbox);
      assertEquals("Cannonical path of temp dir's parent should be equal to cannonical path of a dir that was given to File.createTempFile()",
                   sandbox.getCanonicalPath(), temp.getParentFile().getCanonicalPath());
      String fileName = temp.getName();
      
      assertTrue("File.createTempFile() should create files, but not directories", temp.isFile());
      assertTrue("Name of temp file should start with a prefix that was given to File.createTempFile()",
                 fileName.startsWith("prefix"));
      assertTrue("Name of temp file should end with a suffix that was given to File.createTempFile()", fileName.endsWith("suffix"));
    }
  }

  @Test
  public void testCreateTempFileInNotExistingDir() throws IOException {
    if (verifyUnhandledException("java.io.IOException")) {
      File dir = new File("someImaginaryDir");

      // Attempt to create tempFile in a not existing directory
      // should throw java.io.Exception
      File.createTempFile("prefix", "suffix", dir);
    }
  }

  @Test
  public void testCreateTempFileInDeletedDirectory() throws IOException {
    if (verifyUnhandledException("java.io.IOException")) {
      File dir = new File("fileSandbox/parent/child");
      dir.delete();

      // Attempt to create tempFile in a deleted directory
      // should throw java.io.Exception
      File.createTempFile("prefix", "suffix", dir);
    }
  }

  @Test
  public void testCreateTempFileInAFile() throws IOException {
    if (verifyUnhandledException("java.io.IOException")) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      // Attempt to create tempFile in a deleted directory
      // should throw java.io.Exception
      File.createTempFile("prefix", "suffix", file);
    }
  }

  @Test
  public void testRenameFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      File dest = new File("fileSandbox/file1");

      Verify.getBoolean();
      assertTrue("New file should exist in this state", file.exists());
      assertFalse("Destanation file shouldn't exist in this state", dest.exists());

      assertTrue("File.renameTo() should return true if new file can be create", 
                 file.renameTo(dest));

      assertFalse("After file is renamed it shouldn't exist", file.exists());
      assertTrue("Destanation file should exist after rename operation",
                 dest.exists());
    }
  }

  @Test
  public void testRenameFileThatNotExist() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/IDontExist");
      File dest = new File("fileSandbox/dest");

      assertFalse("File.renameTo() should return false if source file doesn't exist",
                  file.renameTo(dest));
      assertFalse("Destanation file shouldn't exist after rename operation if source file doesn't exist",
                  dest.exists());
    }
  }

  @Test
  public void testRenameDeletedFile() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/file");
      file.createNewFile();

      file.delete();

      File dest = new File("fileSandbox/dest");
      assertFalse("Deleted file can't be removed, so File.renameTo() should return false",
                  file.renameTo(dest));
    }
  }

  @Test
  public void testRenameDirectory() throws IOException {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      File child = new File("fileSandbox/parent/child");
      File file1 = new File("fileSandbox/parent/child/file1");
      File file2 = new File("fileSandbox/parent/child/file2");
      File file3 = new File("fileSandbox/parent/file3");

      file1.createNewFile();
      file2.createNewFile();
      file3.createNewFile();

      File dest = new File("fileSandbox/dest");
      File destChild = new File("fileSandbox/dest/child");
      File destFile1 = new File("fileSandbox/dest/child/file1");
      File destFile2 = new File("fileSandbox/dest/child/file2");
      File destFile3 = new File("fileSandbox/dest/file3");

      Verify.getBoolean();
      assertTrue("Files to rename should exist befor renaming",
                 parent.exists() && child.exists() && file1.exists() && file2.exists() && file3.exists());
      assertFalse("Destanation files shouldn't exist before renaming",
                  dest.exists() || destChild.exists() || destFile1.exists() ||
                  destFile2.exists() || destFile3.exists());

      assertTrue(parent.renameTo(dest));

      assertFalse("Original files shouldn't exist after renaming",
                  parent.exists() || child.exists() || file1.exists() || file2.exists() || file3.exists());
      assertTrue("Destanation files should exist after renaming",
                 dest.exists() && destChild.exists() && destFile1.exists() &&
                 destFile2.exists() && destFile3.exists());
    }
  }

  @Test
  /**
   * FileInfo.list() first reads childs of a directory from a native FS. This test
   * checks that it doesn't do it when some dir was renamed.
   */
  public void testGetDirectoriesChildsAfterRenaming() throws IOException {
    if (verifyNoPropertyViolation()) {
      File newDir = new File("fileSandbox/newDir");
      File child1 = new File("fileSandbox/newDir/child1");
      File child2 = new File("fileSandbox/newDir/child2");

      newDir.mkdir();
      child1.createNewFile();
      child2.createNewFile();

      File parent = new File("fileSandbox/parent");

      newDir.renameTo(parent);

      String[] expectedChilds = {"child1", "child2"};
      assertSameStrings(expectedChilds, parent.list());
    }
  }  
  
  @Test
  public void testBacktrackableReadableFlagSetting() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/child");

      Verify.getBoolean();
      assertTrue("File.canRead() should return true if file can be read", file.canRead());
      assertTrue("File.setReadable() should return true when called for existing file", file.setReadable(false));

      assertFalse("File.canRead() should return false if file's read flag set to false", file.canRead());
    }
  }

  @Test
  public void testChangeReadableForNonExistingFile() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/DoesntExist");

      assertFalse("File.setReadable() should return false when called for a file that doesn't exist",
              file.setReadable(true));
    }
  }
  
  @Test
  public void testBacktrackableWriteableFlagSetting() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/child");

      Verify.getBoolean();
      assertTrue("File.canWrite() should return true if file can be written", file.canWrite());
      assertTrue("File.setWritable() should return true when called for existing file", file.setWritable(false));

      assertFalse("File.canWrite() should return false if file's write flag set to false", file.canWrite());
    }
  }

  @Test
  public void testChangeWritableForNonExistingFile() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/DoesntExist");

      assertFalse("File.setWritable() should return false when called for a file that doesn't exist",
              file.setWritable(true));
    }
  }
    
  @Test
  public void testBacktrackableExecutableFlagSetting() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/child");
      file.setExecutable(true);
      
      Verify.getBoolean();
      assertTrue("File.canExecute() should return true if file can be executed", file.canExecute());
      assertTrue("File.setExecutable() should return true when called for existing file", file.setExecutable(false));

      assertFalse("File.canExecute() should return false if file's execute flag set to false", file.canExecute());
    }
  }

  @Test
  public void testChangeExecutableForNonExistingFile() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent/DoesntExist");

      assertFalse("File.setExecutable() should return false when called for a file that doesn't exist",
              file.setExecutable(true));
    }
  }
  
  @Test
  public void testListDirWhenHasNoRights() {
    if (verifyNoPropertyViolation()) {
      File dir = new File("fileSandbox/parent");
      dir.setExecutable(false);
      dir.setReadable(false);
      
      assertNull("If SUT has not read and execute rights for a directory File.list() should return null",
                 dir.list());
    }
  }
  
  @Test
  public void testCreateFileInDirWithNoWriteRights() throws Exception {
    if (verifyUnhandledException("java.io.IOException")) {
      File dir = new File("fileSandbox/parent");
      dir.setWritable(false);
      File newFile = new File("fileSandbox/parent/newFile");
      
      newFile.createNewFile();
    }
  }
  
  @Test
  public void testRemoveFileIfNoWritePermitionToParentDir() throws Exception {
    if (verifyNoPropertyViolation()) {
      File newFile = new File("fileSandbox/parent/newFile");
      newFile.createNewFile();
      
      File parent = new File("fileSandbox/parent");
      parent.setWritable(false);
      
      assertFalse("If SUT has no write permisions for a file's parent directory, File.delete() should return false", 
              newFile.delete());
      assertTrue("If file wasn't deleted it should exist", newFile.exists());
    }
  }
  
  @Test
  public void testCreateDirectoryInADirWithNoWritePermissions() throws Exception {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      parent.setWritable(false);
      
      File childDir = new File("fileSandbox/parent/childDir");
      assertFalse("If SUT has no write permissions for a parent dir, child dir can't be created", 
                  childDir.mkdir());
      assertFalse("If child directory wasn't created it shouldn't exist", childDir.exists());
      
    }
  }
  
  @Test
  public void testCreateDirectoriesInADirWithNoWritePermissions() throws Exception {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      parent.setWritable(false);
      
      File childDir = new File("fileSandbox/parent/childDir");
      File childChildDir = new File("fileSandbox/parent/childDir/childChildDir");
      
      assertFalse("If SUT has no write permissions for a parent dir, child dir can't be created", 
                  childChildDir.mkdirs());
      assertFalse("If child directory wasn't created it shouldn't exist", childDir.exists());
      assertFalse("If child directory wasn't created it shouldn't exist", childChildDir.exists());
      
    }
  }
  
  @Test
  public void testRenameFileWithNoWritePermissionsForAParentDirectory() throws Exception {
    if (verifyNoPropertyViolation()) {
      File parent = new File("fileSandbox/parent");
      parent.setWritable(false);
      
      File child = new File("fileSandbox/parent/child");
      File newChild = new File("fileSandbox/parent/newChild");
      
      assertFalse("File can't be renamed in a directory with no write permissions", 
                  child.renameTo(newChild));
      assertFalse("If renaming failed, no new file should exist", newChild.exists());
    }
  }
  
  @Test
  public void testRenameDirectoryToADirectoryWithNoWritePermissions() throws Exception {
    if (verifyNoPropertyViolation()) {
      File child = new File("fileSandbox/parent/child");
      File newDir = new File("fileSandbox/parent/newChild");
      child.setWritable(false);
      
      newDir.createNewFile();
      
      assertFalse("If SUT has no write rights for destanation directory renaming can't be performed", 
                  newDir.renameTo(child));
      assertTrue(newDir.exists());
      assertTrue(child.exists());
     
    }
  }
  
  private String[] getPathFromFilesArray(File[] listFiles) {
    String[] result = new String[listFiles.length];

    for (int i = 0; i < result.length; i++) {
      result[i] = listFiles[i].getPath();
    }

    return result;
  }

  /**
   * Assert that two arrays contains same string. Order of elements in each
   * array doesn't matter.
   * @param expected - array of expected values
   * @param result - array of values to check
   */
  private void assertSameStrings(String[] expected, String[] result) {

    assertEquals("Arrays should have same length", expected.length, expected.length);

    HashMap<String, Integer> expectedHash = new HashMap<String, Integer>();
    HashMap<String, Integer> resultHash = new HashMap<String, Integer>();


    for (String exp : expected) {
      Integer val = expectedHash.get(exp);
      if (val == null) {
        expectedHash.put(exp, 1);
      }
      else {
        expectedHash.put(exp, val + 1);
      }
    }

    for (String res : result) {
      Integer val = resultHash.get(res);
      if (val == null) {
        resultHash.put(res, 1);
      }
      else {
        resultHash.put(res, val + 1);
      }
    }

    assertEquals("Arrays should contain same number of elements. Expected " +
                 expectedHash.keySet() + " but received " + resultHash.keySet(),
                 expectedHash.keySet().size(), resultHash.keySet().size());

    for (String key : expectedHash.keySet()) {
      Integer expValue = expectedHash.get(key);
      Integer resValue = resultHash.get(key);

      assertEquals("Expected value " + key + " " + expValue + " times but received " + resValue + " times", 
                   expValue, resValue);
    }

  }

}
