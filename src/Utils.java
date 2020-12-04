package b.ds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utils.java
 *
 * Useful utilities use within the program.
 **/
public class Utils{
  private static BufferedWriter bw;
  private static Lock logLock;

  static{
    bw = null;
    try{
      bw = new BufferedWriter(new FileWriter(new File("d3ad.log"), true));
    }catch(IOException e){
      bw = null;
      warn("Failed to open log file");
    }
    logLock = new ReentrantLock();
  }

  /**
   * timestamp()
   *
   * Get a time stamp in milliseconds.
   *
   * @return A time stamp in milliseconds based on the system clock.
   **/
  public static long timestamp(){
    return System.currentTimeMillis();
  }

  /**
   * write()
   *
   * Write a thread safe log message to the terminal and disk.
   *
   * @param type The type identifier for the message.
   * @param msg The message to be printed.
   **/
  private static void write(String type, String msg){
    StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
    StringBuilder sb = new StringBuilder("[");
    sb.append(timestamp());
    sb.append("] (");
    sb.append(Thread.currentThread().getId());
    sb.append(") ");
    sb.append(ste.getClassName());
    sb.append("->");
    sb.append(ste.getMethodName());
    sb.append("()::");
    sb.append(ste.getLineNumber());
    sb.append(" [");
    sb.append(type);
    sb.append("] ");
    sb.append(msg);
    String s = sb.toString();
    logLock.lock();
    /* Write error stream so program output can be separated from logs */
    System.err.println(s);
    if(bw != null){
      try{
        bw.append(s);
        bw.newLine();
        bw.flush();
      }catch(IOException e){
        /* Don't log, we could end up in an infinite loop */
      }
    }
    logLock.unlock();
  }

  /**
   * log()
   *
   * Log a String to the terminal and disk.
   *
   * @param msg The message to be logged.
   **/
  public static void log(String msg){
    write(">>", msg);
  }

  /**
   * logUnsafe()
   *
   * Log a String to the terminal and disk, which has a potentially unsafe
   * String that should be Base64 encoded. Unsafe Strings printed in the logs
   * could for example affect parsing (for example, newline characters).
   *
   * @param msg The message to be logged.
   * @param unsafe The message/data that is potentially unsafe.
   **/
  public static void logUnsafe(String msg, String unsafe){
    write(">>", msg + " B64:'" + Base64.getEncoder().encodeToString(unsafe.getBytes()) + "'");
  }

  /**
   * warn()
   *
   * Log a warning String to the terminal and disk.
   *
   * @param msg The message to be logged.
   **/
  public static void warn(String msg){
    write("!!", msg);
  }

  /**
   * error()
   *
   * Log an error String to the terminal and disk, flush the streams and exit
   * the program.
   *
   * @param msg The message to be logged.
   **/
  public static void error(String msg){
    write("EE", msg);
    if(bw != null){
      try{
        bw.flush();
        bw.close();
      }catch(IOException e){
        /* Nothing that can be done */
      }
    }
    System.exit(0);
  }
}
