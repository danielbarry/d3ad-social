package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * HandlerEmbed.java
 *
 * Generate a generic embedded page.
 **/
public class HandlerEmbed extends Handler{
  private static byte[] error;

  private Auth.User viewer;
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
    /* Pre-generate known strings */
    error = "<b>Unknown post</b>".getBytes();
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
    os.write(head);
  }

  /**
   * HandlerEmbed()
   *
   * Initialise the variables required to deliver a embed page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param auth Access to the authentication object.
   * @param postId The post to be displayed for this user.
   **/
  public HandlerEmbed(
    HashMap<String, String> kv,
    Auth.User viewer,
    Auth auth,
    String postId
  ){
    this.viewer = viewer;
    this.auth = auth;
    this.postId = postId;
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    /* Check if we have got passed a post ID */
    if(postId != null){
      Post post = Post.readPost(pstDir, postId);
      /* Ensure we have a valid post */
      if(post != null){
        os.write(genPostEntry(new Str(256), post, auth, viewer, 0).toByteArray());
        return;
      }
    }
    /* If we get here, then error */
    os.write(error);
  }
}
