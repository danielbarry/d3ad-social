package b.ds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Data.java
 *
 * Handle file locking to ensure a single thread has access at any one time.
 * This class should support multiple reads and a single write at any one time.
 **/
public abstract class Data{
  private static int MAX_READ;
  private static ConcurrentHashMap<String, ReentrantReadWriteLock> resources =
    new ConcurrentHashMap<String, ReentrantReadWriteLock>();

  /**
   * init()
   *
   * Initialise the static variables.
   *
   * @param config The shared configuration.
   **/
  public static void init(JSON config){
    MAX_READ = 2048;
    try{
      MAX_READ = Integer.parseInt(config.get("disk").get("max-read").value(MAX_READ + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find maximum read value");
    }
  }

  /**
   * read()
   *
   * Read from file on the disk, returning NULL on error.
   *
   * @param path The location read from.
   * @param maxRead The maximum data to be read from disk.
   * @return The data on success, otherwise NULL.
   **/
  private static String read(String path, int maxRead){
    String data = null;
    byte[] raw = new byte[maxRead];
    File file = new File(path);
    resources.putIfAbsent(path, new ReentrantReadWriteLock(true));
    ReentrantReadWriteLock rrwl = resources.get(path);
    rrwl.readLock().lock();
    /* Read file */
    try{
      FileInputStream fis = new FileInputStream(file);
      fis.read(raw);
      fis.close();
    }catch(IOException e){
      data = null;
    }
    rrwl.readLock().unlock();
    data = new String(raw);
    return data;
  }

  /**
   * read()
   *
   * Read from file on the disk, returning NULL on error. Note that this read
   * is limited by the default maximum read value.
   *
   * @param path The location read from.
   * @return The data on success, otherwise NULL.
   **/
  public static String read(String path){
    return read(path, MAX_READ);
  }

  /**
   * readLarge()
   *
   * Read a larger file from the disk, returning NULL on error.
   *
   * @param path The location read from.
   * @param maxRead Specify the maximum file size to be read from disk.
   * @return The data on success, otherwise NULL.
   **/
  public static String readLarge(String path, int maxRead){
    return read(path, maxRead);
  }

  /**
   * write()
   *
   * Generic writing to a file on the disk, returning true on success and false
   * on error. The file is completely overridden by the data given to this
   * function.
   *
   * @param path The location to write to.
   * @param data The data to be written to the location.
   * @param append Whether or not this is an append operation.
   * @return True on success, false on error.
   **/
  private static boolean write(String path, String data, boolean append){
    boolean result = true;
    byte[] raw = data.getBytes();
    File file = new File(path);
    resources.putIfAbsent(path, new ReentrantReadWriteLock(true));
    ReentrantReadWriteLock rrwl = resources.get(path);
    rrwl.writeLock().lock();
    /* Write file */
    try{
      FileOutputStream fos = new FileOutputStream(file, append);
      fos.write(raw);
      fos.flush();
      fos.close();
    }catch(IOException e){
      result = false;
    }
    rrwl.writeLock().unlock();
    return result;
  }

  /**
   * write()
   *
   * Write to file on the disk, returning true on success and false on error.
   * The file is completely overridden by the data given to this function.
   *
   * @param path The location to write to.
   * @param data The data to be written to the location.
   * @return True on success, false on error.
   **/
  public static boolean write(String path, String data){
    return write(path, data, false);
  }

  /**
   * append()
   *
   * Append data to a file on the disk, or create a new one if required. Return
   * an indication as to whether this operating was a success.
   *
   * @param path The location to write to.
   * @param data The data to be written to the location.
   * @return True on success, false on error.
   **/
  public static boolean append(String path, String data){
    return write(path, data, true);
  }

  /**
   * exists()
   *
   * Check whether a file exists.
   *
   * @param path The path to be tests.
   * @return True if it exists, otherwise false.
   **/
  public static boolean exists(String path){
    return (new File(path)).exists();
  }
}
