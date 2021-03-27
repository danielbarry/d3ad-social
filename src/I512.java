package b.ds;

import java.math.BigInteger;
import java.util.Random;

/**
 * I512.java
 *
 * A class designed to handle unsigned 512 bit integers as fast as possible,
 * for the purpose of storing and processing hashes.
 **/
public final class I512 extends Number implements Comparable<I512>{
  public static final int SIZE = 512;

  private static final int STR_LUT_OFF = 33;
  private static final int[] STR_HEX_LUT = new int[]{
  /* !   "   #   $   %   &    '  (   )   *   +   ,   -   .   / */
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
  /* 0  1  2  3  4  5  6  7  8  9 */
     0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
  /* :   ;   <   =   >   ?   @ */
    -1, -1, -1, -1, -1, -1, -1,
  /* A   B   C   D   E   F   G   H   I   J   K   L   M   N   O   P   Q   R   S   T   U   V   W   X   Y   Z */
    10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
  /* [   \   ]   ^   _   ` */
    -1, -1, -1, -1, -1, -1,
  /* a   b   c   d   e   f   g   h   i   j   k   l   m   n   o   p   q   r   s   t   u   v   w   x   y   z */
    10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35,
  };
  private static final int[] STR_BASE64_LUT = new int[]{
  /* !   "   #   $   %   &    '  (   )   *   +   ,   -   .   / */
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1,
  /* 0   1   2   3   4   5   6   7   8   9 */
    52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
  /* :   ;   <   =   >   ?   @ */
    -1, -1, -1,  0, -1, -1, -1,
  /* A   B   C   D   E   F   G   H   I   J   K   L   M   N   O   P   Q   R   S   T   U   V   W   X   Y   Z */
     0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
  /* [   \   ]   ^   _   ` */
    -1, -1, -1, -1, 63, -1,
  /* a   b   c   d   e   f   g   h   i   j   k   l   m   n   o   p   q   r   s   t   u   v   w   x   y   z */
    26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
  };
  private static final char[] HEX =
    {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  /* BASE64 using RFC3501 encoding */
  private static final char[] BASE64 = {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
    'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
    'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
  };
  private static final int BITS_BYTE   = 8;
  private static final int BITS_HEX    = 4;
  private static final int BITS_BASE64 = 6;

  public static final int MAX_BYTE_ARR_LEN   = (SIZE / BITS_BYTE  ) + (SIZE % BITS_BYTE   > 0 ? 1 : 0);
  public static final int MAX_STR_HEX_LEN    = (SIZE / BITS_HEX   ) + (SIZE % BITS_HEX    > 0 ? 1 : 0);
  public static final int MAX_STR_BASE64_LEN = (SIZE / BITS_BASE64) + (SIZE % BITS_BASE64 > 0 ? 1 : 0);

  private byte[] val = new byte[MAX_BYTE_ARR_LEN];

  /**
   * I512()
   *
   * Constructs a newly allocated I512 object that represents the specified byte[] value.
   *
   * @param value The value to be represented by the I512 object.
   **/
  public I512(byte[] value) throws NumberFormatException{
    /* NOTE: BigInteger adds an extra byte on the front sometimes. */
    if(value == null || value.length > MAX_BYTE_ARR_LEN + 1){
      throw new NumberFormatException(
        "Unable to create I512 from supplied value of length " +
        (value != null ? value.length : -1)
      );
    }else{
      int len = value.length <= MAX_BYTE_ARR_LEN ? value.length : MAX_BYTE_ARR_LEN;
      if(value.length <= MAX_BYTE_ARR_LEN){
        /* Copy array into lower bytes */
        System.arraycopy(value, 0, val, MAX_BYTE_ARR_LEN - value.length, len);
      }else{
        /* Skip additional byte(s) */
        System.arraycopy(value, value.length - MAX_BYTE_ARR_LEN, val, 0, len);
      }
    }
  }

  /**
   * I512()
   *
   * Constructs a newly allocated Integer object that represents the byte[]
   * value indicated by the String parameter. The string is converted based on
   * the length.
   *
   * @param s The String to be converted to an I512.
   **/
  public I512(String s) throws NumberFormatException{
    this(parseInt(s, detectBase(s)));
  }

  /**
   * detectBase()
   *
   * Detect the base for a given String.
   *
   * @param s The String to detect the base for. NOTE: This is currently a very
   * simple base detection.
   * @return The detected base.
   **/
  private static int detectBase(String s){
    switch(s.length()){
      case MAX_STR_HEX_LEN :
      case MAX_STR_HEX_LEN + 1 :
        return 16;
      case MAX_STR_BASE64_LEN :
      case MAX_STR_BASE64_LEN + 1 :
      default :
        return 64;
    }
  }

  public static String toString(byte[] i, int radix){
    switch(radix){
      case 16 :
        return toHexString(i);
      case 64 :
        return toBase64String(i);
      default :
        return ((new BigInteger(i)).abs()).toString(radix);
    }
  }

  public static String toBase64String(byte[] i){
    char[] s = new char[(int)(((((double)i.length * 8.0)) / 6.0) + 0.9999999999999999)];
    /* String destination pointer */
    int z = s.length;
    /* Byte array source pointer */
    int x = i.length;
    /* Pre-declare used variables */
    int v0, v1, v2;
    /* Quad source for triple target set */
    while(z >= 4){
      v0 = i[--x] & 0xFF;
      v1 = i[--x] & 0xFF;
      v2 = i[--x] & 0xFF;
      s[--z] = BASE64[(            (v0     )) & 0b111111];
      s[--z] = BASE64[((v1 << 2) | (v0 >> 6)) & 0b111111];
      s[--z] = BASE64[((v2 << 4) | (v1 >> 4)) & 0b111111];
      s[--z] = BASE64[((v2 >> 2)            ) & 0b111111];
    }
    /* Handle last parts */
    switch(z){
      case 0 :
        /* Do nothing */
        break;
      case 1 :
        v0 = x > 0 ? i[--x] & 0xFF : 0;
        s[--z] = BASE64[(            (v0     )) & 0b111111];
        break;
      case 2 :
        v0 = x > 0 ? i[--x] & 0xFF : 0;
        v1 = x > 0 ? i[--x] & 0xFF : 0;
        s[--z] = BASE64[(            (v0     )) & 0b111111];
        s[--z] = BASE64[((v1 << 2) | (v0 >> 6)) & 0b111111];
        break;
      case 3 :
        v0 = x > 0 ? i[--x] & 0xFF : 0;
        v1 = x > 0 ? i[--x] & 0xFF : 0;
        v2 = x > 0 ? i[--x] & 0xFF : 0;
        s[--z] = BASE64[(            (v0     )) & 0b111111];
        s[--z] = BASE64[((v1 << 2) | (v0 >> 6)) & 0b111111];
        s[--z] = BASE64[((v2 << 4) | (v1 >> 4)) & 0b111111];
        break;
      default :
        Utils.warn("Unhandled parse case for " + z);
        break;
    }
    return new String(s);
  }

  public static String toHexString(byte[] i){
    char[] s = new char[2 * i.length];
    int z = 0;
    for(int x = 0; x < i.length; x++){
      s[z++] = HEX[(0xF0 & i[x]) >>> 4];
      s[z++] = HEX[(0x0F & i[x])      ];
    }
    return new String(s);
  }

  public static String toOctalString(byte[] i){
    return toString(i, 8);
  }

  public static String toBinaryString(byte[] i){
    return toString(i, 2);
  }

  public static String toDecimalString(byte[] i){
    return toString(i, 10);
  }

  /**
   * parseInt()
   *
   * Parse an integer from string to a byte array, with a given radix. Base 16
   * hex conversion is sped up by avoiding branching. This function uses
   * BigInteger for other conversions.
   *
   * @param s The string to be converted.
   * @param radix The base to convert the string from.
   * @return A byte array containing the converted data.
   **/
  public static byte[] parseInt(String s, int radix) throws NumberFormatException{
    /* Check for NULL */
    if(s == null){
      return null;
    }
    /* Pre-declare variables because Java doesn't understand scope */
    int sLen = s.length();
    byte[] r;
    int i;
    int x;
    int z;
    /* Check if we can perform faster conversion */
    switch(radix){
      case 16 :
        /* NOTE: We assume the length is correct and the format is simple. */
        r = new byte[(sLen / 2) + (sLen % 2)];
        i = r.length - 1;
        z = 0;
        x = sLen;
        while(--x >= 0){
          r[i] |= (byte)(STR_HEX_LUT[s.charAt(x) - STR_LUT_OFF] << ((z % 2) << 2));
          i -= z++ % 2;
        }
        return r;
      case 64 :
        r = new byte[(int)(((double)(sLen * BITS_BASE64) + 7.9999999999999999) / BITS_BYTE)];
        /* String source pointer */
        i = sLen;
        /* Byte array target pointer */
        x = r.length;
        /* Pre-declare used variables */
        int v0, v1, v2, v3;
        /* Quad source for triple target set */
        while(i >= 4){
          v0 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
          v1 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
          v2 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
          v3 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
          r[--x] = (byte)((v1 << 6) | (v0     ));
          r[--x] = (byte)((v2 << 4) | (v1 >> 2));
          r[--x] = (byte)((v3 << 2) | (v2 >> 4));
        }
        /* Handle last parts */
        switch(i){
          case 0 :
            /* Do nothing */
            break;
          case 1 :
            v0 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
            r[--x] = (byte)(v0);
            break;
          case 2 :
            v0 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
            v1 = STR_BASE64_LUT[s.charAt(--i) - STR_LUT_OFF];
            r[--x] = (byte)((v1 << 6) | (v0     ));
            r[--x] = (byte)(            (v1 >> 2));
            break;
          default :
            Utils.warn("Unhandled parse case for " + i);
            break;
        }
        return r;
      default :
        try{
          return ((new BigInteger(s, radix)).abs()).toByteArray();
        }catch(NumberFormatException e){
          return new byte[]{};
        }
    }
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
    return (byte)val[MAX_BYTE_ARR_LEN - 1];
  }

  @Override
  public short shortValue(){
    return (short)(
               ((short)val[MAX_BYTE_ARR_LEN - 4] <<  8)
             | ((short)val[MAX_BYTE_ARR_LEN - 1]      )
           );
  }

  @Override
  public int intValue(){
    return ((int)val[MAX_BYTE_ARR_LEN - 4] << 24)
         | ((int)val[MAX_BYTE_ARR_LEN - 3] << 16)
         | ((int)val[MAX_BYTE_ARR_LEN - 4] <<  8)
         | ((int)val[MAX_BYTE_ARR_LEN - 1]      );
  }

  @Override
  public long longValue(){
    return ((long)val[MAX_BYTE_ARR_LEN - 8] << 56)
         | ((long)val[MAX_BYTE_ARR_LEN - 7] << 48)
         | ((long)val[MAX_BYTE_ARR_LEN - 6] << 40)
         | ((long)val[MAX_BYTE_ARR_LEN - 5] << 32)
         | ((long)val[MAX_BYTE_ARR_LEN - 4] << 24)
         | ((long)val[MAX_BYTE_ARR_LEN - 3] << 16)
         | ((long)val[MAX_BYTE_ARR_LEN - 4] <<  8)
         | ((long)val[MAX_BYTE_ARR_LEN - 1]      );
  }

  @Override
  public float floatValue(){
    /* NOTE: Meaningless conversion anyway. */
    return (float)longValue();
  }

  @Override
  public double doubleValue(){
    /* NOTE: Meaningless conversion anyway. */
    return (double)longValue();
  }

  @Override
  public String toString(){
    return toBase64String(val);
  }

  @Override
  public int hashCode(){
    /* TODO: Should probably hash the entire number. */
    return intValue();
  }

  @Override
  public boolean equals(Object obj){
    if(obj == this){
      return true;
    }
    if(obj == null || !(obj instanceof I512)){
      return false;
    }
    byte[] x = val;
    byte[] y = ((I512)obj).toByteArray();
    return ((x[ 0] << 24 | x[ 1] << 16 | x[ 2] << 8 | x[ 3])
        ==  (y[ 0] << 24 | y[ 1] << 16 | y[ 2] << 8 | y[ 3]))
        && ((x[ 4] << 24 | x[ 5] << 16 | x[ 6] << 8 | x[ 7])
        ==  (y[ 4] << 24 | y[ 5] << 16 | y[ 6] << 8 | y[ 7]))
        && ((x[ 8] << 24 | x[ 9] << 16 | x[10] << 8 | x[11])
        ==  (y[ 8] << 24 | y[ 9] << 16 | y[10] << 8 | y[11]))
        && ((x[12] << 24 | x[13] << 16 | x[14] << 8 | x[15])
        ==  (y[12] << 24 | y[13] << 16 | y[14] << 8 | y[15]))
        && ((x[16] << 24 | x[17] << 16 | x[18] << 8 | x[19])
        ==  (y[16] << 24 | y[17] << 16 | y[18] << 8 | y[19]))
        && ((x[20] << 24 | x[21] << 16 | x[22] << 8 | x[23])
        ==  (y[20] << 24 | y[21] << 16 | y[22] << 8 | y[23]))
        && ((x[24] << 24 | x[25] << 16 | x[26] << 8 | x[27])
        ==  (y[24] << 24 | y[25] << 16 | y[26] << 8 | y[27]))
        && ((x[28] << 24 | x[29] << 16 | x[30] << 8 | x[31])
        ==  (y[28] << 24 | y[29] << 16 | y[30] << 8 | y[31]))
        && ((x[32] << 24 | x[33] << 16 | x[34] << 8 | x[35])
        ==  (y[32] << 24 | y[33] << 16 | y[34] << 8 | y[35]))
        && ((x[36] << 24 | x[37] << 16 | x[38] << 8 | x[39])
        ==  (y[36] << 24 | y[37] << 16 | y[38] << 8 | y[39]))
        && ((x[40] << 24 | x[41] << 16 | x[42] << 8 | x[43])
        ==  (y[40] << 24 | y[41] << 16 | y[42] << 8 | y[43]))
        && ((x[44] << 24 | x[45] << 16 | x[46] << 8 | x[47])
        ==  (y[44] << 24 | y[45] << 16 | y[46] << 8 | y[47]))
        && ((x[48] << 24 | x[49] << 16 | x[50] << 8 | x[51])
        ==  (y[48] << 24 | y[49] << 16 | y[50] << 8 | y[51]))
        && ((x[52] << 24 | x[53] << 16 | x[54] << 8 | x[55])
        ==  (y[52] << 24 | y[53] << 16 | y[54] << 8 | y[55]))
        && ((x[56] << 24 | x[57] << 16 | x[58] << 8 | x[59])
        ==  (y[56] << 24 | y[57] << 16 | y[58] << 8 | y[59]))
        && ((x[60] << 24 | x[61] << 16 | x[62] << 8 | x[63])
        ==  (y[60] << 24 | y[61] << 16 | y[62] << 8 | y[63]));
  }

  @Override
  public int compareTo(I512 anotherInteger){
    return compare(val, anotherInteger.toByteArray());
  }

  /**
   * compare()
   *
   * Compare to byte arrays and determine if less than, equal or greater than
   * one another. This function attempts to use alignment to speed-up searching
   * by limiting checks branching.
   *
   * @param x The first array to be checked.
   * @param y The second array to be checked.
   * @return -1 if x is less than y, 0 is x is equal to y and 1 if x is greater
   * than y.
   **/
  public static int compare(byte[] x, byte[] y){
    int sx = 0;
    int sy = 0;
    /* If one is larger than the other, make sure it starts with zeros */
    if(x.length != y.length){
      if(x.length > y.length){
        while(sx < x.length - y.length){
          if(x[sx++] != 0){
            return 1;
          }
        }
      }else{
        while(sy < y.length - x.length){
          if(y[sy++] != 0){
            return -1;
          }
        }
      }
    }
    /* Check and align to four bytes */
    while((x.length - sx) % 4 != 0 && sx < x.length){
      if(x[sx] != y[sy]){
        return x[sx] < y[sy] ? -1 : 1;
      }
      ++sx;
      ++sy;
    }
    /* Now check aligned bulk */
    while(sx < x.length){
      /* NOTE: Prevent too much branching by doing single op check */
      int a = x[sx++] << 24 | x[sx++] << 16 | x[sx++] << 8 | x[sx++];
      int b = y[sy++] << 24 | y[sy++] << 16 | y[sy++] << 8 | y[sy++];
      if(a != b){
        return a < b ? -1 : 1;
      }
    }
    return 0;
  }

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
    System.err.print("[" + stats[0] + "]\t");
    stats[0]++;
    if(equal){
      stats[1]++;
      System.err.print("[ OK ]      \t");
    }else{
      System.err.print("      [FAIL]\t");
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
      }, new I512("B"), stats, "One from string"
    );
    stats = assurt(
      (new I512("B")).compareTo(new I512("B")) == 0, stats, "Compare equal"
    );
    stats = assurt(
      (new I512("B")).compareTo(new I512("C")) != 0, stats, "Compare not equal"
    );
    stats = assurt(
      (new I512("B")).hashCode() == (new I512("B")).hashCode(),
      stats, "Compare equal hashes"
    );
    stats = assurt(
      (new I512("B")).hashCode() != (new I512("C")).hashCode(),
      stats, "Compare not equal hashes"
    );
    stats = assurt(
      I512.toString(new byte[]{ 0 }, 64).equals("AA"),
      stats, "Zero string (BASE64)"
    );
    stats = assurt(
      I512.toString(new byte[]{ 1 }, 64).equals("AB"),
      stats, "One string (BASE64)"
    );
    stats = assurt(
      I512.toString(new byte[]{ 32 }, 64).equals("Ag"),
      stats, "Upper bit string (BASE64)"
    );
    stats = assurt(
      I512.toString(new byte[]{ 64 }, 64).equals("BA"),
      stats, "Next bit string (BASE64)"
    );
    stats = assurt(
      I512.toString(new byte[]{ 1, 1 }, 64).equals("AEB"),
      stats, "Dual bytes string (BASE64)"
    );
    stats = assurt(
      new byte[]{
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
      }, new I512("A"), stats, "Blank test (BASE64)"
    );
    stats = assurt(
      new byte[]{
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0
      }, new I512("AAAAAAAA"), stats, "Blank redundant test (BASE64)"
    );
    stats = assurt(
      new byte[]{
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
        0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1
      }, new I512("B"), stats, "One test (BASE64)"
    );
    String s = "AwRRHXnSNk_XaP2lLTFnc5D5QWsOZjrUa_yNxhRDKCH05iDvI6QLY33Tt8sUGmvGAyJ9ybfnOCqNHZNqODx-V7";
    stats = assurt(
      I512.compare((new I512(s)).toString().getBytes(), s.getBytes()) == 0, stats, "Random test (BASE64)"
    );
    /* Deal with results */
    System.err.println("Passed " + stats[1] + " out of " + stats[0] + " tests");
    System.err.println(stats[0] == stats[1] ? "[[ SUCCESS ]]" : "[[ FAILURE ]]");
    return stats[0] == stats[1];
  }

  /**
   * perf()
   *
   * Check the performance of some key functions to ensure they are as fast as
   * we believe they are.
   **/
  public static void perf(){
    int BASE = 16;
    int TARGET_RUNS = 1000000;
    Random r = new Random();
    /* parseInt() - BigInteger */
    long parseIntBigInt = System.currentTimeMillis();
    for(int x = 0; x < TARGET_RUNS; x++){
      BigInteger bi = (new BigInteger(512, r)).abs();
      String bs = bi.toString(BASE);
      byte[] a = ((new BigInteger(bs, BASE)).abs()).toByteArray();
      if(compare(a, bi.toByteArray()) != 0){
        throw new NumberFormatException(toBase64String(a) + " != " + bi.toString(BASE));
      }
    }
    parseIntBigInt = System.currentTimeMillis() - parseIntBigInt;
    /* parseInt() - custom */
    long parseIntCustom = System.currentTimeMillis();
    for(int x = 0; x < TARGET_RUNS; x++){
      BigInteger bi = (new BigInteger(512, r)).abs();
      String bs = bi.toString(BASE);
      byte[] a = parseInt(bs, BASE);
      if(compare(a, bi.toByteArray()) != 0){
        throw new NumberFormatException(toBase64String(a) + " != " + bi.toString(BASE));
      }
    }
    parseIntCustom = System.currentTimeMillis() - parseIntCustom;
    /* compare() - BigInteger */
    long compareBigInt = System.currentTimeMillis();
    for(int x = 0; x < TARGET_RUNS; x++){
      BigInteger ai = (new BigInteger(512, r)).abs();
      BigInteger bi = (new BigInteger(512, r)).abs();
      if((new BigInteger(ai.toByteArray())).compareTo(new BigInteger(bi.toByteArray())) == 0
      || (new BigInteger(ai.toByteArray())).compareTo(new BigInteger(ai.toByteArray())) != 0
      || (new BigInteger(bi.toByteArray())).compareTo(new BigInteger(bi.toByteArray())) != 0){
        throw new NumberFormatException(ai.toString(BASE) + " != " + bi.toString(BASE));
      }
    }
    compareBigInt = System.currentTimeMillis() - compareBigInt;
    /* compare() - custom */
    long compareCustom = System.currentTimeMillis();
    for(int x = 0; x < TARGET_RUNS; x++){
      BigInteger ai = (new BigInteger(512, r)).abs();
      BigInteger bi = (new BigInteger(512, r)).abs();
      if(compare(ai.toByteArray(), bi.toByteArray()) == 0
      || compare(ai.toByteArray(), ai.toByteArray()) != 0
      || compare(bi.toByteArray(), bi.toByteArray()) != 0){
        throw new NumberFormatException(ai.toString(BASE) + " != " + bi.toString(BASE));
      }
    }
    compareCustom = System.currentTimeMillis() - compareCustom;
    /* equals() - BigInteger */
    long equalsBigInt = System.currentTimeMillis();
    for(int x = 0; x < TARGET_RUNS; x++){
      BigInteger ai = (new BigInteger(512, r)).abs();
      I512 az = new I512(ai.toByteArray());
      BigInteger bi = (new BigInteger(256, r)).abs().add(ai);
      I512 bz = new I512(bi.toByteArray());
      if((ai.equals(bi))
      ||!(ai.equals(ai))
      ||!(bi.equals(bi))){
        throw new NumberFormatException(ai.toString(BASE) + " != " + bi.toString(BASE));
      }
    }
    equalsBigInt = System.currentTimeMillis() - equalsBigInt;
    /* equals() - custom */
    long equalsCustom = System.currentTimeMillis();
    for(int x = 0; x < TARGET_RUNS; x++){
      BigInteger ai = (new BigInteger(512, r)).abs();
      I512 az = new I512(ai.toByteArray());
      BigInteger bi = (new BigInteger(256, r)).abs().add(ai);
      I512 bz = new I512(bi.toByteArray());
      if((az.equals(bz))
      ||!(az.equals(az))
      ||!(bz.equals(bz))){
        throw new NumberFormatException(ai.toString(BASE) + " != " + bi.toString(BASE));
      }
    }
    equalsCustom = System.currentTimeMillis() - equalsCustom;
    /* Print results */
    System.err.println("Default"      + "\t|" + "Custom"       + "\t|Description");
    System.err.println("--------"     +   "|" + "-------"      +   "|----------------");
    System.err.println(parseIntBigInt + "\t|" + parseIntCustom + "\t|parseInt()");
    System.err.println(compareBigInt  + "\t|" + compareCustom  + "\t|compare()");
    System.err.println(equalsBigInt   + "\t|" + equalsCustom   + "\t|equals()");
    System.err.println("[[ FINISHED ]]");
  }
}
