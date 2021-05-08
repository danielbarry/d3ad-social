package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Handler.java
 *
 * A basic interface for a specific page or function.
 **/
public abstract class Handler{
  public static final String USER_SUB = "user/";
  public static final String EMBED_SUB = "embed/";
  public static final String HIDE_SUB = "hide/";

  public static String title;
  public static String url;
  public static String sub;
  public static String pstDir;
  public static String usrDir;
  public static byte[] mime;
  public static byte[] head;
  public static byte[] error;
  public static byte[] foot;

  private static int inputMaxLen;
  private static int maxWordLen;
  private static int embedWidth;
  private static int embedHeight;
  private static String form;

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
          "<meta name=\"robots\" content=\"index\" />" +
        "</head>" +
        "<body>";
    head = h.getBytes();
    error = "<b>Error</b>".getBytes();
    foot = (
        "</body>" +
      "</html>"
    ).getBytes();
    inputMaxLen = 512;
    try{
      inputMaxLen = Integer.parseInt(config.get("input").get("max-length").value(inputMaxLen + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find max input length value");
    }
    maxWordLen = 40;
    try{
      maxWordLen = Integer.parseInt(config.get("html").get("max-word-length").value(maxWordLen + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find max word length value");
    }
    embedWidth = 680;
    try{
      embedWidth = Integer.parseInt(config.get("embed").get("width").value(embedWidth + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find embed width value");
    }
    embedHeight = 200;
    try{
      embedHeight = Integer.parseInt(config.get("embed").get("height").value(embedHeight + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find embed height value");
    }
    form = (
        "<textarea" +
          " id=\"post\"" +
          " name=\"post\"" +
          " cols=\"64\"" +
          " rows=\"8\"" +
          " maxlength=\"" + inputMaxLen + "\"" +
          " placeholder=\"What do you think? (max " + inputMaxLen + " characters)\"" +
        "></textarea>" +
        "<br>" +
        "<input type=\"submit\" value=\"submit\">" +
      "</form>"
    );
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
    Str res = new Str(32);
    os.write(head);
    res
      .append("<h1><a href=\"")
      .append(sub)
      .append("\">")
      .append(title)
      .append("</a> social ")
      .append("<a href=\"")
      .append(sub);
    if(user == null){
      res.append("login\">login");
    }else{
      res
        .append(USER_SUB)
        .append(user.id.toString())
        .append("\">@")
        .append(user.username);
      /* If an admin, add a link the admin panel */
      if(user.role == Auth.Role.ADMIN){
        res
          .append("</a> <a href=\"")
          .append(sub)
          .append("about\">*");
      }
    }
    res.append("</a></h1>");
    os.write(res.toByteArray());
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

  /**
   * genPostForm()
   *
   * Generate a user post form to add messages if they are logged in.
   *
   * @param res The string building structure for this object.
   * @param viewer The logged in viewer of the content, otherwise NULL.
   * @return The resulting string object.
   **/
  public static Str genPostForm(Str res, Auth.User viewer) throws IOException{
    if(viewer != null){
      res
        .append("<form action=\"")
        .append(sub)
        .append(USER_SUB)
        .append(viewer.id.toString())
        .append("\" method=\"post\">")
        .append(form);
    }
    return res;
  }

  /**
   * genPostEntry()
   *
   * Generate a formatted post entry.
   *
   * @param res The string building structure for this object.
   * @param post The post to be formatted.
   * @param auth Access to the authentication object.
   * @param viewer The viewer of the object, otherwise NULL.
   * @return The resulting string object.
   **/
  public static Str genPostEntry(Str res, Post post, Auth auth, Auth.User viewer) throws IOException{
    res
      .append("<p><b><a target=\"_blank\" href=\"")
      .append(sub)
      .append(USER_SUB)
      .append(post.user.id.toString())
      .append("\">@")
      .append(post.user.username)
      .append("</a></b> on ")
      .append((new Date(post.creation)).toString())
      .append(" said: <a href=\"")
      .append(sub)
      .append(EMBED_SUB)
      .append(post.id.toString())
      .append("\">&amp;</a> <a target=\"_blank\" href=\"data:text/html,<embed width='")
      .append(Integer.toString(embedWidth))
      .append("' height='")
      .append(Integer.toString(embedHeight))
      .append("' src='")
      .append(url)
      .append(sub)
      .append(EMBED_SUB)
      .append(post.id.toString())
      .append("'></embed>\">embed</a>");
    /* Is this the post owner or admin? */
    if(
      viewer != null && (
        viewer.id.equals(post.user.id) ||
        viewer.role == Auth.Role.ADMIN
    )){
      /* Add option to delete post */
      res
        .append(" <a href=\"")
        .append(url)
        .append(sub)
        .append(HIDE_SUB)
        .append(post.id.toString())
        .append("\">x</a>");
    }
    res.append("<br><quote>");
    res = HandlerUser.postProcessMessage(res, post.message, auth);
    res.append("</quote></p>");
    return res;
  }

  /**
   * postProcessMessage()
   *
   * Post-process the message String just before being served up to add social
   * elements.
   *
   * @param res The string building structure for this object.
   * @param m The message to be formatted.
   * @param auth Access to the authentication object.
   * @return The resulting string object.
   **/
  public static Str postProcessMessage(Str res, String m, Auth auth) throws IOException{
    String[] p = m.split("\\s+");
    /* Process each part */
    int b = 0;
    for(int x = 0; x < p.length; x++){
      /* Make sure it's long enough to be processed */
      if(p[x].length() > 2){
        boolean u = false;
        /* Process first character */
        switch(p[x].charAt(0)){
          case '@' :
            Auth.User user = auth.getUserByName(p[x].substring(1));
            if(user != null){
              res
                .append("<a href=\"")
                .append(sub)
                .append(USER_SUB)
                .append(user.id.toString())
                .append("\">@")
                .append(user.username)
                .append("</a>");
            }else{
              res.append(p[x]);
            }
            break;
          case '*' :
            if(b % 2 == 0){
              res.append("<b>");
            }
            ++b;
            res.append(p[x]);
            break;
          case '[' :
            u = true;
            break;
          default :
            res.append(p[x]);
            break;
        }
        /* Process last character */
        switch(p[x].charAt(p[x].length() - 1)){
          case '*' :
            if(b % 2 == 1){
              res.append("</b>");
            }
            ++b;
            break;
          case ']' :
            if(u){
              try{
                String uStr = p[x]
                  .substring(1, p[x].length() - 1)
                  .replaceAll("&amp;", "&");
                URL url = new URL(uStr);
                String uName = uStr;
                if(uName.length() > maxWordLen){
                  uName = uName.substring(0, maxWordLen);
                  uName += "..";
                }
                uName.replaceAll("&", "&amp;");
                res
                  .append("<a href=\"")
                  .append(uStr)
                  .append("\">")
                  .append(uName)
                  .append("</a>");
              }catch(MalformedURLException e){
                res.append(p[x]);
              }
            }else{
              res.append(p[x]);
            }
            break;
          default :
            /* If we never completed the URL, print what we have */
            if(u){
              res.append(p[x]);
            }
            break;
        }
      }else{
        res.append(p[x]);
      }
      res.append(" ");
    }
    /* Close off bold tag if not done */
    if(b % 2 == 1){
      res.append("</b>");
    }
    return res;
  }
}
