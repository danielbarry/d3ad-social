package b.ds;

import java.util.HashMap;

/**
 * HandlerLogin.java
 *
 * Generate a generic login page.
 **/
public class HandlerLogin extends Handler{
  /**
   * HandlerLogin()
   *
   * Initialise the variables required to deliver a login page.
   *
   * @param kv The key value data from the header.
   **/
  public HandlerLogin(HashMap<String, String> kv){
    /* Do nothing */
  }

  @Override
  public byte[] genBody(){
    return (
      "<form action=\"/login\" method=\"post\">" +
        "<label for=\"username\">username:</label>" +
        "<br>" +
        "<input type=\"text\" id=\"username\" name=\"username\">" +
        "<br>" +
        "<label for=\"password\">password:</label>" +
        "<br>" +
        "<input type=\"password\" id=\"password\" name=\"password\">" +
        "<br>" +
        "<input type=\"submit\" value=\"submit\">" +
      "</form>" +
      "<a href=\"/register\">register</a>"
    ).getBytes();
  }
}
