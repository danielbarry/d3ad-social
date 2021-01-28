package b.ds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Post.java
 *
 * A thin wrapper for handling posts.
 **/
public class Post{
  private static Auth auth;
  private static HashMap<I512, Post> idMap;
  private static ArrayList<Post> recent;

  /* Unique post ID */
  public I512 id = null;
  /* The user that created the post */
  public Auth.User user = null;
  /* The time the post was created */
  public long creation = -1;
  /* The previous post by the same user */
  public I512 previous = null;
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
    idMap = new HashMap<I512, Post>();
    recent = new ArrayList<Post>();
  }

  /**
   * readPost()
   *
   * Read user post from disk and update the relevant variables. Return NULL if
   * an issue occurs.
   *
   * @param loc The location of the post.
   * @param id The ID for the post configuration.
   * @return The post object, otherwise NULL.
   **/
  public static Post readPost(String loc, String id){
    I512 i = new I512(id);
    /* Try to load from cache */
    Post post = idMap.get(i);
    if(post != null && post.id.equals(i)){
      return post;
    }
    /* Load from disk */
    try{
      JSON postData = JSON.build(loc + "/" + id);
      post = new Post();
      post.id = new I512(postData.get("id").value(null));
      post.user = auth.getUserById(postData.get("userid").value(null));
      try{
        post.creation = Long.parseLong(postData.get("creation").value(".."));
      }catch(NumberFormatException e){
        post.creation = -1;
      }
      post.previous = new I512(postData.get("previous").value(null));
      post.message = postData.get("message").value(null);
      if(
        post.id != null    &&
        post.user != null  &&
        post.creation >= 0 &&
        post.message != null
      ){
        addPost(post);
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
   * @param loc The location of the post.
   * @param id The id for the post configuration.
   * @param post The post object to be written.
   * @return The post object, otherwise NULL.
   **/
  public static Post writePost(String loc, String id, Post post){
    /* Save the post to disk */
    JSON data = null;
    try{
      data = new JSON(false);
      data.set(new JSON("id", post.id.toString()));
      data.set(new JSON("userid", post.user.id.toString()));
      data.set(new JSON("creation", Long.toString(post.creation)));
      if(post.previous != null){
        data.set(new JSON("previous", post.previous.toString()));
      }
      data.set(new JSON("message", post.message));
    }catch(Exception e){
      data = null;
    }
    if(data != null && Data.write(loc + "/" + id, data.toString())){
      Utils.log("Post configuration saved " + post.id);
      /* TODO: Get length of post buffer from configuration. */
      /* Add to posts buffer */
      if(recent.size() >= 16){
        recent.remove(0);
      }
      recent.add(post);
      addPost(post);
      return post;
    }else{
      return null;
    }
  }

  /**
   * addPost()
   *
   * Add a post to the cache if it is not already added.
   *
   * @param post The post to be added.
   **/
  private static void addPost(Post post){
    /* Check the post object is already in cache */
    Post temp = idMap.get(post.id);
    if(temp != null && temp == post){
      return;
    }
    /* Make sure cache remains of reasonable size */
    /* NOTE: We assume 1 post = 1kB, so 1 millions posts = 1GB */
    /* TODO: Derive max cache from configuration. */
    if(idMap.size() >= 1000000){
      Object[] keys = idMap.keySet().toArray();
      Utils.log("Need reduce cache, currently is " + keys.length);
      /* Iterate through keys (this is expensive, do it rarely) */
      /* TODO: Derive remove percentage from configuration. */
      int removeNum = keys.length / 4;
      int startPos = removeNum * (new Random(System.currentTimeMillis())).nextInt(4);
      int endPos = startPos + removeNum;
      endPos = endPos < keys.length ? endPos : keys.length;
      for(int x = startPos; x < endPos; x++){
        idMap.remove(keys[x]);
      }
      Utils.log("Cache reduced to " + idMap.size());
    }
    /* Update cache */
    idMap.put(post.id, post);
  }

  /**
   * getRecent()
   *
   * Get a list of the latest list of posts.
   *
   * @return A clone of the latest list of posts.
   **/
  public static ArrayList<Post> getRecent(){
    return (ArrayList<Post>)(recent.clone());
  }
}
