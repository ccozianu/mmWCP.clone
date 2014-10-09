package home.costin.util;

import java.util.*;

/**
 * Utility holder of a key-value pair associating a key with a value
 * The key should be a <code>Comparable</code> object
 * otherwise the KVPair association would be of no much use
 */
public final class NVPair implements java.io.Serializable , Comparable, Map.Entry
{
	private String name;
	private Object value;

public NVPair(String name, Object value)
{
	if (name== null) throw new IllegalArgumentException("NVPair isn't allowed a null name value !");
	this.name= name;
	this.value= value;
}
public int compareTo(java.lang.Object other)
{
	return name.compareTo(((NVPair) other).name);
}
public boolean equals(Object x)
{
	NVPair x1= (NVPair) x;
   if (!x1.name.equals(name)) return false;
   return (value==null ? 
	   								x1.value==null :
	   								value.equals(x1.value));
}
public Object getKey()
{
	return name;
}
public String getName()
{
	return name;
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
	public String toString()
	{
		return "("+name+','+value+')';
	}
}
