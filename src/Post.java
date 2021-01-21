package b.ds;

import java.util.ArrayList;

/**
 * Post.java
 *
 * A thin wrapper for handling posts.
 **/
public class Post{
  private static Auth auth;
  private static ArrayList<Post> posts;

  /* Unique post ID */
  public String id = null;
  /* The user that created the post */
  public Auth.User user = null;
  /* The time the post was created */
  public long creation = -1;
  /* The previous post by the same user */
  public String previous = null;
  /* The escaped (safe) message string */
  public String message = null;

  /**
   * init()
   *
   * Share static variables and initialize posts list.
   *
   * @param auth Give posts access to the users lists.
   **/
  public static void init(Auth auth){
    Post.auth = auth;
    posts = new ArrayList<Post>();
  }

  /**
   * readPost()
   *
   * Read user post from disk and update the relevant variables. Return NULL if
   * an issue occurs.
   *
   * @param path The path for the user configuration.
   * @param post A post object to be written to.
   * @return The post object, otherwise NULL.
   **/
  public static Post readPost(String path, Post post){
    try{
      JSON postData = JSON.build(path);
      post.id = postData.get("id").value(null);
      post.user = auth.getUserById(postData.get("userid").value(null));
      try{
        post.creation = Long.parseLong(postData.get("creation").value(".."));
      }catch(NumberFormatException e){
        post.creation = -1;
      }
      post.previous = postData.get("previous").value(null);
      post.message = postData.get("message").value(null);
      if(
        post.id != null      &&
        post.user != null    &&
        post.creation >= 0   &&
        post.message != null
      ){
        return post;
      }else{
        return null;
      }
    }catch(Exception e){
      return null;
    }
  }

  /**
   * writePost()
   *
   * Write the post data to disk and update the relevant variables. Return NULL
   * if an issue occurs.
   *
   * @param path The path for the user configuration.
   * @param post The post object to be written.
   * @return The post object, otherwise NULL.
   **/
  public static Post writePost(String path, Post post){
    /* TODO: Validation should be done here. */
    /* Save the post to disk */
    String data = "{" +
      "\"id\":\""        + post.id                      + "\"" +
      ",\"userid\":\""   + post.user.id                 + "\"" +
      ",\"creation\":\"" + post.creation                + "\"" +
      (post.previous != null ? ",\"previous\":\"" + post.previous + "\"" : "") +
      ",\"message\":\""  + post.message                 + "\"" +
    "}";
    if(Data.write(path, data)){
      Utils.log("Post configuration saved " + post.id);
      /* TODO: Get length of post buffer from configuration. */
      /* Add to posts buffer */
      if(posts.size() >= 16){
        posts.remove(0);
      }
      posts.add(post);
      return post;
    }else{
      return null;
    }
  }

  /**
   * getPosts()
   *
   * Get a list of the latest buffer.
   *
   * @return A clone of the latest buffer of posts.
   **/
  public static ArrayList<Post> getPosts(){
    return (ArrayList<Post>)(posts.clone());
  }
}
