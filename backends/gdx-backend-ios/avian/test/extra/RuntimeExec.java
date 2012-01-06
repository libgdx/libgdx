package extra;

import java.lang.Runtime;
import java.lang.Process;

public class RuntimeExec {
  public static void main(String[] args) throws java.io.IOException, java.lang.InterruptedException {
    Runtime runtime = Runtime.getRuntime();
    String ieStr = null;
    String charmapStr = null;
    String[] firefox = new String[2];
    
    if(System.getProperty("os.name").equals("windows")){
      System.out.println("Executing internet explorer");
      ieStr = "\"c:\\program files\\internet explorer\\iexplore.exe\" http://www.google.com"; 
    } else {
      System.out.println("Executing Firefox using string");
      ieStr = "firefox http://www.google.com";
    }
    Process ie = runtime.exec(ieStr);
    
    if(System.getProperty("os.name").equals("windows")){
      System.out.println("Executing firefox");
      firefox[0] = "c:\\program files\\mozilla firefox\\firefox.exe";
      firefox[1] = "http://www.google.com";
    } else {
      System.out.println("Executing Firefox using array");
      firefox[0] = "firefox";
      firefox[1] = "http://www.google.com";
    }
    Process ff = runtime.exec(firefox);

    boolean ffSuccess = false;
    boolean ieSuccess = false;
    while(!(ieSuccess && ffSuccess)){
      if(!ffSuccess){
        try{
          System.out.println("Exit value from string exec: " + ff.exitValue());
          ffSuccess = true;
        } catch(IllegalThreadStateException e) {}
      }
      if(!ieSuccess){
        try{
          System.out.println("Exit value from array exec: " + ie.exitValue());
          ieSuccess = true;
        } catch(IllegalThreadStateException e) {}
      }
    }
    if(System.getProperty("os.name").equals("windows")){
      System.out.println("Executing and waiting for charmap");
      charmapStr = "c:\\windows\\system32\\charmap.exe";
    } else {
      System.out.println("Executing and waiting for firefox");
      charmapStr = "firefox http://www.google.com";
    }
    Process cm = runtime.exec(charmapStr);
    System.out.println("Exit value: " + cm.waitFor());
  }
}
