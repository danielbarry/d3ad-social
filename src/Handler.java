package b.ds;

/**
 * Handler.java
 *
 * A basic interface for a specific page or function.
 **/
public interface Handler{
  /**
   * process()
   *
   * Process the requirements of the handler.
   *
   * @return The bytes to be written to the client.
   **/
  public byte[] process();
}
