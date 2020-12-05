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
  private static final char[] HEX =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

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

  /**
   * genRandHash()
   *
   * Generate a secure hash to be used for salting, IDs, etc. This function is
   * expensive to call and should not be done so unless really needed.
   *
   * @return The securely random hash.
   **/
  public static byte[] genRandHash(){
    SecureRandom sr = new SecureRandom();
    byte[] hash = new byte[64];
    sr.nextBytes(hash);
    return hash;
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
  public static String genPassHash(byte[] salt, byte[] usalt, String password){
    String hash = null;
    try{
      MessageDigest md = MessageDigest.getInstance("SHA-512");
      md.update(salt);
      md.update(usalt);
      byte[] bytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
      hash = bytesToHex(bytes);
    }catch(NoSuchAlgorithmException e){
      error("Unable to hash password");
      hash = null;
    }
    return hash;
  }

  /**
   * bytesToHex()
   *
   * Encode a byte array as hex String.
   *
   * @param b The bytes array to be encoded.
   * @return The encoded hex String.
   **/
  public static String bytesToHex(byte[] b){
    char[] s = new char[2 * b.length];
    int i = 0;
    for(int x = 0; x < b.length; x++){
      s[i++] = HEX[(0xF0 & b[x]) >>> 4];
      s[i++] = HEX[(0x0F & b[x])      ];
    }
    return new String(s);
  }

  /**
   * hexToBytes()
   *
   * Decode a hex String as a byte array.
   *
   * @param cs The string to be decoded.
   * @return The decoded byte array.
   **/
  public static byte[] hexToBytes(CharSequence cs){
    int nChars = cs.length();
    if(nChars % 2 != 0){
      Utils.warn("Invalid String length for decoding");
    }
    byte[] result = new byte[nChars / 2];
    for(int i = 0; i < nChars; i += 2){
      int msb = Character.digit(cs.charAt(i), 16);
      int lsb = Character.digit(cs.charAt(i + 1), 16);
      if(msb < 0 || lsb < 0){
        Utils.warn("Non hexadecimal character encountered, unknown results");
      }
      result[i / 2] = (byte) ((msb << 4) | lsb);
    }
    return result;
  }
}
