package b.ds;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

/**
 * HandlerUser.java
 *
 * Generate a generic user page.
 **/
public class HandlerUser extends Handler{
  private static byte[] error;

  private Auth.User viewer;
  private Auth.User subject;
  private Auth auth;

  /**
   * init()
   *
   * Initialise the generic return data.
   *
   * @param config The configuration to be used for building the generic
   * header.
   **/
  public static void init(JSON config){
    error = "<b>Invalid user</b>".getBytes();
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
   **/
  public HandlerUser(HashMap<String, String> kv, Auth.User viewer, Auth.User subject, Auth auth){
    this.viewer = viewer;
    this.subject = subject;
    this.auth = auth;
  }

  @Override
  public byte[] genBody(){
    StringBuilder res = new StringBuilder();
    /* Check if this is a valid page */
    if(subject != null){
      res
        .append("<h2>").append(subject.username).append("'s posts <a href=\"")
        .append(sub).append("rss/").append(subject.id)
        .append("\">RSS</a></h2>");
      /* Generate post form */
      res = genPostForm(res, viewer);
      /* Check for latest comment */
      if(subject.latest != null){
        /* TODO: Get path from configuration. */
        Post post = Post.readPost("dat/pst" + "/" + subject.latest, new Post());
        int postCount = 0;
        /* TODO: Get length from configuration. */
        /* Begin loading posts */
        while(++postCount <= 16 && post != null){
          res = genPostEntry(res, post, auth);
          post = Post.readPost("dat/pst" + "/" + post.previous, new Post());
        }
        /* TODO: We should provide a link to find out more. */
      }
    }else{
      return error;
    }
    return res.toString().getBytes();
  }

  /**
   * genPostForm()
   *
   * Generate a user post form to add messages if they are logged in.
   *
   * @param sb The StringBuilder object to add the form data to.
   * @param viewer The logged in viewer of the content, otherwise NULL.
   * @return The generated form added to the StringBuilder objects.
   **/
  public static StringBuilder genPostForm(StringBuilder sb, Auth.User viewer){
    if(viewer != null){
      return sb
        .append("<form action=\"").append(sub).append("user/").append(viewer.id).append("\" method=\"post\">")
        .append(  "<textarea")
        .append(    " id=\"post\"")
        .append(    " name=\"post\"")
        .append(    " cols=\"64\"")
        .append(    " rows=\"8\"")
        .append(    " maxlength=\"512\"")
        .append(    " placeholder=\"What do you think? (max 512 characters)\"")
        .append(  "></textarea>")
        .append(  "<br>")
        .append(  "<input type=\"submit\" value=\"submit\">")
        .append("</form>");
    }
    return sb;
  }

  /**
   * genPostEntry()
   *
   * Generate a formatted post entry.
   *
   * @param sb The StringBuilder object to use for appending the content.
   * @param post The post to be formatted.
   * @param auth Access to the authentication object.
   * @return The formatted post appended to the StringBuilder object.
   **/
  public static StringBuilder genPostEntry(StringBuilder sb, Post post, Auth auth){
    return sb
      .append("<p>")
      .append(  "<b><a href=\"").append("user/").append(post.user.id).append("\">@").append(post.user.username)
      .append(  "</a></b> on ").append(new Date(post.creation)).append(" said:")
      .append(  "<br>")
      .append(  "<quote>").append(HandlerUser.postProcessMessage(post.message, auth)).append("</quote>")
      .append("</p>");
  }

  /**
   * postProcessMessage()
   *
   * Post-process the message String just before being served up to add social
   * elements.
   *
   * @param m The message to be formatted.
   * @param auth Access to the authentication object.
   * @return The StringBuilder object generated.
   **/
  public static StringBuilder postProcessMessage(String m, Auth auth){
    StringBuilder r = new StringBuilder();
    String[] p = m.split("\\s+");
    /* Process each part */
    int b = 0;
    for(int x = 0; x < p.length; x++){
      /* Make sure it's long enough to be processed */
      if(p[x].length() > 2){
        boolean u = false;
        /* Process first character */
        switch(p[x].charAt(0)){
          case '@' :
            Auth.User user = auth.getUserByName(p[x].substring(1));
            if(user != null){
              r.append("<a href=\"").append(sub).append("user/")
                .append(user.id).append("\">@").append(user.username)
                .append("</a>");
            }else{
              r.append(p[x]);
            }
            break;
          case '*' :
            if(b % 2 == 0){
              r.append("<b>");
            }
            ++b;
            r.append(p[x]);
            break;
          case '[' :
            u = true;
            break;
          default :
            r.append(p[x]);
            break;
        }
        /* Process last character */
        switch(p[x].charAt(p[x].length() - 1)){
          case '*' :
            if(b % 2 == 1){
              r.append("</b>");
            }
            ++b;
            break;
          case ']' :
            if(u){
              try{
                String uStr = p[x].substring(1, p[x].length() - 1);
                URL url = new URL(uStr);
                String uName = uStr;
                if(uName.length() > 20){
                  uName = uName.substring(0, 16);
                  uName += "..";
                }
                r.append("<a href=\"").append(uStr).append("\">").append(uName)
                  .append("</a>");
              }catch(MalformedURLException e){
                r.append(p[x]);
              }
            }else{
              r.append(p[x]);
            }
            break;
          default :
            /* If we never completed the URL, print what we have */
            if(u){
              r.append(p[x]);
            }
            break;
        }
      }else{
        r.append(p[x]);
      }
      r.append(' ');
    }
    /* Close off bold tag if not done */
    if(b % 2 == 1){
      r.append("</b>");
    }
    return r;
  }
}
