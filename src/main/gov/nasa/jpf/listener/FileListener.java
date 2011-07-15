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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Ivan Mushketik
 */
public class FileListener extends ListenerAdapter {
  private static final Set<String> classToLog = new HashSet<String>() {{ 
    add("java.io.File");
    add("java.io.RandomAccessFile");
    add("java.io.FileInputStream");
    add("java.io.FileOutputStream");
  }};

  public FileListener(Config config, JPF jpf){
    jpf.addPublisherExtension(ConsolePublisher.class, this);
  }
  
  static class Transition {
    FileOperation lastFileOp;
    Transition prevTransition;
    int stateId;
  }
  
  static class FileOperation {
    MethodInfo mi;
    ThreadInfo ti;
    
    FileOperation prevFileOp;
    
    public FileOperation(JVM vm) {      
      this.mi = vm.getLastMethodInfo();
      this.ti = vm.getCurrentThread();
    }
  }
  
  private Transition lastTransition = new Transition();
  private int currStateId;  
  
  private void printRawReport(PrintWriter pw) {
    for (Transition transition = lastTransition; transition != null; transition = transition.prevTransition) {
      for (FileOperation fo = transition.lastFileOp; fo != null; fo = fo.prevFileOp) {
        pw.println("State = " + transition.stateId + ";thread = " + fo.ti + ";method = " + fo.mi.getFullName());
      }
    }
  }
  
  @Override
  public void methodEntered(JVM vm) {    
    if (shouldLogMethodClass(vm)) {
      log(vm.getLastMethodInfo());
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
    log("State advanced");
    log(currStateId);
    if (search.isNewState()) {
      log("NEW STATE");
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
    printRawReport(pw);
  }
    
  
  
  private void log(Object o) {
    System.out.println(o);
  }
}
