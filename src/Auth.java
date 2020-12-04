package b.ds;

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
  }

  /**
   * Auth()
   *
   * Initialise the Auth class.
   **/
  public Auth(){
    /* TODO: This needs to be done for things like salting, etc. */
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
    return null;
  }
}
