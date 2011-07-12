
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//
// Copyright (C) 2006 United States Government as represented by the
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

/**
 * Simple example that show how jpf-bfs finds races when two FileInputStreams read
 * from a single file.
 * @author Ivan Mushketik
 */
public class ReadWriteFileRace {

  
  public static void main(String[] args) throws Exception {
    final String fileName = "testFile";
    
    File testFile = new File("testFile");
    // Create a test file (it will be created in BFS, not on the native FS)
    testFile.createNewFile();
    
    // Fill the file with data, so file pointer in FileDescriptor could move
    // and that would create potentional race
    FileOutputStream fos = new FileOutputStream(fileName);
    fos.write(new byte[] {1, 2, 3, 4, 5});
    fos.close();

    final FileInputStream fis1 = new FileInputStream(fileName);
    final FileInputStream fis2 = new FileInputStream(fis1.getFD());

    Runnable r1 = new Runnable() {

      public void run() {
        try {
          fis1.read();
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    };

    Runnable r2 = new Runnable() {

      public void run() {
        try {
          fis2.read();
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      }
    };

    Thread t1 = new Thread(r1);
    Thread t2 = new Thread(r2);

    t1.start();
    t2.start();
  }
}
