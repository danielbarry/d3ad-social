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
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    /* Do nothing */
  }

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
    StringBuilder res = new StringBuilder("<h2>Latest posts</h2>");
    /* TODO: Show a more relevant page if user is logged in. */
    /* Grab latest list of posts */
    ArrayList<Post> posts = Post.getPosts();
    /* Begin loading posts from most recent (last) */
    for(int x = posts.size() - 1; x >= 0; x--){
      Post post = posts.get(x);
      Auth.User user = post.user;
      res
        .append("<p>")
        .append(  "<b><a href=\"").append(sub).append("user/").append(user.id).append("\">@").append(user.username)
        .append(  "</a></b> on ").append(new Date(post.creation)).append(" said:")
        .append(  "<br>")
        .append(  "<quote>").append(post.message).append("</quote>")
        .append("</p>");
    }
    return res.toString().getBytes();
  }
}
