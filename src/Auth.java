package b.ds;

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
    public String id;
    public String name;
    public String token;
    public long tokenRevoke;
  }

  private HashMap<String, User> tokenUsers;

  /**
   * Auth()
   *
   * Initialise the Auth class.
   *
   * @param config Server configuration information.
   **/
  public Auth(JSON config){
    /* TODO: This needs to be done for things like salting, etc. */
    tokenUsers = new HashMap<String, User>();
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
    /* TODO: Check username meets requirements. */
    /* TODO: Check password meets requirements. */
    /* TODO: Create new user. */
    /* TODO: Login with user. */
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
    /* TODO: Attempt to login the user. */
    /* TODO: Generate a new token and update revoke deadline. */
    return null;
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
    if(tokenUsers.containsKey(token)){
      User user = tokenUsers.get(token);
      /* Make sure it is for sure a token match and not just a hash match */
      if(token.equals(user.token)){
        /* Make sure the token is not timed out */
        if(System.currentTimeMillis() <= user.tokenRevoke){
          return user;
        }
      }
    }
    return null;
  }
}
