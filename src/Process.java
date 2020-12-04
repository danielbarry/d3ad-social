package b.ds;

import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
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
  private static final byte[] HTTP_BAD = "<b>Error</b>".getBytes();

  private Socket s;
  private int recBuffSize;

  /**
   * Process()
   *
   * Allow the client to be processed.
   *
   * @param socket The socket of the client.
   * @param recBuffSize The receiver buffer size.
   **/
  public Process(Socket socket, int recBuffSize){
    this.s = socket;
    this.recBuffSize = recBuffSize;
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
    Auth.User user = parseAuth(kv);
    /* Start sending data */
    writeHead(s);
    /* Pass request onto handler */
    if(kv.containsKey("location")){
      /* TODO: Derive handler string. */
      switch(kv.get("location")){
        case "/" :
        case "/index" :
        case "/index.htm" :
        case "/index.html" :
          HandlerHome hh = new HandlerHome(kv);
          write(s, hh.genHead(user));
          write(s, hh.genBody());
          write(s, hh.genFoot());
          break;
        case "/login" :
          HandlerLogin hl = new HandlerLogin(kv);
          write(s, hl.genHead(user));
          write(s, hl.genBody());
          write(s, hl.genFoot());
          break;
        case "/register" :
          HandlerRegister hr = new HandlerRegister(kv);
          write(s, hr.genHead(user));
          write(s, hr.genBody());
          write(s, hr.genFoot());
          break;
        default :
          writeBad(s);
          break;
      }
    }else{
      writeBad(s);
    }
    /* Close the socket */
    close(s);
    Utils.log("Process client ended");
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
          kv.put(parts[i].substring(0, s), parts[i].substring(s + 1));
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
   * @return The logged in user, otherwise NULL.
   **/
  private static Auth.User parseAuth(HashMap<String, String> kv){
    Auth.User user = null;
    /* Check if login or registration */
    if(kv.containsKey("username")){
      /* Is login */
      if(kv.containsKey("password")){
        /* TODO: Attempt login. */
      /* Is registration */
      }else if(kv.containsKey("passworda") && kv.containsKey("passwordb")){
        /* TODO: Attempt registration. */
      }
    /* Check if already logged in */
    }else if(kv.containsKey("Cookie")){
      /* TODO: Attempt to authenticate with the key. */
    }
    return null;
  }

  /**
   * writeHead()
   *
   * Pre-write the header for the client.
   *
   * @param s The socket to be write.
   **/
  private static void writeHead(Socket s){
    write(s, HTTP_HEAD);
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
