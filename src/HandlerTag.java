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
  private static int resLen;

  private Auth.User viewer;
  private Auth auth;
  private String tag;
  private int page;
  private boolean embed;

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
    /* Pull values from configuration */
    resLen = 16;
    try{
      resLen = Integer.parseInt(config.get("tag").get("result-length").value(resLen + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find tags result length value");
    }
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
   * @param page The page to be returned for the tag.
   **/
  public HandlerTag(
    HashMap<String, String> kv,
    Auth.User viewer,
    Auth auth,
    String tag,
    String page
  ){
    this.viewer = viewer;
    this.auth = auth;
    this.tag = tag;
    /* Check if tag is valid */
    if(kv.containsKey("search")){
      this.tag = kv.get("search");
    }
    this.tag = Tag.sanitize(this.tag);
    if(page != null){
      try{
        this.page = Integer.parseInt(page);
      }catch(NumberFormatException e){
        this.page = 0;
      }
    }
    /* Make sure page is non-zero */
    if(this.page < 0){
      this.page = 0;
    }
    /* Check if we got an embed request */
    embed = false;
    if(page != null){
      embed = page.equals("embed");
    }
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
    /* Skip writing the head if we got a request to embed */
    super.genHead(!embed ? os : null, user);
    /* Write the HTML header information */
    os.write(head);
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
      /* NOTE: We read an extra post to see if there are more. */
      ArrayList<Post> posts = Tag.readTag(tagDir, tag, page * resLen, resLen + 1);
      /* Render list of tags */
      for(int x = 0; x < Math.min(posts.size(), resLen); x++){
        Post post = posts.get(x);
        /* Make sure post is valid */
        if(post != null){
          res = genPostEntry(res, post, auth, viewer, 0);
        }
      }
      /* If we found more, display more */
      if(posts.size() > resLen){
        res
          .append("<h2><a href=\"")
          .append(sub)
          .append(TAG_SUB)
          .append(tag)
          .append("/")
          .append(Integer.toString(page + 1))
          .append("\">more</a></h2>");
      }
      os.write(res.toByteArray());
      return;
    }
    /* If we get here, then error */
    os.write(error);
  }
}
