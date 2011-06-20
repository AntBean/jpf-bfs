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
package gov.nasa.jpf.bfs;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import java.util.logging.Logger;

/**
 *
 * @author Ivan Mushketik
 */
public class FSMode {

  // Do nothing
  public static final int NOTHING = 0;
  // Report a warning
  public static final int WARNING = 1;
  // Report an error
  public static final int ERROR = 2;

  // Keys for different modes in config file
  private static final String DO_NOTHING = "nothing";
  private static final String REPORT_WARNING = "warning";
  private static final String THROW_ERROR = "error";

  /**
   * Parse FSMode value from config
   * @param config - config to read value from
   * @param key - key to read string value
   * @return
   */
  public static int parseOnOpened(Config config, String key) {
    String value = config.getString(key);

    if (value == null || value.equals(DO_NOTHING)) {
      return NOTHING;
    } else if (value.equals(REPORT_WARNING)) {
      return WARNING;
    } else if (value.equals(THROW_ERROR)) {
      return ERROR;
    } else {
      throw new JPFConfigException("Unexpected value '" + value +
                                   "' for key '" + key + "' in config.");
    }
  }
}
