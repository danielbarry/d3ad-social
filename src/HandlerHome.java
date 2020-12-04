package b.ds;

import java.util.HashMap;

/**
 * HandlerHome.java
 *
 * Generate a generic home page that doesn't require a login.
 **/
public class HandlerHome implements Handler{
  /**
   * HandlerHome()
   *
   * Initialise the variables required to deliver a home page.
   *
   * @param kv The key value data from the header.
   **/
  public HandlerHome(HashMap<String, String> kv){
    /* TODO: Take values from key values of interest. */
  }

  public byte[] process(){
    /* TODO: Implement this. */
    return "boo yah".getBytes();
  }
}
