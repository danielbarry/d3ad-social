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
  private static final int MAX_READ = 2048;

  private static ConcurrentHashMap<String, ReentrantReadWriteLock> resources =
    new ConcurrentHashMap<String, ReentrantReadWriteLock>();

  /**
   * read()
   *
   * Read from file on the disk, returning NULL on error.
   *
   * @param path The location read from.
   * @return The data on success, otherwise NULL.
   **/
  public static String read(String path){
    String data = null;
    byte[] raw = new byte[MAX_READ];
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
    boolean result = true;
    byte[] raw = data.getBytes();
    File file = new File(path);
    resources.putIfAbsent(path, new ReentrantReadWriteLock(true));
    ReentrantReadWriteLock rrwl = resources.get(path);
    rrwl.writeLock().lock();
    /* Write file */
    try{
      FileOutputStream fos = new FileOutputStream(file, false);
      fos.write(raw);
      fos.flush();
      fos.close();
    }catch(IOException e){
      result = false;
    }
    rrwl.writeLock().unlock();
    return result;
  }
}
