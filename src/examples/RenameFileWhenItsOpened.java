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

import java.io.File;
import java.io.FileInputStream;

/**
 * Simple example that shows how an error when opened file is renamed can be found.
 * 
 * @author Ivan Mushketik
 */
public class RenameFileWhenItsOpened {

  public static void main(String[] args) throws Exception {
    String fileName = "testFile";
    File testFile = new File(fileName);
    testFile.createNewFile();
    
    // Open file
    FileInputStream fis = new FileInputStream(testFile);
   
    File toRename = new File("toRename");
    // Renaming file when it's opened can be treated as an error, or can produce
    // warnings. Behaviour can be set with jpf-bfs.opened_rename config key
    testFile.renameTo(toRename);
    
  }
}
