package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * HandlerRegister.java
 *
 * Generate a generic registration page.
 **/
public class HandlerRegister extends Handler{
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
      "<form action=\"" + sub + "register\" method=\"post\">" +
        "<b>Warning:</b> The security was hacked together, please use random credentials." +
        "<br>" +
        "<br>" +
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
        "<br>" +
        "<br>" +
        "By registering, you agree to the site rules:" +
        "<br>" +
        "1. Don't contribute content you wouldn't show your mother." +
        "<br>" +
        "2. Don't abuse the service - if in doubt, ask." +
      "</form>"
    ).getBytes();
  }

  /**
   * HandlerRegister()
   *
   * Initialise the variables required to deliver a registration page.
   *
   * @param kv The key value data from the header.
   **/
  public HandlerRegister(HashMap<String, String> kv){
    /* Do nothing */
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    os.write(form);
  }
}
