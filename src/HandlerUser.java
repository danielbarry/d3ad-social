package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

/**
 * HandlerUser.java
 *
 * Generate a generic user page.
 **/
public class HandlerUser extends Handler{
  private static int len;
  private static int maxWordLen;
  private static byte[] error;
  private static byte[] form;

  private Auth.User viewer;
  private Auth.User subject;
  private Auth auth;
  private String postId;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    /* Pull values from configuration */
    len = 16;
    try{
      len = Integer.parseInt(config.get("html").get("length").value(len + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find length value");
    }
    maxWordLen = 40;
    try{
      maxWordLen = Integer.parseInt(config.get("html").get("max-word-length").value(maxWordLen + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find max word length value");
    }
    /* Pre-generate known strings */
    error = "<b>Invalid user</b>".getBytes();
    form = (
        "<textarea" +
          " id=\"post\"" +
          " name=\"post\"" +
          " cols=\"64\"" +
          " rows=\"8\"" +
          /* TODO: Get length limit from configuration. */
          " maxlength=\"512\"" +
          " placeholder=\"What do you think? (max 512 characters)\"" +
        "></textarea>" +
        "<br>" +
        "<input type=\"submit\" value=\"submit\">" +
      "</form>"
    ).getBytes();
  }

  /**
   * HandlerUser()
   *
   * Initialise the variables required to deliver a user page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param subject The user being viewed, otherwise NULL.
   * @param auth Access to the authentication object.
   * @param postId The post to be displayed for this user.
   **/
  public HandlerUser(
    HashMap<String, String> kv,
    Auth.User viewer,
    Auth.User subject,
    Auth auth,
    String postId
  ){
    this.viewer = viewer;
    this.subject = subject;
    this.auth = auth;
    this.postId = postId;
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    /* Check if this is a valid page */
    if(subject != null){
      Str res = (new Str(2048))
        .append("<h2>")
        .append(subject.username)
        .append("'s posts <a href=\"")
        .append(sub)
        .append("rss/")
        .append(subject.id.toString())
        .append("\">RSS</a></h2>");
      /* Generate post form */
      res = genPostForm(res, viewer);
      /* Check for latest comment */
      if(subject.latest != null){
        Post post = Post.readPost(
          pstDir,
          postId != null ? postId : subject.latest.toString()
        );
        /* Make sure this is a valid post to display for this user */
        if(post == null || !post.user.id.equals(subject.id)){
          Utils.warn("Requested post under wrong user");
          os.write(error);
          return;
        }
        int postCount = 0;
        /* Begin loading posts */
        while(++postCount <= len && post != null){
          res = genPostEntry(res, post, auth);
          post = Post.readPost(pstDir, post.previous.toString());
        }
        /* Provide a link to find out more */
        if(post != null && post.previous != null){
          res
            .append("<h2><a href=\"")
            .append(sub)
            .append(USER_SUB)
            .append(subject.id.toString())
            .append("/")
            .append(post.id.toString())
            .append("\">more</a></h2>");
        }
      }
      os.write(res.toByteArray());
    }else{
      os.write(error);
    }
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
        .append("\" method=\"post\">");
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
   * @return The resulting string object.
   **/
  public static Str genPostEntry(Str res, Post post, Auth auth) throws IOException{
    res
      .append("<p><b><a href=\"")
      .append(sub)
      .append(USER_SUB)
      .append(post.user.id.toString())
      .append("\">@")
      .append(post.user.username)
      .append("</a></b> on ")
      .append((new Date(post.creation)).toString())
      .append(" said:<br><quote>");
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
