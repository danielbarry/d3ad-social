package b.ds;

import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

/**
 * Process.java
 *
 * This class is responsible for parsing the client and figuring out how it
 * should be handled.
 **/
public class Process extends Thread{
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
    /* TODO: Authenticate (if required). */
    /* TODO: Pass request onto handler. */
    /* TODO: Close the socket. */
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
        r = new String(buff);
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
    for(int x = 1; x < lines.length; x++){
      if(lines[x].length() > 0 && lines[x].charAt(0) != '\0'){
        int s = lines[x].indexOf(':');
        if(s >= 0){
          kv.put(lines[x].substring(0, s), lines[x].substring(s + 1));
        }else{
          Utils.logUnsafe("Unable to process header line", lines[x]);
        }
      }
    }
    return kv;
  }
}
