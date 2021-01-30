package b.ds;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utils.java
 *
 * Useful utilities use within the program.
 **/
public class Utils{
  private static final int DISK_BUFF_MAX = 4096;
  private static final int DISK_BUFF_CAP = DISK_BUFF_MAX / 32;

  private static BufferedWriter bw;
  private static Lock logLock;
  private static Str diskBuff = new Str(DISK_BUFF_CAP);

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
   * @param force True if we should force a disk write, otherwise allow the
   * logger to batch disk writes. You may want to force a disk write on an
   * error for example, where it's possible the program may crash soon.
   **/
  private static void write(String type, String msg, boolean force){
    StackTraceElement ste = Thread.currentThread().getStackTrace()[3];
    Str sb = (new Str(16))
      .append("[")
      .append(Long.toString(timestamp()))
      .append("] (")
      .append(Long.toString(Thread.currentThread().getId()))
      .append(") ")
      .append(ste.getClassName())
      .append("->")
      .append(ste.getMethodName())
      .append("()::")
      .append(Integer.toString(ste.getLineNumber()))
      .append(" [")
      .append(type)
      .append("] ")
      .append(msg)
      .append(System.lineSeparator());
    logLock.lock();
    /* Write error stream so program output can be separated from logs */
    String s = sb.toString();
    System.err.print(s);
    if(bw != null){
      /* Append to disk buffer */
      diskBuff.append(s);
      /* Only write to disk if force or buffer is filled */
      if(force || diskBuff.length() > DISK_BUFF_MAX){
        try{
          bw.append(diskBuff.toString());
          bw.flush();
          diskBuff = new Str(DISK_BUFF_CAP);
        }catch(IOException e){
          /* Don't log, we could end up in an infinite loop */
        }
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
    write(">>", msg, false);
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
    write(">>", msg + " B64:'" + Base64.getEncoder().encodeToString(unsafe.getBytes()) + "'", false);
  }

  /**
   * warn()
   *
   * Log a warning String to the terminal and disk.
   *
   * @param msg The message to be logged.
   **/
  public static void warn(String msg){
    write("!!", msg, false);
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
    write("EE", msg, true);
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

  /**
   * genRandHash()
   *
   * Generate a secure hash to be used for salting, IDs, etc. This function is
   * expensive to call and should not be done so unless really needed.
   *
   * @return The securely random hash.
   **/
  public static I512 genRandHash(){
    SecureRandom sr = new SecureRandom();
    byte[] hash = new byte[64];
    sr.nextBytes(hash);
    return new I512(hash);
  }

  /**
   * genPassHash()
   *
   * Securely hash the password using SHA512 and two salts.
   *
   * @param salt The global site salt.
   * @param usalt The unique user salt.
   * @param password The password to be hashed.
   * @return The securely hashed password. If something goes wrong, either it
   * will crash the entire program (for security) or return a NULL value.
   **/
  public static I512 genPassHash(I512 salt, I512 usalt, String password){
    I512 hash = null;
    try{
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(salt.toByteArray());
      md.update(usalt.toByteArray());
      hash = new I512(md.digest(password.getBytes(StandardCharsets.UTF_8)));
    }catch(NoSuchAlgorithmException e){
      error("Unable to hash password");
      hash = null;
    }
    return hash;
  }

  /**
   * sanitizeString()
   *
   * Sanitize a String to be HTML safe.
   *
   * @param s The String to be checked.
   * @return The sanitized String.
   **/
  public static String sanitizeString(String s){
    return new String(
             s.replaceAll("\\P{Print}", "")
              .replaceAll("&", "&amp;")
              .replaceAll("<", "&lt;")
              .replaceAll(">", "&gt;")
              .replaceAll("\"", "&quot;")
              .getBytes(StandardCharsets.US_ASCII)
           );
  }
}
