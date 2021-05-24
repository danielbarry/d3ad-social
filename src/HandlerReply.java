package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * HandlerReply.java
 *
 * Generate a generic reply page.
 **/
public class HandlerReply extends Handler{
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
    error = "<div><b>Unknown post</b></div>".getBytes();
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
   * HandlerReply()
   *
   * Initialise the variables required to deliver a reply page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param auth Access to the authentication object.
   * @param postId The post to be displayed for this user.
   **/
  public HandlerReply(
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
    /* Check if user logged in */
    if(viewer == null){
      /* Returning the wrong error, but someone is acting malicious anyway */
      os.write(error);
      return;
    }
    /* Check if we have got passed a post ID */
    if(postId != null){
      Post post = Post.readPost(pstDir, postId);
      /* Ensure we have a valid post */
      if(post != null){
        Str res = new Str(256);
        res = genPostEntry(res, post, auth, viewer, 0);
        res = genPostForm(res, viewer, postId);
        os.write(res.toByteArray());
        return;
      }
    }
    /* If we get here, then error */
    os.write(error);
  }
}
