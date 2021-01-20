package b.ds;

import java.util.Date;
import java.util.HashMap;

/**
 * HandlerUser.java
 *
 * Generate a generic user page.
 **/
public class HandlerUser extends Handler{
  private static byte[] error;

  private Auth.User viewer;
  private Auth.User subject;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    error = "<b>Invalid user</b>".getBytes();
  }

  /**
   * HandlerUser()
   *
   * Initialise the variables required to deliver a user page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param subject The user being viewed, otherwise NULL.
   **/
  public HandlerUser(HashMap<String, String> kv, Auth.User viewer, Auth.User subject){
    this.viewer = viewer;
    this.subject = subject;
  }

  @Override
  public byte[] genBody(){
    StringBuilder res = (new StringBuilder())
      .append("<h2>").append(subject.username).append("'s posts ")
      .append("<a href=\"").append(sub).append("rss/").append(subject.id).append("\">RSS</a></h2>");
    if(viewer != null){
      res
        .append("<form action=\"").append(sub).append("user/").append(viewer.id).append("\" method=\"post\">")
        .append(  "<textarea")
        .append(    " id=\"post\"")
        .append(    " name=\"post\"")
        .append(    " cols=\"64\"")
        .append(    " rows=\"8\"")
        .append(    " maxlength=\"512\"")
        .append(    " placeholder=\"What do you think? (max 512 characters)\"")
        .append(  "></textarea>")
        .append(  "<br>")
        .append(  "<input type=\"submit\" value=\"submit\">")
        .append("</form>");
    }
    /* Check if this is a valid page */
    if(subject != null){
      /* Check for latest comment */
      if(subject.latest != null){
        /* TODO: Get path from configuration. */
        Post post = Post.readPost("dat/pst" + "/" + subject.latest, new Post());
        int postCount = 0;
        /* TODO: Get length from configuration. */
        /* Begin loading posts */
        while(++postCount <= 16 && post != null){
          res
            .append("<p>")
            .append(  "<b><a href=\"").append("user/").append(subject.id).append("\">@").append(subject.username)
            .append(  "</a></b> on ").append(new Date(post.creation)).append(" said:")
            .append(  "<br>")
            .append(  "<quote>").append(post.message).append("</quote>")
            .append("</p>");
          post = Post.readPost("dat/pst" + "/" + post.previous, new Post());
        }
        /* TODO: We should provide a link to find out more. */
      }
    }else{
      return error;
    }
    return res.toString().getBytes();
  }
}
