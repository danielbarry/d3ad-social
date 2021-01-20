package b.ds;

/**
 * Handler.java
 *
 * A basic interface for a specific page or function.
 **/
public abstract class Handler{
  public static String title;
  public static String url;
  public static String sub;

  private static byte[] mime;
  private static String head;
  private static byte[] error;
  private static byte[] foot;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    /* Setup public variables */
    title = config.get("title").value("d3ad");
    url = config.get("url").value("127.0.0.1");
    sub = config.get("sub-dir").value("/");
    /* Setup private variables */
    mime = "Content-Type: text/html; charset=utf-8".getBytes();
    head =
      "<html>" +
        "<head>" +
          "<title>[" + title + "]</title>" +
          /* TODO: Get CSS from configuration. */
          "<style>";
    for(int x = 0; x < config.get("html").get("css").length(); x++){
      head += config.get("html").get("css").get(x).value("");
    }
    head +=
          "</style>" +
        "</head>" +
        "<body>";
    error = "<b>Error</b>".getBytes();
    foot = (
        "</body>" +
      "</html>"
    ).getBytes();
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
    return ((new StringBuilder(head))
      .append("<h1>")
      .append(  "<a href=\"").append(sub).append("\">").append(title).append("</a> social ")
      .append(    "<a href=\"").append(sub)
      .append(    (user == null ? "login\">login" : "user/" + user.id + "\">@" + user.username))
      .append(  "</a>")
      .append("</h1>")
    ).toString().getBytes();
  }

  /**
   * genBody()
   *
   * Process the requirements of the handler.
   *
   * @return The bytes to be written to the client.
   **/
  public byte[] genBody(){
    return error;
  }

  /**
   * genFoot()
   *
   * Generate the page footer content.
   *
   * @return The bytes to be written to the client.
   **/
  public byte[] genFoot(){
    return foot;
  }
}
