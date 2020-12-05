package b.ds;

import java.io.File;
import java.util.HashMap;

/**
 * Auth.java
 *
 * Handle all user authentication requests.
 **/
public class Auth{
  /**
   * User.Auth.java
   *
   * An abstract user held in RAM, containing essential data regarding the user
   * in question.
   **/
  public class User{
    /* Unique user ID */
    public String id;
    /* Unique salt for the user */
    public byte[] usalt;
    /* The username the user has chosen (encrypted) */
    public String username;
    /* The password the user has chosen (encrypted) */
    public String password;
    /* The current user token */
    public String token;
    /* The time at which the token comes invalid */
    public long revoke;
  }

  private JSON config;
  private byte[] salt;
  private String userDir;
  private HashMap<String, User> idMap;
  private HashMap<String, User> userMap;
  private HashMap<String, User> tokenMap;

  /**
   * Auth()
   *
   * Initialise the Auth class.
   *
   * @param config Server configuration information.
   **/
  public Auth(JSON config){
    this.config = config;
    salt = Utils.hexToBytes(config.get("security").get("salt").value(""));
    userDir = config.get("data").get("user-dir").value("dat/usr");
    idMap = new HashMap<String, User>();
    userMap = new HashMap<String, User>();
    tokenMap = new HashMap<String, User>();
    /* Read users from disk */
    File[] users = (new File(userDir)).listFiles();
    for(int x = 0; x < users.length; x++){
      if(users[x].isFile() && !users[x].isDirectory() && users[x].length() > 0){
        User user = readUser(users[x].getPath());
      }
    }
  }

  /**
   * register()
   *
   * Attempt registration and reply back with the validated used, otherwise NULL.
   *
   * @param username The requested username.
   * @param passwordA The first password.
   * @param passwordB The repeated password.
   * @return The logged in user, otherwise NULL.
   **/
  public User register(String username, String passwordA, String passwordB){
    Utils.logUnsafe("Trying to register new user", username);
    /* Check username meets requirements */
    if(
      username == null         ||
      username.length() < 8    ||
      username.length() > 64   ||
      !checkUsername(username) ||
      userMap.containsKey(username)
    ){
      Utils.logUnsafe("Bad username for registration", username);
      return null;
    }
    /* Check password meets requirements */
    if(
      passwordA == null       ||
      passwordA.length() < 8  ||
      passwordA.length() > 64 ||
      !passwordA.equals(passwordB)
    ){
      Utils.logUnsafe("Bad password for registration", username);
      return null;
    }
    /* Create new user */
    User user = new User();
    /* Generate unique ID */
    while(idMap.containsKey(user.id = Utils.bytesToHex(Utils.genRandHash())));
    user.usalt = Utils.genRandHash();
    user.username = username;
    user.password = Utils.genPassHash(salt, user.usalt, passwordA);
    /* Generate unique token */
    while(tokenMap.containsKey(user.token = Utils.bytesToHex(Utils.genRandHash())));
    user.revoke = System.currentTimeMillis() * 2; // TODO
    /* Save the user to disk */
    if(writeUser(userDir + "/" + user.id, user) != user){
      Utils.warn("Unable to save new user");
    }else{
      idMap.put(user.id, user);
      userMap.put(user.username, user);
      tokenMap.put(user.token, user);
    }
    /* Login with user */
    return login(username, passwordA);
  }

  /**
   * login()
   *
   * Attempt to login the user, otherwise return NULL.
   *
   * @param username The username.
   * @param password The password.
   * @return The logged in user, otherwise NULL.
   **/
  public User login(String username, String password){
    User user = null;
    /* Attempt to login the user */
    if(userMap.containsKey(username)){
      user = userMap.get(username);
      password = Utils.genPassHash(salt, user.usalt, password);
      /* Make sure for sure it's the right user and password */
      if(user.username.equals(username) && user.password.equals(password)){
        /* Remove existing token if required */
        if(tokenMap.containsKey(user.token) && tokenMap.get(user.token).token == user.token){
          tokenMap.remove(user.token);
        }
        /* Generate a unique token and update revoke deadline */
        while(tokenMap.containsKey(user.token = Utils.bytesToHex(Utils.genRandHash())));
        user.revoke = System.currentTimeMillis() * 2; // TODO
        tokenMap.put(user.token, user);
      }else{
        user = null;
      }
    }
    return user;
  }

  /**
   * token()
   *
   * Convert a token to a user if possible, otherwise return NULL.
   *
   * @param token The token to be checked.
   * @return The logged in user, otherwise NULL.
   **/
  public User token(String token){
    /* Make sure it's not NULL */
    if(token == null){
      return null;
    }
    /* Check that we do have a token */
    if(tokenMap.containsKey(token)){
      User user = tokenMap.get(token);
      /* Make sure it is for sure a token match and not just a hash match */
      if(user.token != null && token.equals(user.token)){
        /* Make sure the token is not timed out */
        if(System.currentTimeMillis() <= user.revoke){
          return user;
        }
      }
    }
    return null;
  }

  /**
   * readUser()
   *
   * Read user data from disk and update the relevant variables. Return NULL if
   * an issue occurs.
   *
   * @param userPath The path for the user configuration.
   * @return The user object, otherwise NULL.
   **/
  private User readUser(String userPath){
    try{
      JSON userData = JSON.build(userPath);
      User user = new User();
      user.id = userData.get("id").value(null);
      String usalt = userData.get("usalt").value(null);
      if(usalt != null){
        user.usalt = Utils.hexToBytes(usalt);
      }else{
        Utils.warn("Failed to read user's salt from configuration");
        return null;
      }
      user.username = userData.get("username").value(null);
      user.password = userData.get("password").value(null);
      user.token = null;
      if(
        user.id != null       &&
        user.usalt != null    &&
        user.username != null &&
        user.password != null
      ){
        idMap.put(user.id, user);
        userMap.put(user.username, user);
      }
      return user;
    }catch(Exception e){
      return null;
    }
  }

  /**
   * writeUser()
   *
   * Write the user data to disk and update the relevant variables. Return NULL
   * if an issue occurs.
   *
   * @param path The path for the user configuration.
   * @param user The user object to be written.
   * @return The user object, otherwise NULL.
   **/
  private User writeUser(String path, User user){
    /* Save the user to disk */
    String data = "{" +
      "\"id\":\""       + user.id                      + "\"," +
      "\"usalt\":\""    + Utils.bytesToHex(user.usalt) + "\"," +
      "\"username\":\"" + user.username                + "\"," +
      "\"password\":\"" + user.password                + "\""  +
    "}";
    if(Data.write(userDir + "/" + user.id, data)){
      Utils.logUnsafe("User configuration saved", user.username);
      return user;
    }else{
      return null;
    }
  }

  /**
   * checkUsername()
   *
   * Check that a username contains valid characters.
   *
   * @param username The username String to be checked.
   * @return True if the username contains only valid characters, otherwise
   * false.
   **/
  private boolean checkUsername(String username){
    for(char c : username.toCharArray()){
      if(
        (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        (c >= '0' && c <= '9')
      ){
        continue;
      }else{
        return false;
      }
    }
    return true;
  }
}
