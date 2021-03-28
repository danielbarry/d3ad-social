package b.ds;

import java.lang.reflect.Field;

/**
 * Str.java
 *
 * A super simple and fast string implementation. It assumes ASCII (7 bit
 * characters) are used and makes uses of Java reflection to speed-up the
 * retrieval of String data.
 *
 * NOTE: This class requires Java 9 or above due to some changes in the way in
 * which Strings are handled internally.
 **/
public final class Str implements java.io.Serializable, Comparable<Str>, CharSequence{
  private static Field stringField;
  private static int padSkip;

  static{
    try{
      stringField = String.class.getDeclaredField("value");
    }catch(NoSuchFieldException e){
      e.printStackTrace();
    }
    stringField.setAccessible(true);
    try{
      byte[] d = (byte[])(stringField.get("#"));
      padSkip = d.length;
    }catch(IllegalAccessException e){
      e.printStackTrace();
    }
  }

  private byte[][] data;
  private int cap;
  private int idx;
  private int len;

  public Str(int capacity){
    data = new byte[capacity][];
    cap = capacity;
    idx = 0;
    len = 0;
  }

  public Str(String s){
    this(1);
    try{
      data[idx] = (byte[])(stringField.get(s));
    }catch(IllegalAccessException e){
      e.printStackTrace();
    }
    len = data[idx].length;
    ++idx;
  }

  public Str(byte[] data){
    this(1);
    this.data[idx] = data;
    len = this.data[idx].length;
    ++idx;
  }

  public Str(byte[] data, int beginIndex, int endIndex){
    this(1);
    len = endIndex - beginIndex;
    this.data[idx] = new byte[len];
    System.arraycopy(data, beginIndex, this.data[idx], 0, len);
    ++idx;
  }

  public Str append(String s){
    /* Check capacity */
    if(idx >= cap){
      Utils.warn("Out of capacity, generating more");
      expand(cap * 2);
    }
    /* Add the array */
    try{
      data[idx] = (byte[])(stringField.get(s));
    }catch(IllegalAccessException e){
      e.printStackTrace();
    }
    len += data[idx].length;
    ++idx;
    return this;
  }

  public Str append(byte[] s){
    /* Check capacity */
    if(idx >= cap){
      Utils.warn("Out of capacity, generating more");
      expand(cap * 2);
    }
    /* Add the array */
    data[idx] = s;
    len += data[idx].length;
    ++idx;
    return this;
  }

  public int compareTo(Str that){
    if(this.len != that.len){
      return this.len < that.len ? -1 : 1;
    }
    /* Setup count */
    int z = 0;
    /* Setup indexes */
    int a = 0;
    int b = 0;
    /* Setup offsets */
    int x = 0;
    int y = 0;
    while(z++ < this.len){
      /* Perform test */
      if(this.data[a][x] != that.data[b][y]){
        return this.data[a][x] < that.data[b][y] ? -1 : 1;
      }
      /* Increment this */
      if(++x >= this.data[a].length){
        ++a;
        x = 0;
      }
      /* Increment that */
      if(++y >= this.data[b].length){
        ++b;
        y = 0;
      }
    }
    return 0;
  }

  public CharSequence subSequence(int beginIndex, int endIndex){
    return this.substring(beginIndex, endIndex);
  }

  public Str substring(int beginIndex, int endIndex){
    byte[] val = new byte[endIndex - beginIndex];
    for(int i = 0; i < endIndex - beginIndex; i++){
      val[i] = (byte)(charAt(i));
    }
    return new Str(val, beginIndex, endIndex);
  }

  public char charAt(int index){
    int c = 0;
    for(int i = 0; i < idx; i++){
      if(c + data[i].length > index){
        return (char)(data[i][index - c]);
      }else{
        c += data[i].length;
      }
    }
    throw new IndexOutOfBoundsException("Index " + index + " is out of bounds!");
  }

  /**
   * expand()
   *
   * Expand the capacity of the array to a new value.
   *
   * @param capacity The new value of the capacity.
   **/
  public void expand(int capacity){
    /* Make sure it is actually expanding */
    if(capacity < cap){
      return;
    }
    /* Perform expansion */
    byte [][] d = new byte[capacity][];
    System.arraycopy(data, 0, d, 0, idx);
    data = d;
    cap = capacity;
  }

  /**
   * capacity()
   *
   * Check the capacity of this object.
   *
   * @return The capacity of this objects.
   **/
  public int capacity(){
    return cap;
  }

  /**
   * remaining()
   *
   * Check the remaining capacity of this objects.
   *
   * @return The remaining capacity for this object.
   **/
  public int remaining(){
    return cap - idx;
  }

  /**
   * length()
   *
   * Returns the current length of the array.
   *
   * @return The current length of the array.
   **/
  public int length(){
    return len;
  }

  /**
   * toByteArray()
   *
   * Encodes the string as 7 bit ASCII. This ignores all encoding, so be
   * CAREFUL.
   *
   * @return The convert byte array.
   **/
  public byte[] toByteArray(){
    int z = 0;
    byte[] r = new byte[len / padSkip];
    for(int i = 0; i < idx; i++){
      for(int x = 0; x < data[i].length; x += padSkip){
        r[z++] = data[i][x];
      }
    }
    return r;
  }

  @Override
  public String toString(){
    return new String(toByteArray());
  }
}
