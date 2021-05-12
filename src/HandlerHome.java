package b.ds;

import java.io.IOException;
import java.io.OutputStream;
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
  public void genBody(OutputStream os) throws IOException{
    Str res = new Str(256);
    res.append("<h2>latest posts</h2>");
    /* Generate post form */
    res = genPostForm(res, viewer, null);
    /* TODO: Show a more relevant page if user is logged in. */
    /* Grab latest list of posts */
    ArrayList<Post> posts = Post.getRecent();
    /* Begin loading posts from most recent (last) */
    for(int x = posts.size() - 1; x >= 0; x--){
      res = genPostEntry(res, posts.get(x), auth, viewer, 0);
    }
    os.write(res.toByteArray());
  }
}
