package b.ds;

import java.util.ArrayList;

/**
 * Tag.java
 *
 * A thin wrapper for handling tags.
 **/
public class Tag{
  private static final int ENTRY_LEN = ((new I512("0")).toString() + "\n").length();

  /**
   * readTag()
   *
   * Read the information for a given tag.
   *
   * @param loc The location of the post.
   * @param tag The tag to be searched for.
   * @param offset The offset to start reading the tags from.
   * @param length The number of tags to be read from the offset.
   * @return A list of found tags.
   **/
  public static ArrayList<Post> readTag(String loc, String tag, int offset, int length){
    ArrayList<Post> results = new ArrayList<Post>();
    /* Make sure we were given good values */
    if(loc == null || tag == null || offset < 0 || length < 0){
      return null;
    }
    /* Sanitize the tag */
    tag = sanitize(tag);
    if(tag == null){
      return null;
    }
    /* Read the results */
    /* TODO: Read variables from configuration. */
    String[] entries = Data.readRearLines(loc + "/" + tag, ENTRY_LEN, 0, 16);
    if(entries != null){
      /* Try to convert each post */
      for(int x = 0; x < entries.length; x++){
        /* TODO: Get post location from somewhere. */
        Post post = Post.readPost("dat/pst", entries[x].trim());
        if(post != null){
          results.add(post);
        }
      }
    }
    return results;
  }

  /**
   * writeTag()
   *
   * Write information for a given tag.
   *
   * @param loc The location of the post.
   * @param tag The tag to be searched for.
   * @param post The post to be associated with the tag.
   * @param The post that was written, otherwise NULL.
   **/
  public static Post writeTag(String loc, String tag, Post post){
    /* Make sure we were given good values */
    if(loc == null || tag == null || post == null){
      return null;
    }
    /* Sanitize the tag */
    tag = sanitize(tag);
    if(tag == null){
      return null;
    }
    /* Write the tag */
    Data.append(loc + "/" +  tag, post.id.toString() + "\n");
    return post;
  }

  /**
   * sanitize()
   *
   * Sanitize a given tag for the purpose of searching. The returned string
   * should be valid for searching.
   *
   * @param s The string to be sanitized.
   * @return The sanitized string, otherwise NULL.
   **/
  public static String sanitize(String s){
    /* Make sure we were given something and of correct length */
    /* TODO: Pull length out of configuration. */
    if(s == null || s.length() < 2 || s.length() > 64){
      return null;
    }
    /* Setup array to be cloned into */
    char[] t = s.toCharArray();
    /* Loop over each character */
    for(int x = 0; x < t.length; x++){
      /* Make lower case */
      t[x] |= 0b00100000;
      /* Make sure character in valid range */
      if(t[x] < 'a' || t[x] > 'z'){
        /* Don't waste time processing invalid tag */
        return null;
      }
    }
    /* Return the sanitized string */
    return new String(t);
  }
}
