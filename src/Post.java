package b.ds;

/**
 * Post.java
 *
 * A thin wrapper for handling posts.
 **/
public class Post{
  /* Unique post ID */
  public String id = null;
  /* TODO: Just point to the user already in RAM. */
  /* The user ID that created the post */
  public String userid = null;
  /* The time the post was created */
  public long creation = -1;
  /* The previous post by the same user */
  public String previous = null;
  /* The escaped (safe) message string */
  public String message = null;

  /**
   * readPost()
   **/
  public static Post readPost(String path){
    return null; // TODO
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
      ",\"userid\":\""   + post.userid                  + "\"" +
      ",\"creation\":\"" + post.creation                + "\"" +
      (post.previous != null ? ",\"previous\":\"" + post.previous + "\"" : "") +
      ",\"message\":\""  + post.message                 + "\"" +
    "}";
    if(Data.write(path, data)){
      Utils.log("Post configuration saved " + post.id);
      return post;
    }else{
      return null;
    }
  }
}
