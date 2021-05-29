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
  private int tagMaxCount;
  private int authDelay;
  private int postDelay;
  private String pstDir;
  private String tagDir;
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
    int poolSize = 8;
    int port = 8080;
    recBuffSize = 2048;
    String subDir = "/";
    inputMaxLen = 512;
    tagMaxCount = 4;
    authDelay = 1000;
    postDelay = 1000;
    subDirLen = 1;
    pstDir = "dat/pst";
    tagDir = "dat/tag";
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
      inputMaxLen = Integer.parseInt(config.get("input").get("max-length").value(inputMaxLen + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find input maximum length value");
    }
    try{
      tagMaxCount = Integer.parseInt(config.get("tag").get("max-count").value(tagMaxCount + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find input maximum length value");
    }
    try{
      authDelay = Integer.parseInt(config.get("authentication").get("delay-ms").value(authDelay + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find authentication delay value");
    }
    try{
      postDelay = Integer.parseInt(config.get("post").get("delay-ms").value(postDelay + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find post delay value");
    }
    try{
      timeout = Integer.parseInt(config.get("timeout-ms").value(timeout + ""));
    }catch(NumberFormatException e){
      Utils.warn("Unable to find timeout value");
    }
    subDir = config.get("sub-dir").value("/");
    subDirLen = subDir.length();
    pstDir = config.get("data").get("post-dir").value(pstDir);
    tagDir = config.get("data").get("tag-dir").value(tagDir);
    usrDir = config.get("data").get("user-dir").value(usrDir);
    /* Log out server values */
    Utils.log("Requested pool size is '"            + poolSize    + "'");
    Utils.log("Requested port is '"                 + port        + "'");
    Utils.log("Requested receive buffer size is '"  + recBuffSize + "'");
    Utils.log("Requested reuse address is '"        + reuseAddr   + "'");
    Utils.log("Requested input maximum length is '" + inputMaxLen + "'");
    Utils.log("Requested tag maximum count is '"    + tagMaxCount + "'");
    Utils.log("Requested authentication delay is '" + authDelay   + "'");
    Utils.log("Requested post delay is '"           + postDelay   + "'");
    Utils.log("Requested timeout is '"              + timeout     + "'");
    Utils.log("Requested sub directory is '"        + subDir      + "'");
    Utils.log("Requested post directory is '"       + pstDir      + "'");
    Utils.log("Requested tag directory is '"        + tagDir      + "'");
    Utils.log("Requested user directory is '"       + usrDir      + "'");
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
    auth = new Auth(config);
    Data.init(config);
    Post.init(auth);
    Tag.init(config);
    Handler.init(config);
    HandlerAbout.init(config);
    HandlerEmbed.init(config);
    HandlerHome.init(config);
    HandlerLogin.init(config);
    HandlerRegister.init(config);
    HandlerReply.init(config);
    HandlerRSS.init(config);
    HandlerTag.init(config);
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
                tagMaxCount,
                authDelay,
                postDelay,
                auth,
                pstDir,
                tagDir,
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
