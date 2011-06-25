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
 * MJI model class for backtrackable java.io.File.
 *
 * @author Ivan Mushketik
 */
public class File
{
  public static final String separator = System.getProperty("file.separator");
  public static final char separatorChar = separator.charAt(0);
  public static final String pathSeparator = System.getProperty("path.separator");
  public static final char pathSeparatorChar = pathSeparator.charAt(0);

  int id; // link to the real File object
  private String filename;
  private String canonicalPath;

  private FileInfo fileInfo;

  protected FileInfo getFileInfo() {
    return fileInfo;
  }

  public File(String filename) {

    System.out.println("File(String)");

    if (filename == null){
      throw new NullPointerException();
    }
    
    this.filename = filename;
    canonicalPath = getCanonicalPath(filename);
    fileInfo = FileInfo.getFileInfo(canonicalPath);
  }

  private static native String getCanonicalPath(String filename);

  public File (String parent, String child) {
  	filename = parent + separator + child;
    canonicalPath = getCanonicalPath(filename);
  }
  
  public File (File parent, String child) {
    canonicalPath = parent.canonicalPath + separator + child;
  }
  
  public File(java.net.URI uri) { throw new UnsupportedOperationException(); }
  
  public String getName() {
    int idx = canonicalPath.lastIndexOf(separatorChar);
    if (idx >= 0){
      return canonicalPath.substring(idx+1);
    } else {
      return canonicalPath;
    }
  }

  public String getParent() {
    int idx = filename.lastIndexOf(separatorChar);
    if (idx >= 0){
      return filename.substring(0,idx);
    } else {
      return null;
    }
  }

  public File getParentFile() {
    String parentFileName = getParent();

    if (parentFileName != null) {
      return new File(parentFileName);
    }

    return null;
  }

  
  public int compareTo(File that) {
    return this.filename.compareTo(that.filename);
  }
  
  public boolean equals(Object o) {
    if (o instanceof File){
      File otherFile = (File) o;
      return filename.equals(otherFile.filename);
    } else {
      return false;
    }
  }
  
  public int hashCode() {
    return filename.hashCode();
  }
  
  public String toString()  {
    return filename;
  }
  
  int getPrefixLength() { return 0; }
  
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
    return fileInfo.getFileState().isReadableForSUT();
    

    
  }

  public boolean setReadable(boolean readable) {
    System.out.println("File.setReadable()");

    if (fileInfo.exists()) {
      fileInfo.getFileState().setReadableForSUT(readable);

      return true;
    }

    return false;
  }

  public boolean canWrite() {
    System.out.println("File.canWrite()");

    if (fileInfo.exists()) {
      return fileInfo.getFileState().isWritableForSUT();
    }

    return false;
  }

  public boolean setWritable(boolean writable) {
    System.out.println("File.setReadable()");

    if (fileInfo.exists()) {
      fileInfo.getFileState().setWritableForSUT(writable);

      return true;
    }

    return false;
  }

  public boolean canExecute() {
    System.out.println("File.canWrite()");

    if (fileInfo.exists()) {
      return fileInfo.getFileState().isExecutableForSUT();
    }

    return false;
  }

  public boolean setExecutable(boolean executable) {
    System.out.println("File.setReadable()");

    if (fileInfo.exists()) {
      fileInfo.getFileState().setExecutableForSUT(executable);

      return true;
    }

    return false;
  }

  public boolean exists() {
    System.out.println("File.exists()");

    if (fileInfo != null) {
      return fileInfo.exists();
    }

    return false;
  }

  public boolean isDirectory() { 
    System.out.println("File.isDirectory()");
    
    if (fileInfo.exists()) {
      return fileInfo.getFileState().isDir();
    }
    
    return false;
  }

  public boolean isFile() {
    System.out.println("File.isFile()");

    if (fileInfo != null && fileInfo.exists()) {
      return !fileInfo.getFileState().isDir();
    }

    return false;
  }

  public native boolean isHidden();
  
  public long length() {
    System.out.println("File.length()");

    return fileInfo.getFileState().getLength();
  }

  public boolean createNewFile() throws java.io.IOException {
    System.out.println("File.createNewFile()");
    
    return fileInfo.createNewFile(canonicalPath);
  }

  public boolean delete() {
    System.out.println("File.delete()");
    return fileInfo.delete();
  }

  public void deleteOnExit() {}
  
  public String[] list()  {
    System.out.println("File.list()");
    String[] childsCP = fileInfo.list();

    if (childsCP != null) {
      String[] childsNames = new String[childsCP.length];

      for (int i = 0; i < childsCP.length; i++) {
        childsNames[i] = childsCP[i].substring(canonicalPath.length() + 1);
      }

      return childsNames;
    }
    
    return null;
  }

  public String[] list(FilenameFilter filter)  {
    System.out.println("File.list(FilenameFilter)");    
    String[] childs = list();

    if (childs != null) {
      int shift = 0;

      for (int i = 0; i < childs.length; i++) {
        if (!filter.accept(this, childs[i])) {
          shift++;
        } else {
          if (shift != 0) {
            childs[i - shift] = childs[i];
          }
        }
      }

      String[] filteredChilds = new String[childs.length - shift];

      System.arraycopy(childs, 0, filteredChilds, 0, childs.length - shift);

      return filteredChilds;
    }

    return null;
  }
  
  public File[] listFiles()  {
    System.out.println("File.listFiles()");
    
    String[] childs = list();
    if (childs != null) {
      File[] result = new File[childs.length];

      for (int i = 0; i < childs.length; i++) {
        result[i] = new File(canonicalPath, childs[i]);
      }

      return result;
    }

    return null;
  }
  
  public File[] listFiles(FilenameFilter filter) {
    System.out.println("File.listFiles(FilenameFilter)");

    if (fileInfo != null) {
      String[] childs = list(filter);

      if (childs != null) {
        File[] result = new File[childs.length];

        for (int i = 0; i < childs.length; i++) {
          result[i] = new File(canonicalPath, childs[i]);
        }

        return result;
      }

      return null;
    }

    return null;
  }
  
  public File[] listFiles(FileFilter filter) {
    System.out.println("File.listFiles(FilenameFilter)");
    
    File[] children = listFiles();

    if (children != null) {
      int shift = 0;

      for (int i = 0; i < children.length; i++) {
        if (!filter.accept(children[i])) {
          shift++;
        } else {
          if (shift != 0) {
            children[i - shift] = children[i];
          }
        }
      }

      File[] filteredChildren = new File[children.length - shift];

      System.arraycopy(children, 0, filteredChildren, 0, children.length - shift);

      return filteredChildren;
    }

    return null;
  }

  public boolean mkdir() {
    System.out.println("File.mkdir()");
    return fileInfo.mkdir();
  }
  
  public boolean mkdirs() {
    System.out.println("File.mkdirs()");

    return fileInfo.mkdirs(true);
  }


  public boolean renameTo(File f)  { 
    System.out.println("File.renameTo()");
    return fileInfo.renameTo(f.canonicalPath);
  }

  public long lastModified() {
    System.out.println("File.setLastModified()");

    if (fileInfo.exists()) {
      return fileInfo.getFileState().getLastModified();
    }

    return -1;
  }
  
  public boolean setLastModified(long time)  {
    System.out.println("File.setLastModified()");

    if (fileInfo.exists()) {
      fileInfo.getFileState().setLastModified(time);

      return true;
    }

    return false;
  }

  public boolean setReadOnly()  { return false; }
  
  public static File[] listRoots() {
    String[] rootsNames = listRootsNames();
    File[] roots = new File[rootsNames.length];

    for (int i = 0; i < rootsNames.length; i++) {
      roots[i] = new File(rootsNames[i]);
    }

    return roots;
  }

  public static native String[] listRootsNames();

  public native long getFreeSpace();

  public native long getTotalSpace();

  public native long getUsableSpace();

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

    String tmpDirCP = getCanonicalPath(tmpDir);
    String tempFileName = FileInfo.createTempFile(tmpDirCP, separatorChar, prefix, suffix);

    if (tempFileName != null) {
      return new File(tempFileName);
    }

    throw new IOException("No such directory");
  }
  
  public static File createTempFile(String prefix, String suffix) throws IOException  {
    return createTempFile(prefix, suffix, null);
  }
}
