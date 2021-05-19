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
