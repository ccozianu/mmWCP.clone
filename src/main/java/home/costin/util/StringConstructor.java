/*
 * Created on May 17, 2004
 *
 */
package home.costin.util;

/**
 * @author ccozianu
 * @deprecated this class was initiated way back when Java didn't have StringBuilders
 */
public class StringConstructor {

	char buffer[];
	int length;
	int position=0;
	
	public StringConstructor() { this(64); }
	
	public StringConstructor( int capacity) {
		buffer= new char[capacity];
		length= capacity;
		}
		
	public void upsize(int delta) {
		char[] temp = new char[length+delta];
		System.arraycopy(buffer,0,temp,0,position); 
		length= temp.length; 
		buffer= temp;
	}
	
	public StringConstructor append(char c) {
		if (position==length) {
			upsize(length);	
		}
		buffer[position++]=c;
		return(this);
	}
	
	public StringConstructor append(Object o) {
		String s= o.toString();
		for (int i=0;i<s.length();i++) append(s.charAt(i));
		return this;
	}
	
	public void reset() 
		{ position=0; }
	
	public String toString() 
		{ return new String(buffer,0,position); }
}
