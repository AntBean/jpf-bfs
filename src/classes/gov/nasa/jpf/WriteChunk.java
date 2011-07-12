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

package gov.nasa.jpf;

/**
 * Each object of this class represent one chunk of data that was written by a SUT
 * on a Backtrackable FileSystem.
 * Each chunk stores a reference of chunk that was written before it, so they form 
 * a list of write chunks. Start of the list represents the most recent write operation 
 * in this SuT state. Last element of the WriteChunks list represent first write operation
 * that was performed during SuT execution path from initial state to a current state.
 * @author Ivan Mushketik
 */
public class WriteChunk {
  // Offset of data chunk in a file
  private long startPos;
  // Length of data chunk
  private int length;
  // Name of a file in a special cache directory, that stores all data that was
  // written by a SUT during its run.
  private String fileName;
  // Chunk that was written before this chunk. If null, then no data was written
  // before this one
  private WriteChunk prevChunk;

  public WriteChunk(long offset, int length, String fileName) {
    this.startPos = offset;
    this.length = length;
    this.fileName = fileName;
  }

  /**
   * Get offset from the start of the file to the beginning of the write chunk data.
   * @return the offset
   */
  public long getOffset() {
    return startPos;
  }

  /**
   * Get the length of the data written.
   * @return the length
   */
  public int getLength() {
    return length;
  }

  /**
   * Get name of the file on the native file system that stores data, that was 
   * written by this data chunk.
   * @return the fileName
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Get write chunk that was written before current one.
   * 
   * @return write chunk that was written before current one if any data was written
   * before current write chunk, null otherwise.
   */
  public WriteChunk getPrevChunk() {
    return prevChunk;
  }
}
