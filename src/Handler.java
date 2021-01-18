package b.ds;

/**
 * Handler.java
 *
 * A basic interface for a specific page or function.
 **/
public abstract class Handler{
  /**
   * genHead()
   *
   * Generate the page header content.
   *
   * @param user The logged in user, otherwise NULL.
   * @return The bytes to be written to the client.
   **/
  public byte[] genHead(Auth.User user){
    return (
      "<html>" +
        "<head>" +
          /* TODO: Get title from configuration. */
          "<title>[d3ad]</title>" +
          "<style>" +
            "body{background-color:#111;color:#EEE;font-family:monospace;}" +
            "a{color:#FFF;}a:before{content:\"[\"}a:after{content:\"]\"}" +
            "p{background-color:#222;padding:8px;}" +
            "quote{background-color:#EEE;color:#222;width:100%;}" +
          "</style>" +
        "</head>" +
        "<body>" +
          /* TODO: Get title from configuration. */
          "<h1>" +
            "<a href=\"/\">d3ad</a> social " +
              "<a href=\"/" +
              (user == null ? "login\">login" : "user/" + user.id + "\">@" + user.username) +
            "</a>" +
          "</h1>"
    ).getBytes();
  }

  /**
   * genBody()
   *
   * Process the requirements of the handler.
   *
   * @return The bytes to be written to the client.
   **/
  public byte[] genBody(){
    return "<b>Error</b>".getBytes();
  }

  /**
   * genFoot()
   *
   * Generate the page footer content.
   *
   * @return The bytes to be written to the client.
   **/
  public byte[] genFoot(){
    return (
        "</body>" +
      "</html>"
    ).getBytes();
  }
}
