package b.ds;

import java.util.Date;
import java.util.HashMap;

/**
 * HandlerRSS.java
 *
 * Generate a generic RSS reply.
 **/
public class HandlerRSS extends Handler{
  private static byte[] mime;
  private static byte[] head;
  private static byte[] foot;

  private Auth.User viewer;
  private Auth.User subject;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    mime = "Content-Type: application/xml".getBytes();
    head = (
      "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
      "<rss version=\"2.0\">" +
      "<channel>" +
        "<title>[" + title + "]</title>" +
        "<link>" + url + sub + "</link>" +
        /* TODO: Get this value from the configuration. */
        "<ttl>" + 30 + "</ttl>"
    ).getBytes();
    foot = (
      "</channel>" +
      "</rss>"
    ).getBytes();
  }

  /**
   * HandlerRSS()
   *
   * Initialise the variables required to deliver an RSS page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param subject The user being viewed, otherwise NULL.
   **/
  public HandlerRSS(HashMap<String, String> kv, Auth.User viewer, Auth.User subject){
    this.viewer = viewer;
    this.subject = subject;
  }

  @Override
  public byte[] genMime(){
    return mime;
  }

  @Override
  public byte[] genHead(Auth.User user){
    return head;
  }

  @Override
  public byte[] genBody(){
    StringBuilder res = new StringBuilder();
    /* Check if this is a valid page */
    if(subject != null){
      /* Check for latest comment */
      if(subject.latest != null){
        /* TODO: Get path from configuration. */
        Post post = Post.readPost("dat/pst", subject.latest);
        int postCount = 0;
        /* TODO: Get length from configuration. */
        /* Begin loading posts */
        while(++postCount <= 8 && post != null){
          res
            .append("<item>")
            .append(  "<title>").append(subject.username).append("</title>")
            .append(  "<link>").append(url).append(sub).append("user/").append(subject.id).append("</link>")
            .append(  "<description>").append(post.message).append("</description>")
            .append("</item>");
          post = Post.readPost("dat/pst", post.previous);
        }
      }
    }
    return res.toString().getBytes();
  }

  @Override
  public byte[] genFoot(){
    return foot;
  }
}
