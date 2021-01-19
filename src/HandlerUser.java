package b.ds;

import java.util.Date;
import java.util.HashMap;

/**
 * HandlerUser.java
 *
 * Generate a generic user page.
 **/
public class HandlerUser extends Handler{
  private Auth.User viewer;
  private Auth.User subject;

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
    /* TODO: Switch to StringBuilder object. */
    String res =
      "<h2>" + subject.username + "'s posts " +
      "<a href=\"/rss/" + subject.id + "\">RSS</a></h2>";
    if(viewer != null){
      res +=
        "<form action=\"/user/" + viewer.id + "\" method=\"post\">" +
          "<textarea" +
            " id=\"post\"" +
            " name=\"post\"" +
            " cols=\"64\"" +
            " rows=\"8\"" +
            " maxlength=\"512\"" +
            " placeholder=\"What do you think? (max 512 characters)\"" +
          "></textarea>" +
          "<br>" +
          "<input type=\"submit\" value=\"submit\">" +
        "</form>";
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
          res +=
            "<p>" +
              "<b><a href=\"/user/" + subject.id + "\">@" + subject.username +
              "</a></b> on " + (new Date(post.creation)) + " said:" +
              "<br>" +
              "<quote>" + post.message + "</quote>" +
            "</p>";
          post = Post.readPost("dat/pst" + "/" + post.previous, new Post());
        }
        /* TODO: We should provide a link to find out more. */
      }
    }else{
      return "<b>Invalid user</b>".getBytes();
    }
    return res.getBytes();
  }
}
