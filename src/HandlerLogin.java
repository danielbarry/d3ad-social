package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * HandlerLogin.java
 *
 * Generate a generic login page.
 **/
public class HandlerLogin extends Handler{
  private static byte[] form;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    form = (
      "<form action=\"" + sub + "login\" method=\"post\">" +
        "<label for=\"username\">username:</label>" +
        "<br>" +
        "<input type=\"text\" id=\"username\" name=\"username\">" +
        "<br>" +
        "<label for=\"password\">password:</label>" +
        "<br>" +
        "<input type=\"password\" id=\"password\" name=\"password\">" +
        "<br>" +
        "<input type=\"submit\" value=\"submit\">" +
        "<br>" +
        "<a href=\"" + sub + "register\">register</a>" +
      "</form>"
    ).getBytes();
  }

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
  public void genBody(OutputStream os) throws IOException{
    os.write(form);
  }
}
