package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * HandlerTag.java
 *
 * Generate a generic tag search page.
 **/
public class HandlerTag extends Handler{
  private static byte[] error;

  private String tag;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    /* Pre-generate known strings */
    error = "<b>Unknown request</b>".getBytes();
  }

  /**
   * genHead()
   *
   * Generate the page header content.
   *
   * @param os The OutputStream to write the data to.
   * @param user The logged in user, otherwise NULL.
   **/
  public void genHead(OutputStream os, Auth.User user) throws IOException{
    os.write(head);
  }

  /**
   * HandlerTag()
   *
   * Initialise the variables required to deliver a tag page.
   *
   * @param kv The key value data from the header.
   * @param tag The tag to be searched for.
   **/
  public HandlerTag(HashMap<String, String> kv, String tag){
    this.tag = tag;
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    /* TODO: Return list of tags. */
    /* If we get here, then error */
    os.write(error);
  }
}
