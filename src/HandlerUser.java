package b.ds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * HandlerUser.java
 *
 * Generate a generic user page.
 **/
public class HandlerUser extends Handler{
  private static byte[] error;
  private static int len;

  private Auth.User viewer;
  private Auth.User subject;
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
    error = "<div><b>Invalid user</b></div>".getBytes();
    len = 16;
    try{
      len = Integer.parseInt(config.get("html").get("length").value(len + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find length value");
    }
  }

  /**
   * HandlerUser()
   *
   * Initialise the variables required to deliver a user page.
   *
   * @param kv The key value data from the header.
   * @param viewer The logged in user, otherwise NULL.
   * @param subject The user being viewed, otherwise NULL.
   * @param auth Access to the authentication object.
   * @param postId The post to be displayed for this user.
   **/
  public HandlerUser(
    HashMap<String, String> kv,
    Auth.User viewer,
    Auth.User subject,
    Auth auth,
    String postId
  ){
    this.viewer = viewer;
    this.subject = subject;
    this.auth = auth;
    this.postId = postId;
  }

  @Override
  public void genBody(OutputStream os) throws IOException{
    /* Check if this is a valid page */
    if(subject != null){
      Str res = (new Str(2048))
        .append("<h2>")
        .append(subject.username)
        .append("'s posts <a href=\"")
        .append(sub)
        .append("rss/")
        .append(subject.id.toString())
        .append("\">RSS</a></h2>");
      /* Generate post form */
      res = genPostForm(res, viewer, null);
      /* Check for latest comment */
      if(subject.latest != null){
        Post post = null;
        if(postId != null){
          post = Post.readPost(pstDir, postId);
          if(post != null){
            /* As we were given a post, ensure it's valid for this user */
            if(!post.user.id.equals(subject.id)){
              /* Check if we have a quote */
              if(post.quote != null){
                Post qpost = Post.readPost(pstDir, post.quote.toString());
                if(qpost == null || !qpost.user.id.equals(subject.id)){
                  Utils.warn("Requested post under wrong user");
                  os.write(error);
                  return;
                }
              }
            }
          }else{
            Utils.warn("Requested post under wrong user");
            os.write(error);
            return;
          }
        }else if(subject.latest != null){
          post = Post.readPost(pstDir, subject.latest.toString());
        }
        int postCount = 0;
        /* Begin loading posts */
        while(++postCount <= len && post != null){
          res = genPostEntry(res, post, auth, viewer, 0);
          post = Handler.getNextPost(post, subject);
        }
        /* Provide a link to find out more */
        if(post != null && post.previous != null){
          res
            .append("<h2><a href=\"")
            .append(sub)
            .append(USER_SUB)
            .append(subject.id.toString())
            .append("/")
            .append(post.id.toString())
            .append("\">more</a></h2>");
        }
      }
      os.write(res.toByteArray());
    }else{
      os.write(error);
    }
  }
}
