package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * HandlerAbout.java
 *
 * Generate server information.
 **/
public class HandlerAbout extends Handler{
  private Auth auth;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    /* Do nothing */
  }

  /**
   * HandlerAbout()
   *
   * Initialise the variables required to deliver basic server information.
   *
   * @param kv The key value data from the header.
   * @param auth Access to the authentication object.
   **/
  public HandlerAbout(HashMap<String, String> kv, Auth auth){
    this.auth = auth;
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    Str res = new Str(256);
    res.append("<h2>about server</h2>");
    /* Generate system stats */
    OperatingSystemMXBean osb = ManagementFactory.getOperatingSystemMXBean();
    res.append("<p><b>system</b>")
      .append("<quote>Arch: ")
        .append(osb.getArch())
        .append("</quote>")
      .append("<quote>OS: ")
        .append(osb.getName())
        .append("</quote>")
      .append("<quote>Version: ")
        .append(osb.getVersion())
        .append("</quote>")
      .append("</p>");
    /* Generate memory stats */
    res.append("<p><b>memory</b>");
    long memTotal = Runtime.getRuntime().totalMemory();
    res.append("<quote>Total: ")
      .append(formatSize(memTotal))
      .append("</quote>");
    long memFree = Runtime.getRuntime().freeMemory();
    res.append("<quote>Free: ")
      .append(formatSize(memFree))
      .append("</quote>");
    long memUsed = memTotal - memFree;
    res.append("<quote>Used: ")
      .append(formatSize(memUsed))
      .append("</quote>");
    res.append("</p>");
    /* Generate CPU stats */
    res.append("<p><b>CPU</b>")
      .append("<quote>Processors: ")
        .append(Integer.toString(osb.getAvailableProcessors()))
        .append("</quote>")
      .append("<quote>Load average: ")
        .append(Double.toString(osb.getSystemLoadAverage() * 100))
        .append("%</quote>")
      .append("</p>");
    /* Generate user stats */
    res.append("<p><b>users</b>")
      .append("<quote>Registered: ")
        .append(Integer.toString(auth.getNumUsers()))
        .append("</quote>")
      .append("<quote>Active: ")
        .append(Integer.toString(auth.getActiveUsers()))
        .append("</quote>")
      .append("</p>");
    /* Generate post stats */
    res.append("<p><b>posts</b>")
      .append("<quote>Cached: ")
        .append(Integer.toString(Post.getNumActive()))
        .append("</quote>")
      .append("</p>");
    os.write(res.toByteArray());
  }

  /**
   * formatSize()
   *
   * Format the size to something meaningful.
   *
   * @param size The size to be formatted.
   * &return The formatted size string representing the data.
   **/
  private static String formatSize(long size){
    if(size < 1024){
      return Long.toString(size) + " bytes";
    }else if(size < 1048576){
      return String.format("%.2f kilobytes", (double)size / 1024);
    }else if(size < 1073741824){
      return String.format("%.2f Megabytes", (double)size / 1048576);
    }else if(size < 1099511627776L){
      return String.format("%.2f Gigabytes", (double)size / 1073741824);
    }else if(size < 1125899906842624L){
      return String.format("%.2f Terabytes", (double)size / 1099511627776L);
    }else{
      return "many bytes";
    }
  }
}
