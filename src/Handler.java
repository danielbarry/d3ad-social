package b.ds;

/**
 * Handler.java
 *
 * A basic interface for a specific page or function.
 **/
public abstract class Handler{
  private static byte[] mime;
  private static String title;
  private static String head;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    mime = "Content-Type: text/html; charset=utf-8".getBytes();
    title = config.get("title").value("d3ad");
    head =
      "<html>" +
        "<head>" +
          "<title>[" + title + "]</title>" +
          /* TODO: Get CSS from configuration. */
          "<style>";
    for(int x = 0; x < config.get("css").length(); x++){
      head += config.get("css").get(x).value("");
    }
    head +=
          "</style>" +
        "</head>" +
        "<body>";
  }

  /**
   * genMine()
   *
   * Allow the mime return type to be overwritten.
   *
   * @return The mine return string.
   **/
  public byte[] genMime(){
    return mime;
  }

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
      head +
      "<h1>" +
        "<a href=\"/\">" + title + "</a> social " +
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
