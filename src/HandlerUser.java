package b.ds;

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
    String res = "<b>" + subject.username + "'s posts</b>";
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
        String postId = subject.latest;
        int postCount = 0;
        /* TODO: Begin loading out posts. */
      }
    }else{
      return "<b>Invalid user</b>".getBytes();
    }
    return res.getBytes();
  }
}
