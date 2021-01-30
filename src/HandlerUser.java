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
      os.write((
        "<h2>" +
        subject.username +
        "'s posts <a href=\"" +
        sub +
        "rss/" +
        subject.id.toString() +
        "\">RSS</a></h2>"
      ).getBytes());
      /* Generate post form */
      genPostForm(os, viewer);
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
          genPostEntry(os, post, auth);
          post = Post.readPost(pstDir, post.previous.toString());
        }
        /* Provide a link to find out more */
        if(post != null && post.previous != null){
          os.write((
            "<h2><a href=\"" +
            sub +
            USER_SUB +
            subject.id.toString() +
            "/" +
            post.id.toString() +
            "\">more</a></h2>"
          ).getBytes());
        }
      }
    }else{
      os.write(error);
    }
  }

  /**
   * genPostForm()
   *
   * Generate a user post form to add messages if they are logged in.
   *
   * @param os The OutputStream to write the data to.
   * @param viewer The logged in viewer of the content, otherwise NULL.
   **/
  public static void genPostForm(OutputStream os, Auth.User viewer) throws IOException{
    if(viewer != null){
      os.write((
        "<form action=\"" +
        sub +
        USER_SUB +
        viewer.id.toString() +
        "\" method=\"post\">"
      ).getBytes());
      os.write(form);
    }
  }

  /**
   * genPostEntry()
   *
   * Generate a formatted post entry.
   *
   * @param os The OutputStream to write the data to.
   * @param post The post to be formatted.
   * @param auth Access to the authentication object.
   **/
  public static void genPostEntry(OutputStream os, Post post, Auth auth) throws IOException{
    os.write((
      "<p><b><a href=\"" +
      sub +
      USER_SUB +
      post.user.id.toString() +
      "\">@" +
      post.user.username
    ).getBytes());
    os.write((
      "</a></b> on " +
      (new Date(post.creation)).toString() +
      " said:<br><quote>"
    ).getBytes());
    HandlerUser.postProcessMessage(os, post.message, auth);
    os.write("</quote></p>".getBytes());
  }

  /**
   * postProcessMessage()
   *
   * Post-process the message String just before being served up to add social
   * elements.
   *
   * @param os The OutputStream to write the data to.
   * @param m The message to be formatted.
   * @param auth Access to the authentication object.
   **/
  public static void postProcessMessage(OutputStream os, String m, Auth auth) throws IOException{
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
              os.write((
                "<a href=\"" +
                sub +
                USER_SUB +
                user.id.toString() +
                "\">@" +
                user.username +
                "</a>"
              ).getBytes());
            }else{
              os.write(p[x].getBytes());
            }
            break;
          case '*' :
            if(b % 2 == 0){
              os.write("<b>".getBytes());
            }
            ++b;
            os.write(p[x].getBytes());
            break;
          case '[' :
            u = true;
            break;
          default :
            os.write(p[x].getBytes());
            break;
        }
        /* Process last character */
        switch(p[x].charAt(p[x].length() - 1)){
          case '*' :
            if(b % 2 == 1){
              os.write("</b>".getBytes());
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
                os.write((
                  "<a href=\"" +
                  uStr +
                  "\">" +
                  uName +
                  "</a>"
                ).getBytes());
              }catch(MalformedURLException e){
                os.write(p[x].getBytes());
              }
            }else{
              os.write(p[x].getBytes());
            }
            break;
          default :
            /* If we never completed the URL, print what we have */
            if(u){
              os.write(p[x].getBytes());
            }
            break;
        }
      }else{
        os.write(p[x].getBytes());
      }
      os.write(" ".getBytes());
    }
    /* Close off bold tag if not done */
    if(b % 2 == 1){
      os.write("</b>".getBytes());
    }
  }
}
