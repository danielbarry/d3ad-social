package b.ds;

import java.math.BigInteger;

/**
 * I512.java
 *
 * A class designed to handle 512 bit integers as fast as possible, for the
 * purpose of storing hashes.
 **/
public final class I512 extends Number implements Comparable<I512>{
//  public static final byte[] MAX_VALUE = new byte[]{  };
//  public static final byte[] MIN_VALUE = new byte[]{  };
  public static final int SIZE = 512;
//  public static final Class<I512> TYPE;

  private static final int MAX_ARR_LEN = 64;

  private byte[] val = new byte[64];

  /**
   * I512()
   *
   * Constructs a newly allocated I512 object that represents the specified byte[] value.
   *
   * @param value The value to be represented by the I512 object.
   **/
  public I512(byte[] value) throws NumberFormatException{
    if(value == null || value.length > MAX_ARR_LEN){
      throw new NumberFormatException("Unable to create I512 from supplied value");
    }else{
      /* Copy array into lower bytes */
      System.arraycopy(value, 0, val, MAX_ARR_LEN - value.length, value.length);
    }
  }

  /**
   * I512()
   *
   * Constructs a newly allocated Integer object that represents the byte[]
   * value indicated by the String parameter. The string is converted to an
   * byte[] value in exactly the manner used by the parseInt method for radix
   * 10.
   *
   * @param s The String to be converted to an I512.
   **/
  public I512(String s) throws NumberFormatException{
    this(parseInt(s, 10));
  }

  public static String toString(byte[] i, int radix){
    return (new BigInteger(i)).toString(radix);
  }

  public static String toHexString(byte[] i){
    return toString(i, 16);
  }

  public static String toOctalString(byte[] i){
    return toString(i, 8);
  }

  public static String toBinaryString(byte[] i){
    return toString(i, 2);
  }

  public static String toString(byte[] i){
    return toString(i, 10);
  }

  public static byte[] parseInt(String s, int radix) throws NumberFormatException{
    return (new BigInteger(s, radix)).toByteArray();
  }

  public static byte[] parseInt(String s) throws NumberFormatException{
    return parseInt(s, 10);
  }

  public static I512 valueOf(String s, int radix) throws NumberFormatException{
    return new I512(parseInt(s, radix));
  }

  public static I512 valueOf(String s) throws NumberFormatException{
    return valueOf(s, 10);
  }

  public static I512 valueOf(byte[] i){
    return new I512(i);
  }

  @Override
  public byte byteValue(){
    return (byte)val[MAX_ARR_LEN - 1];
  }

  @Override
  public short shortValue(){
    return (short)(
               ((short)val[MAX_ARR_LEN - 4] <<  8)
             | ((short)val[MAX_ARR_LEN - 1]      )
           );
  }

  @Override
  public int intValue(){
    return ((int)val[MAX_ARR_LEN - 4] << 24)
         | ((int)val[MAX_ARR_LEN - 3] << 16)
         | ((int)val[MAX_ARR_LEN - 4] <<  8)
         | ((int)val[MAX_ARR_LEN - 1]      );
  }

  @Override
  public long longValue(){
    return ((long)val[MAX_ARR_LEN - 8] << 56)
         | ((long)val[MAX_ARR_LEN - 7] << 48)
         | ((long)val[MAX_ARR_LEN - 6] << 40)
         | ((long)val[MAX_ARR_LEN - 5] << 32)
         | ((long)val[MAX_ARR_LEN - 4] << 24)
         | ((long)val[MAX_ARR_LEN - 3] << 16)
         | ((long)val[MAX_ARR_LEN - 4] <<  8)
         | ((long)val[MAX_ARR_LEN - 1]      );
  }

  @Override
  public float floatValue(){
    /* TODO: Possibly a bad conversion. */
    return (float)intValue();
  }

  @Override
  public double doubleValue(){
    /* TODO: Possibly a bad conversion. */
    return (double)longValue();
  }

  @Override
  public String toString(){
    return toString(val);
  }

  @Override
  public int hashCode(){
    /* TODO: Should probably hash the entire number. */
    return intValue();
  }

  @Override
  public boolean equals(Object obj){
    return obj != null         &&
           obj instanceof I512 &&
           compare(val, ((I512)obj).toByteArray()) == 0;
  }

//  public static I512 getInteger(String nm){
//    return null; // TODO
//  }

//  public static I512 getInteger(String nm, byte[] val){
//    return null; // TODO
//  }

//  public static I512 getInteger(String nm, I512 val){
//    return null; // TODO
//  }

//  public static I512 decode(String nm) throws NumberFormatException{
//    return null; // TODO
//  }

  @Override
  public int compareTo(I512 anotherInteger){
    return compare(val, anotherInteger.toByteArray());
  }

  public static int compare(byte[] x, byte[] y){
    return (new BigInteger(x)).compareTo(new BigInteger(y));
  }

//  public static int highestOneBit(byte[] i){
//    return 0; // TODO
//  }

//  public static int lowestOneBit(byte[] i){
//    return 0; // TODO
//  }

//  public static int numberOfLeadingZeros(byte[] i){
//    return 0; // TODO
//  }

//  public static int numberOfTrailingZeros(byte[] i){
//    return 0; // TODO
//  }

//  public static int bitCount(byte[] i){
//    return 0; // TODO
//  }

//  public static int rotateLeft(byte[] i, int distance){
//    return 0; // TODO
//  }

//  public static int rotateRight(byte[] i, int distance){
//    return 0; // TODO
//  }

//  public static byte[] reverse(byte[] i){
//    return null; // TODO
//  }

//  public static int signum(byte[] i){
//    return 0; // TODO
//  }

//  public static byte[] reverseBytes(byte[] i){
//    return null;
//  }

  /**
   * toByteArray()
   *
   * Access to the internal byte array of this object, untrimmed of leading zeros.
   *
   * @return The byte array for this object.
   **/
  public byte[] toByteArray(){
    return val;
  }

  /**
   * assurt()
   *
   * Assert that the result was as expected.
   *
   * @param e The expected value.
   * @param r The result from the action.
   * @param stats The current statistics from running the tests.
   * @param msg A short string explaining the test.
   * @return The updated statistics from running the tests.
   **/
  private static int[] assurt(byte[] e, I512 r, int[] stats, String msg){
    boolean equal = true;
    for(int x = 0; x < 64; x++){
      if(r.toByteArray()[x] != e[x]){
        equal = false;
        break;
      }
    }
    stats = assurt(equal, stats, msg);;
    if(!equal){
      String be = "{";
      String br = "{";
      for(int x = 0; x < 64; x++){
        be += e[x] + ",";
        br += r.toByteArray()[x] + ",";
      }
      be += "}";
      br += "}";
      System.err.println("e -> " + be);
      System.err.println("r -> " + br);
    }
    return stats;
  }

  /**
   * assurt()
   *
   * Assert that the result was as expected.
   *
   * @param equal True if the result passes, otherwise false.
   * @param stats The current statistics from running the tests.
   * @param msg A short string explaining the test.
   * @return The updated statistics from running the tests.
   **/
  private static int[] assurt(boolean equal, int[] stats, String msg){
    System.err.print("[" + stats[0] + "]  ");
    stats[0]++;
    if(equal){
      stats[1]++;
      System.err.print("[ OK ]        ");
    }else{
      System.err.print("      [FAIL]  ");
    }
    System.err.println(msg);
    return stats;
  }

  /**
   * test()
   *
   * Perform tests to make sure the class is behaving correctly. Results of the
   * test are printed to standard error output. The result of the tests if then
   * returned from the function.
   *
   * @return True of success, otherwise false.
   **/
  public static boolean test(){
    int[] stats = new int[]{ 0, 0 };
    /* Run tests */
    stats = assurt(
      new byte[]{
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
      }, new I512(new byte[]{ 0 }), stats, "Blank test"
    );
    stats = assurt(
      new byte[]{
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1
      }, new I512(new byte[]{ 1 }), stats, "One test"
    );
    stats = assurt(
      new byte[]{
        127,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,127
      }, new I512(new byte[]{
        127,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,127
      }), stats, "Long test"
    );
    stats = assurt(
      new byte[]{
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1
      }, new I512("1"), stats, "One from string"
    );
    stats = assurt(
      (new I512("1")).compareTo(new I512("1")) == 0, stats, "Compare equal"
    );
    stats = assurt(
      (new I512("2")).compareTo(new I512("1")) != 0, stats, "Compare not equal"
    );
    stats = assurt(
      (new I512("1")).hashCode() == (new I512("1")).hashCode(),
      stats, "Compare equal hashes"
    );
    stats = assurt(
      (new I512("1")).hashCode() != (new I512("2")).hashCode(),
      stats, "Compare not equal hashes"
    );
    /* Deal with results */
    System.err.println("Passed " + stats[1] + " out of " + stats[0] + " tests");
    System.err.println(stats[0] == stats[1] ? "[[ SUCCESS ]]" : "[[ FAILURE ]]");
    return stats[0] == stats[1];
  }
}
