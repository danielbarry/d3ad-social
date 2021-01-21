package b.ds;

import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Process.java
 *
 * This class is responsible for parsing the client and figuring out how it
 * should be handled.
 **/
public class Process extends Thread{
  private static final byte[] HTTP_LINE = "\r\n".getBytes();
  private static final byte[] HTTP_HEAD = "HTTP/1.1 200 OK".getBytes();
  private static final byte[] HTTP_TYPE = "Content-Type: text/html; charset=utf-8".getBytes();
  private static final byte[] HTTP_COOK = "Set-Cookie: ".getBytes();
  private static final byte[] HTTP_BAD = "<b>Error</b>".getBytes();

  private Socket s;
  private long start;
  private int recBuffSize;
  private int subDirLen;
  private Auth auth;

  /**
   * Process()
   *
   * Allow the client to be processed.
   *
   * @param socket The socket of the client.
   * @param start The start time for the process.
   * @param recBuffSize The receiver buffer size.
   * @param subDirLen The subdirectory length.
   * @param auth Access to the authentication mechanism.
   **/
  public Process(Socket socket, long start, int recBuffSize, int subDirLen, Auth auth){
    this.s = socket;
    this.start = start;
    this.recBuffSize = recBuffSize;
    this.subDirLen = subDirLen;
    this.auth = auth;
  }

  /**
   * run()
   *
   * Start processing the user's request.
   **/
  @Override
  public void run(){
    Utils.log("Process client started");
    /* Read the header */
    String raw = readHead(s, recBuffSize);
    if(raw == null){
      Utils.warn("Failed to read from client socket");
      return;
    }
    /* Parse the header */
    HashMap<String, String> kv = parseHead(raw);
    /* Authenticate (if required) */
    Auth.User user = parseAuth(kv, auth);
    /* Handle user POST */
    if(!parsePost(kv, user)){
      /* Delete location to force a bad message */
      kv.remove("location");
    }
    /* Pass request onto handler */
    if(kv.containsKey("location")){
      String loc = kv.get("location");
      loc = loc.length() < subDirLen ? "" : loc.substring(subDirLen);
      /* Derive handler string */
      String hand = loc;
      int z = hand.indexOf('/');
      if(z >= 0){
        hand = hand.substring(0, z);
        loc = loc.substring(z + 1, loc.length());
      }
      Handler h = new HandlerHome(kv, user, auth);
      Utils.logUnsafe("User requesting from location", hand);
      switch(hand){
        case "" :
        case "index" :
        case "index.htm" :
        case "index.html" :
          writeHead(s, h.genMime(), user);
          write(s, h.genHead(user));
          write(s, h.genBody());
          write(s, h.genFoot());
          break;
        case "login" :
          if(user == null){
            h = new HandlerLogin(kv);
          }
          writeHead(s, h.genMime(), user);
          write(s, h.genHead(user));
          write(s, h.genBody());
          write(s, h.genFoot());
          break;
        case "register" :
          if(user == null){
            h = new HandlerRegister(kv);
          }
          writeHead(s, h.genMime(), user);
          write(s, h.genHead(user));
          write(s, h.genBody());
          write(s, h.genFoot());
          break;
        case "rss" :
          h = new HandlerRSS(kv, user, auth.getUserById(loc));
          writeHead(s, h.genMime(), user);
          write(s, h.genHead(user));
          write(s, h.genBody());
          write(s, h.genFoot());
          break;
        case "user" :
          h = new HandlerUser(kv, user, auth.getUserById(loc), auth);
          writeHead(s, h.genMime(), user);
          write(s, h.genHead(user));
          write(s, h.genBody());
          write(s, h.genFoot());
          break;
        default :
          writeHead(s, HTTP_TYPE, user);
          writeBad(s);
          Utils.log("Unable to process request from location");
          break;
      }
    }else{
      writeHead(s, HTTP_TYPE, user);
      writeBad(s);
      Utils.log("Unable to read header");
    }
    /* Close the socket */
    close(s);
    Utils.log("Process client ended after " + (System.currentTimeMillis() - start) + " ms");
  }

  /**
   * readHead()
   *
   * Read the header from the socket.
   *
   * @param s The socket to be read.
   * @param n The maximum buffer size.
   * @return The data read from the socket. If an error occurs, return NULL.
   **/
  private static String readHead(Socket s, int n){
    String r = null;
    if(s != null && n > 0){
      byte[] buff = new byte[n];
      try{
        s.getInputStream().read(buff);
        r = new String(buff, StandardCharsets.UTF_8);
      }catch(IOException e){
        r = null;
      }
    }
    return r;
  }

  /**
   * parseHead()
   *
   * Parse the header from a given string. If no values are found, an empty
   * hash set is returned.
   *
   * @param h The raw header string.
   * @return A collection of key value header data.
   **/
  private static HashMap<String, String> parseHead(String h){
    HashMap<String, String> kv = new HashMap<String, String>();
    String[] lines = h.split("\r\n");
    /* If it's the first entry, process the HTTP */
    if(lines.length > 0){
      String[] parts = lines[0].split(" ");
      if(parts.length == 3){
        kv.put("request", parts[0]);
        kv.put("location", parts[1]);
        kv.put("protocol", parts[2]);
      }else{
        Utils.logUnsafe("Unable to process the first line of header", lines[0]);
      }
    }
    /* Process the remaining key values */
    int x = 1;
    for(; x < lines.length; x++){
      if(lines[x].length() <= 0 || lines[x].charAt(0) == '\0'){
        break;
      }
      int s = lines[x].indexOf(':');
      if(s >= 0){
        kv.put(lines[x].substring(0, s).trim(), lines[x].substring(s + 1).trim());
      }else{
        Utils.logUnsafe("Unable to process header line", lines[x]);
      }
    }
    /* TODO: We assume that Content-Type: application/x-www-form-urlencoded. */
    /* Process POST data */
    for(++x; x < lines.length; x++){
      if(lines[x].length() <= 0 || lines[x].charAt(0) == '\0'){
        break;
      }
      String[] parts = lines[x].split("&");
      for(int i = 0; i < parts.length; i++){
        int s = parts[i].indexOf('=');
        if(s >= 0){
          kv.put(parts[i].substring(0, s).trim(), parts[i].substring(s + 1).trim());
        }else{
          Utils.logUnsafe("Unable to process form line", parts[i]);
        }
      }
    }
    return kv;
  }

  /**
   * parseAuth()
   *
   * Try to authenticate a user if possible. Reasons for authorizing a user
   * could be from login, registration or a user which is already logged in.
   * The purpose is to handle these cases and allow for a logged in user to be
   * returned if possible.
   *
   * @param kv The key value mappings from the header.
   * @param auth The server authentication mechanism.
   * @return The logged in user, otherwise NULL.
   **/
  private static Auth.User parseAuth(HashMap<String, String> kv, Auth auth){
    Auth.User user = null;
    /* Check if login or registration */
    if(kv.containsKey("username")){
      /* Is login */
      if(kv.containsKey("password")){
        return auth.login(kv.get("username"), kv.get("password"));
      /* Is registration */
      }else if(kv.containsKey("passworda") && kv.containsKey("passwordb")){
        return auth.register(kv.get("username"), kv.get("passworda"), kv.get("passwordb"));
      }
    /* Check if already logged in */
    }else if(kv.containsKey("Cookie")){
      String rawCookie = kv.get("Cookie");
      int z = rawCookie.lastIndexOf('=');
      if(z >= 0){
        return auth.token(rawCookie.substring(z + 1));
      }
    }
    return null;
  }

  /**
   * parsePost()
   *
   * Parse a POST request from the user from the context of a specific logged
   * in user. Actions handled here may include commenting for example.
   *
   * @param kv The key value mappings from the header.
   * @param user The authenticated user, otherwise NULL.
   * @return True if there are no errors to be reported, otherwise false.
   **/
  private static boolean parsePost(HashMap<String, String> kv, Auth.User user){
    /* Make sure we handle a logged in user */
    if(user == null){
      return true;
    }
    /* Check if a post request was made */
    if(kv.containsKey("post")){
      /* TODO: Get from configuration. */
      String postDir = "dat/pst";
      String userDir = "dat/usr";
      /* Create a post object */
      Post post = new Post();
      while(Data.exists(postDir + "/" + (post.id = Utils.bytesToHex(Utils.genRandHash()))));
      post.user = user;
      post.creation = System.currentTimeMillis();
      post.previous = user.latest;
      try{
        post.message = URLDecoder.decode(kv.get("post"), "UTF-8");
      }catch(UnsupportedEncodingException e){
        return false;
      }
      /* Validate the input */
      if(
        post.message == null       ||
        post.message.length() <= 0 ||
        post.message.length() > 512
      ){
        return false;
      }
      /* Sanitize the input */
      post.message = Utils.sanitizeString(post.message);
      /* Save post */
      if(Post.writePost(postDir + "/" + post.id, post) != post){
        Utils.warn("Unable to save new post");
        return false;
      }
      /* Update user data */
      user.latest = post.id;
      if(Auth.writeUser(userDir + "/" + user.id, user) != user){
        Utils.warn("Unable to save updated user");
        return false;
      }
      return true;
    }
    return true;
  }

  /**
   * writeHead()
   *
   * Pre-write the header for the client.
   *
   * @param s The socket to be write.
   * @param mime The mime byte array.
   * @param user A valid authorized user if one has been found.
   **/
  private static void writeHead(Socket s, byte[] mime, Auth.User user){
    write(s, HTTP_HEAD);
    write(s, HTTP_LINE);
    write(s, mime);
    if(user != null){
      write(s, HTTP_LINE);
      write(s, HTTP_COOK);
      write(s, ("token=" + user.token).getBytes());
    }
    write(s, HTTP_LINE);
    write(s, HTTP_LINE);
  }

  /**
   * writeBad()
   *
   * Write an error to the client.
   *
   * @param s The socket to be write.
   **/
  private static void writeBad(Socket s){
    write(s, HTTP_BAD);
  }

  /**
   * write()
   *
   * Write to the client.
   *
   * @param s The socket to be write.
   * @param b The bytes to be written.
   **/
  private static void write(Socket s, byte[] b){
    if(s != null){
      try{
        s.getOutputStream().write(b);
      }catch(IOException e){
        /* Do nothing */
      }
    }
  }

  /**
   * close()
   *
   * Write the remaining buffer and close the connection.
   *
   * @param s The socket to be write.
   **/
  private static void close(Socket s){
    if(s != null){
      try{
        s.getOutputStream().flush();
        s.getOutputStream().close();
        s.close();
      }catch(IOException e){
        /* Do nothing */
      }
    }
  }
}
