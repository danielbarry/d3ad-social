package b.ds;

import java.util.HashMap;

/**
 * HandlerHome.java
 *
 * Generate a generic home page that doesn't require a login.
 **/
public class HandlerHome extends Handler{
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

  @Override
  public byte[] genBody(){
    /* TODO: Implement this. */
    return (
      "Content here"
    ).getBytes();
  }
}
