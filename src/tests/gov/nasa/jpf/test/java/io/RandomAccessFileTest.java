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
import static gov.nasa.jpf.test.java.io.BFSTestUtils.*;

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
      raf.seek(0);
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

  @Test
  public void testDecreaseFileLength() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 2, 3, 4, 5, 6, 7};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      File testFile = new File("fileSandbox/testFile");

      Verify.getBoolean();
      assertEquals(7, testFile.length());

      raf.setLength(3);
      raf.close();
      assertEquals(3, testFile.length());
    }
  }

  @Test
  public void testIncreaseFileLength() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 2, 3, 4, 5, 6, 7};
      raf.write(toWrite);

      raf.close();
    }

    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      File testFile = new File("fileSandbox/testFile");

      Verify.getBoolean();
      assertEquals(7, testFile.length());
      int read;
      byte[] buffer = new byte[15];
      read = raf.read(buffer);
      assertEquals(7, read);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7}, buffer, read);
      raf.seek(raf.length());
      raf.write(new byte[] {10, 20, 30});

      raf.seek(0);
      read = raf.read(buffer);
      assertEquals(10, read);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7, 10, 20, 30}, buffer, read);

      raf.setLength(7);
      assertEquals(7, testFile.length());

      raf.setLength(10);
      assertEquals(10, testFile.length());

      raf.seek(raf.length());
      raf.write(new byte[] {42});
      raf.seek(0);

      read = raf.read(buffer);
      assertEquals(11, read);
      assertReadResult(new byte[] {1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 42}, buffer, read);
    }
  }
  
  @Test
  public void OpenFileForReadWriteWithNoWriteRights() throws Exception {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      File testFile = new File("fileSandbox/testFile");
      testFile.setWritable(false);
      
      RandomAccessFile raf = new RandomAccessFile(testFile, "rws");
    }
  }
  
  @Test
  public void OpenFileForReadWithNoReadRights() throws Exception {
    if (verifyUnhandledException("java.io.FileNotFoundException")) {
      File testFile = new File("fileSandbox/testFile");
      testFile.setReadable(false);
      
      RandomAccessFile raf = new RandomAccessFile(testFile, "r");
    }
  }
  
  @Test
  public void testReadFully() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 2, 3, 4, 5, 6, 7};
      raf.write(toWrite);

      raf.close();
    }
    
    if (verifyUnhandledException("java.io.EOFException")) {      
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "r");
      raf.readFully(new byte[200]);
    }
  }
  
  @Test
  public void testRead() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      
      Verify.getBoolean();
      raf.write(201);
      raf.write(100);
      raf.write(202);
      raf.write(99);
      
      raf.seek(0);
      assertEquals(201, raf.read());
      assertEquals(100, raf.read());
      assertEquals(202, raf.read());
      assertEquals(99, raf.read());
      assertEquals(-1, raf.read());
      
    }
  }
  
  @Test
  public void testSkipBytes() throws Exception {
    if (!isJPFRun()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 2, 3, 4, 5, 6, 7};
      raf.write(toWrite);

      raf.close();
    }
    
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "r");
      
      Verify.getBoolean();
      assertEquals(4, raf.skipBytes(4));
      assertEquals(4, raf.getFilePointer());
      
      assertEquals(3, raf.skipBytes(4));
      assertEquals(7, raf.getFilePointer());
      
      assertEquals(0, raf.skipBytes(4));
      assertEquals(7, raf.getFilePointer());
      
    }
  }
  
  @Test
  public void testReadWriteBoolean() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      
      raf.writeBoolean(true);
      raf.writeBoolean(false);
      raf.writeBoolean(true);
      
      raf.seek(0);
      assertTrue(raf.readBoolean());
      assertFalse(raf.readBoolean());
      assertTrue(raf.readBoolean());
    }
  }
  
  @Test
  public void testReadBooleanFromEmptyFile() throws Exception {
    if (verifyUnhandledException("java.io.EOFException")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.readBoolean();
    }
  }
  
  @Test
  public void testReadWriteByte() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      
      raf.writeByte(10);
      raf.writeByte(20);
      raf.writeByte(30);
      
      raf.seek(0);
      assertEquals(10, raf.readByte());
      assertEquals(20, raf.readByte());
      assertEquals(30, raf.readByte());
    }
  }
  
  @Test
  public void testReadByteFromEmptyFile() throws Exception {
    if (verifyUnhandledException("java.io.EOFException")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.readByte();
    }
  }
  
  @Test
  public void testReadWriteShort() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      raf.writeShort(1042);
      raf.writeShort(2042);
      raf.writeShort(3042);

      raf.seek(0);
      assertEquals(1042, raf.readShort());
      assertEquals(2042, raf.readShort());
      assertEquals(3042, raf.readShort());
    }
  }
  
  @Test
  public void testReadShortFromEmptyFile() throws Exception {
    if (verifyUnhandledException("java.io.EOFException")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.readShort();
    }
  }
  
  @Test
  public void testReadWriteChar() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      raf.writeChar('s');
      raf.writeChar('t');
      raf.writeChar('r');

      raf.seek(0);
      assertEquals('s', raf.readChar());
      assertEquals('t', raf.readChar());
      assertEquals('r', raf.readChar());
    }
  }
  
  @Test
  public void testReadCharFromEmptyFile() throws Exception {
    if (verifyUnhandledException("java.io.EOFException")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.readChar();
    }
  }
  
  @Test
  public void testReadWriteInt() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      raf.writeInt(Integer.MAX_VALUE - 1);
      raf.writeInt(Integer.MAX_VALUE - 2);
      raf.writeInt(Integer.MAX_VALUE - 3);

      raf.seek(0);
      assertEquals(Integer.MAX_VALUE - 1, raf.readInt());
      assertEquals(Integer.MAX_VALUE - 2, raf.readInt());
      assertEquals(Integer.MAX_VALUE - 3, raf.readInt());
    }
  }
  
  @Test
  public void testReadIntFromEmptyFile() throws Exception {
    if (verifyUnhandledException("java.io.EOFException")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.readInt();
    }
  }
  
  @Test
  public void testReadWriteLong() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");

      raf.writeLong(Long.MAX_VALUE - 1);
      raf.writeLong(Long.MAX_VALUE - 2);
      raf.writeLong(Long.MAX_VALUE - 3);

      raf.seek(0);
      assertEquals(Long.MAX_VALUE - 1, raf.readLong());
      assertEquals(Long.MAX_VALUE - 2, raf.readLong());
      assertEquals(Long.MAX_VALUE - 3, raf.readLong());
    }    
  }
  
  @Test
  public void testReadLongFromEmptyFile() throws Exception {
    if (verifyUnhandledException("java.io.EOFException")) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      raf.readLong();
    }
  }
  
  @Test
  public void testWriteBytes() throws Exception {
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte bytes[] = new byte[] {1, 2, 3, 4, 5};
      String str = new String(bytes);
      
      raf.writeBytes(str);
      
      raf.seek(0);
      byte buffer[] = new byte[10];
      int read = raf.read(buffer);
      assertReadResult(bytes, buffer, read);
    }
  }
  
  @Test
  public void testSeekBeyondFileAndWrite() throws Exception {
    if (!isJPFRun()) {     
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      byte[] toWrite = {1, 2, 3, 4, 5, 6, 7};
      raf.write(toWrite);

      raf.close();
    }
    
    if (verifyNoPropertyViolation()) {
      RandomAccessFile raf = new RandomAccessFile("fileSandbox/testFile", "rws");
      
      raf.setLength(0);
      raf.write(new byte[] {1, 2, 3, 4});
      raf.seek(7);
      raf.write(30);
      assertEquals(8, raf.getFilePointer());
      
      raf.seek(0);
      byte buffer[] = new byte[10];
      int read = raf.read(buffer);
      
      assertReadResult(new byte[] {1, 2, 3, 4, 0, 0, 0, 30}, buffer, read);
      assertEquals(8, raf.length());
    }
  }
}
