package b.ds;

import java.util.Date;
import java.util.HashMap;

/**
 * HandlerRSS.java
 *
 * Generate a generic RSS reply.
 **/
public class HandlerRSS extends Handler{
  private Auth.User viewer;
  private Auth.User subject;

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
    return "Content-Type: application/xml".getBytes();
  }

  @Override
  public byte[] genHead(Auth.User user){
    return (
      "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
      "<rss version=\"2.0\">" +
      "<channel>" +
        /* TODO: Generate from configuration. */
        "<title>[d3ad]</title>" +
        "<link>127.0.0.1:8080</link>"
    ).getBytes();
  }

  @Override
  public byte[] genBody(){
    /* TODO: Switch to StringBuilder object. */
    String res = "";
    /* Check if this is a valid page */
    if(subject != null){
      /* Check for latest comment */
      if(subject.latest != null){
        /* TODO: Get path from configuration. */
        Post post = Post.readPost("dat/pst" + "/" + subject.latest, new Post());
        int postCount = 0;
        /* TODO: Get length from configuration. */
        /* Begin loading posts */
        while(++postCount <= 8 && post != null){
          res +=
            "<item>" +
              "<title>" + subject.username + "</title>" +
              /* TODO: Get link from configuration. */
              "<link>127.0.0.1:8080/user/" + subject.id + "</link>" +
              "<description>" + post.message + "</description>" +
            "</item>";
          post = Post.readPost("dat/pst" + "/" + post.previous, new Post());
        }
        /* TODO: We should provide a link to find out more. */
      }
    }
    return res.getBytes();
  }

  @Override
  public byte[] genFoot(){
    return (
      "</channel>" +
      "</rss>"
    ).getBytes();
  }
}
