package gov.nasa.jpf.test.java.io;

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class RandomAccessFileTest extends TestJPF {
  @BeforeClass
  public static void setUpClass() throws Exception {
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
  public static void tearDownClass() throws Exception {
    if (!FileUtils.removeRecursively(new File("fileSandbox"))) {
      throw new RuntimeException("Unable to remove sandbox directories");
    }
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
}
