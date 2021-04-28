package b.ds;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server.java
 *
 * Create a server on a given port and handle clients.
 **/
public class Server extends Thread{
  private JSON config;
  private Auth auth;
  private ServerSocket ss;
  private ExecutorService pool;
  private int recBuffSize;
  private int subDirLen;
  private int inputMaxLen;
  private String pstDir;
  private String usrDir;

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
    int poolSize = 8;
    int port = 8080;
    recBuffSize = 2048;
    String subDir = "/";
    inputMaxLen = 512;
    subDirLen = 1;
    pstDir = "dat/pst";
    usrDir = "dat/usr";
    boolean reuseAddr = false;
    int timeout = 10000;
    try{
      poolSize = Integer.parseInt(config.get("pool-size").value(poolSize + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find pool size value");
    }
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
      subDir = config.get("sub-dir").value("/");
      subDirLen = subDir.length();
    }catch(NumberFormatException e){
      Utils.warn("Unable to find sub directory value");
    }
    try{
      inputMaxLen = Integer.parseInt(config.get("input").get("max-length").value(inputMaxLen + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find input maximum length value");
    }
    try{
      timeout = Integer.parseInt(config.get("timeout-ms").value(timeout + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find timeout value");
    }
    pstDir = config.get("data").get("post-dir").value("dat/pst");
    usrDir = config.get("data").get("user-dir").value("dat/usr");
    /* Log out server values */
    Utils.log("Requested pool size is '"           + poolSize    + "'");
    Utils.log("Requested port is '"                + port        + "'");
    Utils.log("Requested receive buffer size is '" + recBuffSize + "'");
    Utils.log("Requested reuse address is '"       + reuseAddr   + "'");
    Utils.log("Requested sub directory is '"       + subDir      + "'");
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
    /* Setup thread pool */
    pool = Executors.newFixedThreadPool(poolSize);
    /* Initialise shared variables */
    Post.init(auth);
    Handler.init(config);
    HandlerAbout.init(config);
    HandlerHome.init(config);
    HandlerLogin.init(config);
    HandlerRegister.init(config);
    HandlerRSS.init(config);
    HandlerUser.init(config);
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
            pool.execute(
              new Process(
                ss.accept(),
                System.currentTimeMillis(),
                recBuffSize,
                subDirLen,
                inputMaxLen,
                auth,
                pstDir,
                usrDir
              )
            );
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
