package home.costin.util;

import java.util.*;

/**
 * Utility holder of a key-value pair associating a key with a value
 * The key should be a <code>Comparable</code> object
 * otherwise the KVPair association would be of no much use
 */
public final class KVPair implements java.io.Serializable , Comparable, Map.Entry
{
	private Comparable key;
	private Object value;
public KVPair(Comparable key, Object value)
{
	if (key== null) throw new IllegalArgumentException("KVPair isn't allowed a null key value !");
	this.key= key;
	this.value= value;
}
public int compareTo(java.lang.Object other)
{
	return key.compareTo(((KVPair) other).key);
}
public boolean equals(Object x)
{
   KVPair x1= (KVPair) x;
   if (!x1.key.equals(key)) return false;
   return (value==null ? 
	   								x1.value==null :
	   								value.equals(x1.value));
}
public Object getKey()
{
	return key;
}
public Object getValue()
{
	return value;
}
public Object setValue(Object value)
{
	Object oldValue=this.value;
	this.value=value;
	return oldValue;
}
}
