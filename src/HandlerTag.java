package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * HandlerTag.java
 *
 * Generate a generic tag search page.
 **/
public class HandlerTag extends Handler{
  private static byte[] error;

  private Auth.User viewer;
  private Auth auth;
  private String tag;

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
    error = "<div><b>Unknown request</b></div>".getBytes();
  }

  /**
   * HandlerTag()
   *
   * Initialise the variables required to deliver a tag page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param auth Access to the authentication object.
   * @param tag The tag to be searched for.
   **/
  public HandlerTag(
    HashMap<String, String> kv,
    Auth.User viewer,
    Auth auth,
    String tag
  ){
    this.viewer = viewer;
    this.auth = auth;
    this.tag = tag;
    /* TODO: Check if tag is valid. */
    if(kv.containsKey("search")){
      this.tag = kv.get("search");
    }
    this.tag = Tag.sanitize(this.tag);
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    /* Make sure tag is okay */
    if(tag != null){
      Str res = (new Str(2048))
        .append("<h2>tags labelled '")
        .append(tag)
        .append("'</h2>");
      /* Return list of tags */
      /* TODO: Pull values out of configuration. */
      ArrayList<Post> posts = Tag.readTag("dat/tag", tag, 0, 16);
      /* Render list of tags */
      for(int x = 0; x < posts.size(); x++){
        Post post = posts.get(x);
        /* Make sure post is valid */
        if(post != null){
          res = genPostEntry(res, post, auth, viewer, 0);
        }
      }
      os.write(res.toByteArray());
      return;
    }
    /* If we get here, then error */
    os.write(error);
  }
}
