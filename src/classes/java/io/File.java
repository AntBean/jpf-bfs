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
package java.io;


import gov.nasa.jpf.FileInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * MJI model class for java.io.File
 *
 * NOTE - a number of methods are only stubbed out here to make Eclipse compile
 * JPF code that uses java.io.File (there is no way to tell Eclipse to exclude the
 * model classes from ths build-path)
 *
 * @author Owen O'Malley
 */
public class File
{
  public static final String separator = System.getProperty("file.separator");
  public static final char separatorChar = separator.charAt(0);
  public static final String pathSeparator = System.getProperty("path.separator");
  public static final char pathSeparatorChar = pathSeparator.charAt(0);

  int id; // link to the real File object
  private String filename;
  private String cannonicalPath;

  private FileInfo fileInfo;

  private FileInfo getFileInfo() {
    System.out.println("getFileInfo()");
    if (fileInfo == null) {
      fileInfo = FileInfo.getFileInfo(cannonicalPath);
    }
    
    if (fileInfo == null) {
      System.out.println("Returned FileInfo - null");
    }
    else {
      System.out.println("Returned FileInfo - " + fileInfo);
    }

    return fileInfo;
  }

  public File(String filename) {

    System.out.println("File(String)");

    if (filename == null){
      throw new NullPointerException();
    }
    
    cannonicalPath = getCannonicalPath(filename);
  }

  private static native String getCannonicalPath(String filename);

  public File (String parent, String child) {
  	cannonicalPath = parent + separator + child;
  }
  
  public File (File parent, String child) {
    cannonicalPath = parent.cannonicalPath + separator + child;
  }
  
  public File(java.net.URI uri) { throw new UnsupportedOperationException(); }
  
  public String getName() {
    int idx = cannonicalPath.lastIndexOf(separatorChar);
    if (idx >= 0){
      return cannonicalPath.substring(idx+1);
    } else {
      return cannonicalPath;
    }
  }

  public String getParent() {
    int idx = cannonicalPath.lastIndexOf(separatorChar);
    if (idx >= 0){
      return cannonicalPath.substring(0,idx);
    } else {
      return null;
    }
  }
  
  public int compareTo(File that) {
    return this.cannonicalPath.compareTo(that.cannonicalPath);
  }
  
  public boolean equals(Object o) {
    if (o instanceof File){
      File otherFile = (File) o;
      return cannonicalPath.equals(otherFile.cannonicalPath);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return cannonicalPath.hashCode();
  }
  
  public String toString()  {
    return cannonicalPath;
  }
  
  
  //--- native peer intercepted (hopefully)
  
  int getPrefixLength() { return 0; }
  public native File getParentFile();
  
  public String getPath() {
    return filename;
  }

  public native boolean isAbsolute();
  public native String getAbsolutePath();
  public native File getAbsoluteFile();
  public native String getCanonicalPath() throws java.io.IOException;

  public native File getCanonicalFile() throws java.io.IOException;

  private native String getURLSpec();
  public java.net.URL toURL() throws java.net.MalformedURLException {
    return new URL(getURLSpec());
  }

  private native String getURISpec();
  public java.net.URI toURI() {
    try {
      return new URI(getURISpec());
    } catch (URISyntaxException x){
      return null;
    }
  }

  public boolean canRead() { 
    System.out.println("File.canRead()");
    getFileInfo();

    if (fileInfo != null) {
      return fileInfo.getFileState().isReadableForSUT();
    }

    return false;
  }

  public boolean setReadable(boolean readable) {
    System.out.println("File.setReadable()");
    getFileInfo();

    if (fileInfo != null) {
      fileInfo.getFileState().setReadableForSUT(readable);

      return true;
    }

    return false;
  }

  public boolean canWrite() {
    System.out.println("File.canWrite()");
    getFileInfo();

    if (fileInfo != null) {
      return fileInfo.getFileState().isWritableForSUT();
    }

    return false;
  }

  public boolean exists() {
    System.out.println("File.exists()");
    getFileInfo();

    if (fileInfo != null) {
      return fileInfo.exists();
    }

    return false;
  }

  public boolean isDirectory() { 
    System.out.println("File.isDirectory()");
    getFileInfo();
    
    if (fileInfo != null && fileInfo.exists()) {
      return fileInfo.getFileState().isDir();
    }
    
    return false;
  }

  public boolean isFile() {
    System.out.println("File.isFile()");
    getFileInfo();

    if (fileInfo != null && fileInfo.exists()) {
      return !fileInfo.getFileState().isDir();
    }

    return false;
  }

  public boolean isHidden() { return false; }
  public long lastModified() { return -1L; }
  public long length() { return -1; }


  public boolean createNewFile() throws java.io.IOException {
    System.out.println("File.createNewFile()");
    
    return FileInfo.createNewFile(cannonicalPath);
  }

  public boolean delete() {
    System.out.println("File.delete()");
    getFileInfo();

    if (fileInfo != null) {
      return fileInfo.delete();
    }

    return false;
  }


  public void deleteOnExit() {}
  
  public String[] list()  {
    System.out.println("File.list()");
    getFileInfo();

    if (fileInfo != null) {
      return fileInfo.list();
    }

    return null;
  }

  public String[] list(FilenameFilter filter)  {
    System.out.println("File.list(FilenameFilter)");
    getFileInfo();

    if (fileInfo != null) {
      String[] childs = fileInfo.list();

      int shift = 0;

      for (int i = 0 ; i < childs.length; i++) {
        if (!filter.accept(this, childs[i])) {
          shift++;
        }
        else {
          if (shift != 0) {
            childs[i - shift] = childs[i];
          }
        }
      }

      String[] filteredChilds = new String[childs.length - shift];

      System.arraycopy(childs, 0, filteredChilds, 0, childs.length - shift);

      return childs;
    }

    return null;
  }
  
  public File[] listFiles()  { return null; }
  public File[] listFiles(FilenameFilter fnf)  { return null; }
  public File[] listFiles(FileFilter ff)  { return null; }

  public boolean mkdir() {
    System.out.println("File.mkdir()");
    return FileInfo.mkdir(cannonicalPath);
  }
  
  public boolean mkdirs() { return false; }
  public boolean renameTo(File f)  { return false; }
  public boolean setLastModified(long t)  { return false; }
  public boolean setReadOnly()  { return false; }
  
  public static native File[] listRoots();
  
  public static File createTempFile(String prefix, String suffix, File dir) throws IOException  {
    if (prefix == null){
      throw new NullPointerException();
    }
    
    String tmpDir;
    if (dir == null){
      tmpDir = System.getProperty("java.io.tmpdir");
      if (tmpDir == null){
        tmpDir = ".";
      }
      if (tmpDir.charAt(tmpDir.length()-1) != separatorChar){
        tmpDir += separatorChar;
      }
      
      if (suffix == null){
        suffix = ".tmp";
      }
    } else {
      tmpDir = dir.getPath();
    }
    
    return new File(tmpDir + prefix + suffix);
  }
  
  public static File createTempFile(String prefix, String suffix) throws IOException  {
    return createTempFile(prefix, suffix, null);
  }
}
