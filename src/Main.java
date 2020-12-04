package b.ds;

/**
 * Main.java
 *
 * Process command line inputs, load the configuration and start the server.
 **/
public class Main{
  private JSON json;

  /**
   * main()
   *
   * The entry point into the program. This method creates an instance of Main
   * and passes the command line arguments to it.
   *
   * @param args The command line arguments.
   **/
  public static void main(String[] args){
    Utils.log("Program started");
    new Main(args);
    Utils.log("Program ended");
  }

  /**
   * Main()
   *
   * Check the command line options and execute the program appropriately.
   *
   * @param args The command line arguments.
   **/
  public Main(String[] args){
    try{
      json = new JSON("");
    }catch(Exception e){
      json = null;
      Utils.error("Was unable to create blank JSON configuration");
    }
    /* Loop the command line arguments */
    for(int x = 0; x < args.length; x++){
      /* Check which command we are processing */
      switch(args[x]){
        case "-c" :
        case "--config" :
          x = config(args, x);
          break;
        case "-h" :
        case "--help" :
        case "?" :
        case "/?" :
          x = help(args, x);
          break;
        default :
          Utils.warn("Unknown parameter '" + args[x] + "', see '-h' for help");
          break;
      }
    }
    /* Check if we should run HTTP server */
    if(json.get("active").value("false").equals("true")){
      Utils.log("Starting HTTP server");
      Server s = new Server(json);
      s.start();
    }else{
      Utils.log("Not starting HTTP server");
    }
  }

  /**
   * config()
   *
   * Set the server configuration file.
   *
   * @param args The command line arguments.
   * @param x The current offset into the command line arguments.
   * @return The new offset into the command line arguments.
   **/
  private int config(String[] args, int x){
    if(++x < args.length){
      try{
        json = JSON.build(args[x]);
      }catch(Exception e){
        Utils.warn("Failed to load configuration file '" + args[x] + "'");
        try{
          json = new JSON("");
        }catch(Exception z){
          Utils.error("Failed to create blank JSON configuration");
        }
      }
    }else{
      Utils.warn("Not enough parameters given to set configuration");
    }
    return x;
  }

  /**
   * help()
   *
   * Display program help.
   *
   * @param args The command line arguments.
   * @param x The current offset into the command line arguments.
   * @return The new offset into the command line arguments.
   **/
  private int help(String[] args, int x){
    System.out.println("fc.jar [OPT]");
    System.out.println("");
    System.out.println("  OPTions");
    System.out.println("");
    System.out.println("    -c  --config  Server configuration file");
    System.out.println("                    <FILE>.json");
    System.out.println("    -h  --help    Display this help");
    return x;
  }
}
