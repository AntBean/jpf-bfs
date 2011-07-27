//
// Copyright (C) 2007 United States Government as represented by the
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
package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.annotation.JPFOptions;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.Heap;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Listener that stored all methods relate to a file system operations, and output
 * them in selected format in case of property violation.
 * @author Ivan Mushketik
 */
@JPFOptions ({
  @JPFOption(type="String", key="file-listener.output-format", defaultValue="table", 
             comment="Format of the output of the file operations that lead to an"
                   + "error; table = format file operations in table like format;"
                   + "raw = raw output format"),
  @JPFOption(type="boolean", key="file-listener.log-constructors", defaultValue="falsee",
             comment="Define if calls to constructors methods should be saved and "
                   + "outputed on property violation")
})
public class FileListener extends ListenerAdapter {
  private static final Set<String> classToLog = new HashSet<String>() {{ 
    add("java.io.File");
    add("java.io.RandomAccessFile");
    add("java.io.FileInputStream");
    add("java.io.FileOutputStream");
  }};
  
  private static final String OUTPUT_FORMAT_KEY = "file-listener.output-format";
  private static final String LOG_CONSTRUCTORS_KEY = "file-listener.log-constructors";
  
  private static enum OutputFormat {
    RAW,
    TABLE
  }

  private OutputFormat outputFormat;
  private boolean logConstructors;
  
  public FileListener(Config config, JPF jpf){
    jpf.addPublisherExtension(ConsolePublisher.class, this);
    
    logConstructors = config.getBoolean(LOG_CONSTRUCTORS_KEY, false);
    outputFormat = config.getEnum(OUTPUT_FORMAT_KEY, OutputFormat.values(), OutputFormat.TABLE);
  }
  
  static class Transition {
    FileOperation lastFileOp;
    Transition prevTransition;
    int stateId;
  }
  
  static class FileOperation {
    MethodInfo mi;
    ThreadInfo ti;
    String canonicalFilePath;
    
    FileOperation prevFileOp;
    
    public FileOperation(JVM vm) {      
      this.mi = vm.getLastMethodInfo();
      this.ti = vm.getCurrentThread();
      
      canonicalFilePath = getFilePath(vm);
    }
    
    private String getFilePath(JVM vm) {
      ThreadInfo ti = vm.getLastThreadInfo();
      StackFrame frame = ti.getTopFrame();
      
      if (frame != null) {

        MethodInfo mi = vm.getLastMethodInfo();
        int ref = frame.getThis();
        
        if (ref > 0) {
          Heap heap = vm.getHeap();
          ElementInfo ei = heap.get(ref);
          
          String className = ei.getClassInfo().getName();
          
          // We log operations from File, FileXStream, RandomAccessFile. File
          // has a "canonicalPath" field, other classes has fd (FileDescriptor)
          // object reference that has "canonicalPath" field
          if (!className.equals("java.io.File")) {
            int fdRef = ei.getReferenceField("fd");
            ei = heap.get(fdRef);
          }
          
          if (ei != null) {
            return ei.getStringField("canonicalPath");
          }
          
        }
      }
      
      return null;
    }
  }
  
  private Transition lastTransition = new Transition();
  private int currStateId;  
  
  private void printTableHeader(PrintWriter pw) {
    pw.format("%5s %10s %-70s %-50s\n", "state", "thread", "file", "method");
    
  }
  
  private void printTableReport(PrintWriter pw) {
    printTableHeader(pw);
    
    for (Transition transition = lastTransition; transition != null; transition = transition.prevTransition) {
      for (FileOperation fo = transition.lastFileOp; fo != null; fo = fo.prevFileOp) {
        pw.format("%5d %10s %-70s %-50s\n", transition.stateId, fo.ti.getName(), fo.canonicalFilePath, fo.mi.getFullName());
      }
    }
  }

  
  private void printRawReport(PrintWriter pw) {
    for (Transition transition = lastTransition; transition != null; transition = transition.prevTransition) {
      for (FileOperation fo = transition.lastFileOp; fo != null; fo = fo.prevFileOp) {
        pw.println(transition.stateId + "; " + fo.ti + "; " + fo.canonicalFilePath + "; " + fo.mi.getFullName());
      }
    }
  }
  
  @Override
  public void methodEntered(JVM vm) {    
    if (shouldLogMethodClass(vm)) {
      addNewFileOperation(vm);
    }
  }
  
  private void addNewFileOperation(JVM vm) {    
    FileOperation newFI = new FileOperation(vm);
    
    newFI.prevFileOp = lastTransition.lastFileOp;
    lastTransition.lastFileOp = newFI;
  }
  
  private boolean shouldLogMethodClass(JVM vm) {
    MethodInfo mi = vm.getLastMethodInfo();     
    if ((mi.isCtor() || mi.isClinit()) && !logConstructors) {
      return false;
    }
    
    ClassInfo ci = mi.getClassInfo();
    
    return classToLog.contains(ci.getName());           
  }
  
  private void saveLastTransition() {
    if (lastTransition.lastFileOp != null) {
      lastTransition.stateId = currStateId;
      
      Transition newTransition = new Transition();
      newTransition.prevTransition = lastTransition;
      lastTransition = newTransition;
      
    }
  }
  
  @Override
  public void stateAdvanced(Search search) {
    currStateId = search.getStateId();
    if (search.isNewState()) {
      saveLastTransition();
    }
  }
  
  @Override
  public void stateBacktracked(Search search) {
    
    int stateId = search.getStateId();
    while ((lastTransition != null) && (lastTransition.stateId > stateId)){
      lastTransition = lastTransition.prevTransition;
    }
    
  }
  
  HashMap<Integer, Transition> storedTransition = new HashMap<Integer,Transition>();
  
  @Override
  public void stateStored (Search search) {
    storedTransition.put(search.getStateId(), lastTransition);
  }
  
  @Override
  public void stateRestored(Search search) {
    int stateId = search.getStateId();
    Transition transition = storedTransition.get(stateId);
    if (transition != null) {
      lastTransition = transition;
      storedTransition.remove(stateId);  // not strictly required, but we don't come back
    }  
  }
  
  @Override
  public void publishPropertyViolation (Publisher publisher) {
    saveLastTransition();
    PrintWriter pw = publisher.getOut();
    
    if (outputFormat == OutputFormat.TABLE) {
      printTableReport(pw);
    }
    else if (outputFormat == OutputFormat.RAW) {
      printRawReport(pw);
    }
  }
}
