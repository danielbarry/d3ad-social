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
  private Auth.User viewer;
  private Auth auth;

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
   * @param viewer The logged in user, otherwise NULL.
   * @param auth Access to the authentication object.
   **/
  public HandlerHome(HashMap<String, String> kv, Auth.User viewer, Auth auth){
    this.viewer = viewer;
    this.auth = auth;
  }

  @Override
  public byte[] genBody(){
    StringBuilder res = new StringBuilder("<h2>latest posts</h2>");
    /* Generate post form */
    HandlerUser.genPostForm(res, viewer);
    /* TODO: Show a more relevant page if user is logged in. */
    /* Grab latest list of posts */
    ArrayList<Post> posts = Post.getRecent();
    /* Begin loading posts from most recent (last) */
    for(int x = posts.size() - 1; x >= 0; x--){
      res = HandlerUser.genPostEntry(res, posts.get(x), auth);
    }
    return res.toString().getBytes();
  }
}
