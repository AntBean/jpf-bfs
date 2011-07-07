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

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Ignore;

/**
 * Utils for BFS tests
 * @author Ivan Mushketik
 */
@Ignore
public class BFSTestUtils extends TestJPF {
  public static void assertReadResult(byte[] expected, byte[] buffer, int read) {
    assertEquals("Expected to read " + expected.length + " but read " + read, 
                 expected.length, read);
    
    
    String expectedStr = byteArrayToStr(expected, expected.length);
    String bufferStr = byteArrayToStr(buffer, read);    
    String errorMsg = "Expected " + expectedStr + " but read " + bufferStr;

    
    for (int i = 0; i < read; i++) {
      assertEquals(errorMsg, expected[i], buffer[i]);
    }
  }

  private static String byteArrayToStr(byte[] array, int length) {
    String result = "[";

    for (int i = 0; i < length; i++) {
      result += array[i] + ", ";
    }

    return result + "]";
  }
}
