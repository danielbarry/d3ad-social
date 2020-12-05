package b.ds;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Server.java
 *
 * Create a server on a given port and handle clients.
 **/
public class Server extends Thread{
  private JSON config;
  private Auth auth;
  private ServerSocket ss;
  private int recBuffSize;

  /**
   * Server()
   *
   * Initialise the server with the required settings.
   *
   * @param config The configuration file.
   **/
  public Server(JSON config){
    this.config = config;
    this.auth = new Auth(config);
    int port = 8080;
    recBuffSize = 2048;
    boolean reuseAddr = false;
    int timeout = 10000;
    try{
      port = Integer.parseInt(config.get("port").value(port + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find port value");
    }
    try{
      recBuffSize = Integer.parseInt(config.get("rec-buff-size").value(recBuffSize + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find receive buffer size value");
    }
    reuseAddr = config.get("reuse-addr").value("false").equals("true");
    try{
      timeout = Integer.parseInt(config.get("timeout-ms").value(timeout + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find timeout value");
    }
    /* Log out server values */
    Utils.log("Requested port is '"                + port        + "'");
    Utils.log("Requested receive buffer size is '" + recBuffSize + "'");
    Utils.log("Requested reuse address is '"       + reuseAddr   + "'");
    Utils.log("Requested timeout is '"             + timeout     + "'");
    /* Setup server socket */
    try{
      ss = new ServerSocket(port);
      Utils.log("Binded server at " + port);
    }catch(IOException e){
      ss = null;
      Utils.warn("Unable to bind server at " + port);
    }
    /* Setup server configuration if we can */
    if(ss != null){
      try{
        ss.setReceiveBufferSize(recBuffSize);
        Utils.log("Server receive buffer size set");
      }catch(SocketException e){
        Utils.warn("Unable to set receive buffer size");
      }
      try{
        ss.setReuseAddress(reuseAddr);
        Utils.log("Server reuse address set");
      }catch(SocketException e){
        Utils.warn("Unable to set reuse address");
      }
      try{
        ss.setSoTimeout(timeout);
        Utils.log("Server timeout set");
      }catch(SocketException e){
        Utils.warn("Unable to set timeout");
      }
    }
  }

  /**
   * run()
   *
   * Run the server thread for the given port.
   **/
  @Override
  public void run(){
    /* Make sure we setup the server socket */
    if(ss == null){
      Utils.warn("Cannot start main server loop");
      return;
    }
    /* Outer server main loop */
    for(;;){
      try{
        /* Inner server main loop */
        for(;;){
          try{
            (new Process(ss.accept(), recBuffSize, auth)).start();
          }catch(SocketTimeoutException ste){
            Utils.log("Socket timeout, client may have be disconnected");
          }
        }
      }catch(Exception e){
        Utils.warn("Inner main loop crashed, restarting");
      }
    }
  }
}
