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
  public static final String REPLY_SUB = "reply/";
  public static final String HIDE_SUB = "hide/";
  public static final String TAG_SUB = "tag/";

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
  private static String search;

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
    search =
      "<form action=\"" + sub + TAG_SUB + "\" method=\"post\">" +
        "<input type=\"text\" id=\"search\" name=\"search\" value=\"search tags\">" +
        "<input type=\"submit\" value=\"submit\">" +
      "</form>";
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
    res
      .append("</a>")
      .append(search)
      .append("</h1>");
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
   * @param quoteId The post being quoted, otherwise NULL.
   * @return The resulting string object.
   **/
  public static Str genPostForm(Str res, Auth.User viewer, String quoteId) throws IOException{
    if(viewer != null){
      res
        .append("<form action=\"")
        .append(sub)
        .append(USER_SUB)
        .append(viewer.id.toString())
        .append("\" method=\"post\">");
      /* Check if we want to quote a post */
      if(quoteId != null){
        res
          .append("<input type=\"hidden\" id=\"quote\" name=\"quote\" value=\"")
          .append(quoteId)
          .append("\">");
      }
      res.append(form);
    }
    return res;
  }

  /**
   * getNextPost()
   *
   * Given a post, get the next post for the user. This method considers also
   * the quotes for a given user and includes them in the correct context.
   *
   * @param post The post to be incremented upon.
   * @param user The user to find the next post for.
   * @return The next post, otherwise NULL.
   **/
  public static Post getNextPost(Post post, Auth.User user){
    Post next = null;
    /* Check if there is anything to be read */
    if(post.previous != null){
      /* Check if the current post is a quote */
      if(post.user == user){
        next = Post.readPost(pstDir, post.previous.toString());
      }else{
        /* Make sure there is a quote */
        if(post.qprevious != null){
          next = Post.readPost(pstDir, post.qprevious.toString());
        }
      }
    }
    /* Return what we have */
    return next;
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
   * @param entry This counts the number of times this method is entered into,
   * the default being zero. The purpose it prevent endless recursion.
   * @return The resulting string object.
   **/
  public static Str genPostEntry(Str res, Post post, Auth auth, Auth.User viewer, int entry) throws IOException{
    /* Make sure we're not in too deep */
    if(entry > 1){
      return res;
    }
    res
      .append("<div><b><a target=\"_blank\" href=\"")
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
      .append("\">&amp;</a><a target=\"_blank\" href=\"data:text/html,<embed width='")
      .append(Integer.toString(embedWidth))
      .append("' height='")
      .append(Integer.toString(embedHeight))
      .append("' src='")
      .append(url)
      .append(sub)
      .append(EMBED_SUB)
      .append(post.id.toString())
      .append("'></embed>\">e</a>");
    /* Is this a logged in user? */
    if(viewer != null){
      /* Offer the option to quote reply */
      res
        .append("<a href=\"")
        .append(sub)
        .append(REPLY_SUB)
        .append(post.id.toString())
        .append("\">&gt;</a>");
      /* Is this the post owner or admin? */
      if(
        viewer != null && (
          viewer.id.equals(post.user.id) ||
          viewer.role == Auth.Role.ADMIN
      )){
        /* Add option to delete post */
        res
          .append("<a href=\"")
          .append(sub)
          .append(HIDE_SUB)
          .append(post.id.toString())
          .append("\">");
        switch(post.state){
          case NONE :
            res.append("x");
            break;
          case HIDE :
            res.append("+");
            break;
          default :
            /* Display at least something for an unknown state */
            res.append("?");
            break;
        }
        res.append("</a>");
      }
    }
    res.append("<br><quote>");
    /* Check if message deleted */
    if(post.state != Post.State.HIDE){
      /* Are we quoting somebody? */
      if(post.quote != null){
        Post quote = Post.readPost(pstDir, post.quote.toString());
        if(quote != null){
          res = genPostEntry(res, quote, auth, viewer, entry + 1);
        }
      }
      res = HandlerUser.postProcessMessage(res, post.message, auth);
    }else{
      res.append("<i>Message deleted</i>");
    }
    res.append("</quote></div>");
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
