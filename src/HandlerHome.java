package b.ds;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * HandlerHome.java
 *
 * Generate a generic home page.
 **/
public class HandlerHome extends Handler{
  /**
   * HandlerHome()
   *
   * Initialise the variables required to deliver a home page.
   *
   * @param kv The key value data from the header.
   **/
  public HandlerHome(HashMap<String, String> kv){
    /* Do nothing */
  }

  @Override
  public byte[] genBody(){
    String res = "<h2>Latest posts</h2>";
    /* TODO: Show a more relevant page if user is logged in. */
    /* Grab latest list of posts */
    ArrayList<Post> posts = Post.getPosts();
    /* Begin loading posts from most recent (last) */
    for(int x = posts.size() - 1; x >= 0; x--){
      Post post = posts.get(x);
      Auth.User user = post.user;
      res +=
        "<p>" +
          "<b><a href=\"/user/" + user.id + "\">@" + user.username +
          "</a></b> on " + (new Date(post.creation)) + " said:" +
          "<br>" +
          "<quote>" + post.message + "</quote>" +
        "</p>";
    }
    return res.getBytes();
  }
}
