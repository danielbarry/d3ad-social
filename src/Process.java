package b.ds;

import java.net.Socket;

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
    /* TODO: Implement the processor. */
    Utils.log("Process client ended");
  }
}
