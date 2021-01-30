package b.ds;

import java.lang.reflect.Field;

/**
 * Str.java
 *
 * A super simple and fast string implementation.
 **/
public final class Str implements java.io.Serializable, Comparable<Str>, CharSequence{
  private static Field stringField;

  static{
    try{
      stringField = String.class.getDeclaredField("value");
    }catch(NoSuchFieldException e){
      e.printStackTrace();
    }
    stringField.setAccessible(true);
  }

  private char[][] data;
  private int cap;
  private int idx;
  private int len;

  public Str(int capacity){
    data = new char[capacity][];
    cap = capacity;
    idx = 0;
    len = 0;
  }

  public Str(String s){
    this(1);
    try{
      data[idx] = (char[])(stringField.get(s));
    }catch(IllegalAccessException e){
      e.printStackTrace();
    }
    len = data[idx].length;
    ++idx;
  }

  public Str(char[] data){
    this(1);
    this.data[idx] = data;
    len = this.data[idx].length;
    ++idx;
  }

  public Str(char[] data, int beginIndex, int endIndex){
    this(1);
    len = endIndex - beginIndex;
    this.data[idx] = new char[len];
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
      data[idx] = (char[])(stringField.get(s));
    }catch(IllegalAccessException e){
      e.printStackTrace();
    }
    len += data[idx].length;
    ++idx;
    return this;
  }

  public Str append(char[] s){
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
    char[] val = new char[endIndex - beginIndex];
    for(int i = 0; i < endIndex - beginIndex; i++){
      val[i] = charAt(i);
    }
    return new Str(val, beginIndex, endIndex);
  }

  public char charAt(int index){
    int c = 0;
    for(int i = 0; i < idx; i++){
      if(c + data[i].length > index){
        return data[i][index - c];
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
    char[][] d = new char[capacity][];
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
    byte[] r = new byte[len];
    for(int i = 0; i < idx; i++){
      for(int x = 0; x < data[i].length; x++){
        r[z++] = (byte)(data[i][x] & 0b01111111);
      }
    }
    return r;
  }

  public StringBuilder toStringBuilder(){
    StringBuilder sb = new StringBuilder(len);
    for(int i = 0; i < idx; i++){
      sb.append(data[i]);
    }
    return sb;
  }

  @Override
  public String toString(){
    return toStringBuilder().toString();
  }
}
