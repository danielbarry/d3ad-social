package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;

/**
 * HandlerRSS.java
 *
 * Generate a generic RSS reply.
 **/
public class HandlerRSS extends Handler{
  private static int len;
  private static int ttl;
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
    /* Pull values from configuration */
    len = 8;
    try{
      len = Integer.parseInt(config.get("rss").get("length").value(len + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find length value");
    }
    ttl = 30;
    try{
      ttl = Integer.parseInt(config.get("rss").get("ttl-mins").value(ttl + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find ttl value");
    }
    /* Pre-generate known strings */
    mime = "Content-Type: application/xml".getBytes();
    head = (
      "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
      "<rss version=\"2.0\">" +
      "<channel>" +
        "<title>[" + title + "]</title>" +
        "<link>" + url + sub + "</link>" +
        "<ttl>" + ttl + "</ttl>"
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
  public void genHead(OutputStream os, Auth.User user) throws IOException{
    os.write(head);
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    /* Check if this is a valid page */
    if(subject != null){
      /* Check for latest comment */
      if(subject.latest != null){
        Post post = Post.readPost(
          pstDir,
          subject.latest != null ? subject.latest.toString() : null
        );
        int postCount = 0;
        /* Begin loading posts */
        Str res = new Str(256);
        while(++postCount <= len && post != null){
          res
            .append("<item>")
              .append("<title>")
                .append(post.user.username)
              .append("</title>")
              .append("<link>")
                .append(url)
                .append(sub)
                .append(EMBED_SUB)
                .append(post.id.toString())
              .append("</link>")
              .append("<description>");
              if(post.state != Post.State.HIDE){
                res.append(post.message);
              }else{
                res.append("Message deleted");
              }
            res
              .append("</description>")
            .append("</item>");
            post = Handler.getNextPost(post, subject);
        }
        os.write(res.toByteArray());
      }
    }
  }

  @Override
  public void genFoot(OutputStream os) throws IOException{
    os.write(foot);
  }
}
