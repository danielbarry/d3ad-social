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
    public I512 id = null;
    /* Unique salt for the user */
    public I512 usalt = null;
    /* The username the user has chosen */
    public String username = null;
    /* The password the user has chosen (encrypted) */
    public I512 password = null;
    /* TODO: Remove old user token if issuing another or is old. */
    /* The current user token */
    public I512 token = null;
    /* The time at which the token comes invalid */
    public long revoke = -1;
    /* The latest post by this user */
    public I512 latest = null;
  }

  private static HashMap<I512, User> idMap = new HashMap<I512, User>();
  private static HashMap<String, User> userMap = new HashMap<String, User>();
  private static HashMap<I512, User> tokenMap = new HashMap<I512, User>();

  private JSON config;
  private I512 salt;
  private String userDir;
  private long tokenTimeout;

  /**
   * Auth()
   *
   * Initialise the Auth class.
   *
   * @param config Server configuration information.
   **/
  public Auth(JSON config){
    this.config = config;
    salt = new I512(config.get("security").get("salt").value("0"));
    userDir = config.get("data").get("user-dir").value("dat/usr");
    tokenTimeout = Long.parseLong(config.get("security").get("token-timeout-ms").value("86400000"));
    /* Read users from disk */
    File[] users = (new File(userDir)).listFiles();
    for(int x = 0; x < users.length; x++){
      if(users[x].isFile() && !users[x].isDirectory() && users[x].length() > 0){
        User user = readUser(users[x].getPath(), new User());
        /* Check if old user type */
        if(users[x].getPath().length() - (userDir.length() + 1) > I512.MAX_STR_BASE64_LEN){
          Utils.log("Upgrading user -> " + user.id);
          /* Create new type */
          if(writeUser(userDir + "/" + user.id, user) != null){
            Utils.log("Removing old user -> " + user.id);
            /* Remove old type if */
            (new File(userDir + "/" + I512.toHexString(user.id.toByteArray()))).delete();
          }
        }
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
      username.length() < 6    ||
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
    while(idMap.containsKey(user.id = Utils.genRandHash()));
    user.usalt = Utils.genRandHash();
    user.username = username;
    user.password = Utils.genPassHash(salt, user.usalt, passwordA);
    /* Generate unique token */
    while(tokenMap.containsKey(user.token = Utils.genRandHash()));
    user.revoke = System.currentTimeMillis() + tokenTimeout;
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
      I512 pwd = Utils.genPassHash(salt, user.usalt, password);
      /* Make sure for sure it's the right user and password */
      if(user.username.equals(username) && user.password.equals(pwd)){
        /* Remove existing token if required */
        if(tokenMap.containsKey(user.token) && tokenMap.get(user.token).token == user.token){
          tokenMap.remove(user.token);
        }
        /* Generate a unique token and update revoke deadline */
        while(tokenMap.containsKey(user.token = Utils.genRandHash()));
        user.revoke = System.currentTimeMillis() + tokenTimeout;
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
    I512 t = null;
    try{
      t = new I512(token);
    }catch(NumberFormatException e){
      Utils.warn("Badly formatted token " + token);
    }
    /* Check that we do have a token */
    if(t != null && tokenMap.containsKey(t)){
      User user = tokenMap.get(t);
      /* Make sure it is for sure a token match and not just a hash match */
      if(user.token != null && t.equals(user.token)){
        /* Make sure the token is not timed out */
        if(System.currentTimeMillis() <= user.revoke){
          return user;
        }
      }
    }
    Utils.log("Invalid or inactive token used");
    return null;
  }

  /**
   * getUserById()
   *
   * Get the user by a given ID, or NULL if the user cannot be found.
   *
   * @param id The String ID to use for searching.
   * @return The user found, otherwise NULL.
   **/
  public User getUserById(String id){
    /* Make sure it's not NULL */
    if(id == null){
      return null;
    }
    I512 i = new I512(id);
    /* Check for user String */
    if(idMap.containsKey(i)){
      User user = idMap.get(i);
      /* Make sure it really was a match */
      if(user.id.equals(i)){
        return user;
      }
    }
    return null;
  }

  /**
   * getUserByName()
   *
   * Get the user by a given name, or NULL if the user cannot be found.
   *
   * @param name The String name to use for searching.
   * @return The user found, otherwise NULL.
   **/
  public User getUserByName(String name){
    /* Make sure it's not NULL */
    if(name == null){
      return null;
    }
    /* Check for user String */
    if(userMap.containsKey(name)){
      User user = userMap.get(name);
      /* Make sure it really was a match */
      if(user.username.equals(name)){
        return user;
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
   * @param path The path for the user configuration.
   * @param user A user object to be written to.
   * @return The user object, otherwise NULL.
   **/
  public static User readUser(String path, User user){
    try{
      JSON userData = JSON.build(path);
      user.id = new I512(userData.get("id").value(null));
      user.usalt = null;
      try{
        user.usalt = new I512(userData.get("usalt").value(null));
      }catch(NumberFormatException e){
        Utils.warn("Failed to read user's salt from configuration");
        return null;
      }
      user.username = userData.get("username").value(null);
      user.password = new I512(userData.get("password").value(null));
      user.token = null;
      user.revoke = System.currentTimeMillis();
      user.latest = new I512(userData.get("latest").value(null));
      if(
        user.id != null       &&
        user.usalt != null    &&
        user.username != null &&
        user.password != null
      ){
        idMap.put(user.id, user);
        userMap.put(user.username, user);
        return user;
      }else{
        Utils.warn("Could not read user");
        return null;
      }
    }catch(Exception e){
      Utils.warn("Exception throw whilst reading user");
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
  public static User writeUser(String path, User user){
    /* Save the user to disk */
    JSON data = null;
    try{
      data = new JSON(false);
      data.set(new JSON("id", user.id.toString()));
      data.set(new JSON("usalt", user.usalt.toString()));
      data.set(new JSON("username", user.username));
      data.set(new JSON("password", user.password.toString()));
      /* NOTE: Do not store token. */
      /* NOTE: Do not store revoke. */
      if(user.latest != null){
        data.set(new JSON("latest", user.latest.toString()));
      }
    }catch(Exception e){
      data = null;
    }
    if(data != null && Data.write(path, data.toString())){
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

  /**
   * getNumUsers()
   *
   * Get the number of registered users by checking how many are loaded.
   *
   * @return The number of registered users.
   **/
  public int getNumUsers(){
    return idMap.size();
  }

  /**
   * getActiveUsers()
   *
   * Get the number of active users by checking how many tokens are active.
   *
   * @return The number of active users.
   **/
  public int getActiveUsers(){
    /* TODO: Should more accurately reflect active users by checking token times. */
    return tokenMap.size();
  }
}
