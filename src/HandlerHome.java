package b.ds;

import java.util.HashMap;

/**
 * HandlerHome.java
 *
 * Generate a generic home page.
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
    /* Do nothing */
  }

  @Override
  public byte[] genBody(){
    /* TODO: Return default content here. */
    return (
      "Content here"
    ).getBytes();
  }
}
