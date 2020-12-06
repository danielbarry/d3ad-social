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
      post.userid = postData.get("userid").value(null);
      try{
        post.creation = Long.parseLong(postData.get("creation").value(".."));
      }catch(NumberFormatException e){
        post.creation = -1;
      }
      post.previous = postData.get("previous").value(null);
      post.message = postData.get("message").value(null);
      if(
        post.id != null       &&
        post.userid != null   &&
        post.creation >= 0    &&
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
