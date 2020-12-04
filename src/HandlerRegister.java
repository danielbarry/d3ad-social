package b.ds;

import java.util.HashMap;

/**
 * HandlerRegister.java
 *
 * Generate a generic registration page.
 **/
public class HandlerRegister extends Handler{
  /**
   * HandlerRegister()
   *
   * Initialise the variables required to deliver a registration page.
   *
   * @param kv The key value data from the header.
   **/
  public HandlerRegister(HashMap<String, String> kv){
    /* TODO: Take values from key values of interest. */
  }

  @Override
  public byte[] genBody(){
    /* TODO: Implement this. */
    return (
      "<b>Warning:</b> The security was hacked together, please use random credentials." +
      "<form action=\"/register\" method=\"post\">" +
        "<label for=\"username\">username:</label>" +
        "<br>" +
        "<input type=\"text\" id=\"username\" name=\"username\">" +
        "<br>" +
        "<label for=\"passworda\">password:</label>" +
        "<br>" +
        "<input type=\"password\" id=\"passworda\" name=\"passworda\">" +
        "<br>" +
        "<label for=\"passwordb\">password (repeat):</label>" +
        "<br>" +
        "<input type=\"password\" id=\"passwordb\" name=\"passwordb\">" +
        "<br>" +
        "<input type=\"submit\" value=\"submit\">" +
      "</form>" +
      "By registering, you agree to the site rules:" +
      "<br>" +
      "1. Don't contribute content you wouldn't show your mother." +
      "<br>" +
      "2. Don't abuse the service - if in doubt, ask."
    ).getBytes();
  }
}
