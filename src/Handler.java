package b.ds;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handler.java
 *
 * A basic interface for a specific page or function.
 **/
public abstract class Handler{
  public static final String USER_SUB = "user/";

  public static String title;
  public static String url;
  public static String sub;
  public static String pstDir;
  public static String usrDir;

  private static byte[] mime;
  private static byte[] head;
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
    pstDir = config.get("data").get("post-dir").value("dat/pst");
    usrDir = config.get("data").get("user-dir").value("dat/usr");
    /* Setup private variables */
    mime = "Content-Type: text/html; charset=utf-8".getBytes();
    String h =
      "<html>" +
        "<head>" +
          "<title>[" + title + "]</title>" +
          "<style>";
    for(int x = 0; x < config.get("html").get("css").length(); x++){
      h += config.get("html").get("css").get(x).value("");
    }
    h +=
          "</style>" +
        "</head>" +
        "<body>";
    head = h.getBytes();
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
   * @param os The mime type to be written.
   **/
  public byte[] genMime(){
    return mime;
  }

  /**
   * genHead()
   *
   * Generate the page header content.
   *
   * @param os The OutputStream to write the data to.
   * @param user The logged in user, otherwise NULL.
   **/
  public void genHead(OutputStream os, Auth.User user) throws IOException{
    os.write(head);
    os.write((new StringBuilder("<h1><a href=\""))
      .append(sub)
      .append("\">")
      .append(title)
      .append("</a> social ")
      .append("<a href=\"")
      .append(sub)
    .toString().getBytes());
    if(user == null){
      os.write("login\">login".getBytes());
    }else{
      os.write((new StringBuilder(USER_SUB))
        .append(user.id.toString())
        .append("\">@")
        .append(user.username)
      .toString().getBytes());
    }
    os.write("</a></h1>".getBytes());
  }

  /**
   * genBody()
   *
   * Process the requirements of the handler.
   *
   * @param os The OutputStream to write the data to.
   **/
  public void genBody(OutputStream os) throws IOException{
    os.write(error);
  }

  /**
   * genFoot()
   *
   * Generate the page footer content.
   *
   * @param os The OutputStream to write the data to.
   **/
  public void genFoot(OutputStream os) throws IOException{
    os.write(foot);
  }
}
