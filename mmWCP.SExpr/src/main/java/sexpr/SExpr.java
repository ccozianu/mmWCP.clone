/*
 * Created on Apr 14, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package sexpr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;

import sexpr.util.Base64;

/**
 * @author ccozianu
 *
 */
public abstract class SExpr implements Iterable<SExpr> {
	
	SExpr meta= null;
	private SExpr(SExpr meta_) {
		meta= meta_;
		}

	public static final SExpr NIL= new NIL_();

	private static class NIL_ extends SExpr {
		
			NIL_() { super(null); }
				 
		public String stringValue() { throw new IllegalArgumentException("This was a NIL"); }

		public boolean equals(Object obj) { return ((SExpr) obj).isNil(); }
		public int hashCode() { return 0; }

		public SExpr CAR() { throw new IllegalArgumentException("This was a NIL"); }
		public SExpr CDR() { throw new IllegalArgumentException("This was a NIL"); }
		public String toString() { return "()"; }
		public byte[] bytesValue() { throw new IllegalArgumentException("This was a NIL"); }
		static final Iterator nilIterator= new Iterator () { 
			public boolean hasNext() { return false; }
			public Object next() { throw new NoSuchElementException(); }
			public void remove() {throw new UnsupportedOperationException();}
		};
		@Override
		public Iterator iterator() {
			return super.iterator();
		}
	}
	
	public SExpr cons(SExpr cdr) {
		return new CONS_(this,cdr);	
	}
	
	public final static SExpr TRUE= new _Boolean_(true),
							  FALSE= new _Boolean_(false);
	
	private static class _Boolean_ extends SExpr {
		private boolean value;
		private _Boolean_(boolean value_) {
			super(null);
			this.value= value_;
		}

		public boolean boolValue() { return value;}

		public String toString() { return value ? "#t":"#f";}

	}
	
	public static class ListConstructor {
		SExpr first= NIL; CONS_ last=null;
		
		public void append(SExpr element){
			if (first==NIL) {
				last= (CONS_)element.cons(first);
				first= last;
			}
			else {
				last.cdr= element.cons(NIL);
				last= (CONS_)last.cdr;
			}
		}
		
		/**
		 * @return the current list and resets to empty
		 * useful in algorithms where the object need to be reused
		 */
		public SExpr make(){
			SExpr result=first;
			first=SExpr.NIL;
			last=null;
			return result;
		}
		
		public SExpr makeDot(SExpr something){
			last.cdr= something;
			SExpr result=first;
			first=SExpr.NIL;
			last=null;
			return result;
		}
	}
	
	private static class CONS_ extends SExpr {
		SExpr car, cdr;
		private CONS_(SExpr car_,SExpr cdr_) { this(car_,cdr_,null);}
		private CONS_(SExpr car_, SExpr cdr_, SExpr junk){
			     super(junk); 
			     assert(car_!=null && cdr_ != null);
				 this.car= car_; this.cdr= cdr_ ; }
		
		public SExpr setCDR(SExpr cdr_) {this.cdr= cdr_; return this; }

		public String stringValue() { throw new IllegalArgumentException("This is a pair not an atom"); }
		
		public byte[] bytesValue() { throw new IllegalArgumentException("This is a pair not an bytearray");}

		public boolean equals(Object o) {
			// TODO: derecursify this thingie
			return
			(o ==null ) ? false 
				: ! (o instanceof CONS_) ? false
							: car.equals(((CONS_)o).car) &&
							  cdr.equals(((CONS_)o).cdr) ;		
		}
		
		public int hashCode() {
			// TODO:  derecursify as well
			return (CONS_.class.hashCode()) ^ (car.hashCode() & 0xFFFF) |  (cdr.hashCode() & 0xFFFF0000);
		}

		public SExpr CAR() { return car; }
		public SExpr CDR() { return cdr; }
		
		/** 
		 * assuming it's a list return iterator over list
		 */
		@Override
		public Iterator iterator() {
			return new Iterator(){
				SExpr pointer= CONS_.this;
				public boolean hasNext() {return pointer.isPair();}
				public Object next() {
					SExpr result= pointer.CAR();
					pointer=pointer.CDR();
					return result;
				}
				public void remove() { throw new UnsupportedOperationException();}
			};
		}
	}
	
	public final boolean  isNil() {return this instanceof NIL_;};
	
	/**
	 * returns an informative type of the type of the datum
	 * for example: ATOM, Number, String, Boolean
	 */
	protected String getTypeName() { return getClass().getName();};
	
	/**
	 * 
	 */
	public final boolean isPair() { return  this instanceof CONS_; }
	public SExpr CAR(){throw new DynamicTypingException("CONS",getTypeName());}
	public SExpr CDR() {throw new DynamicTypingException("CONS",getTypeName());}
	public SExpr setCDR(SExpr cdr_) {throw new DynamicTypingException("CONS",getTypeName());}
	
	public SExpr _1st() { return CAR();}
	public SExpr _2nd() { return CDR().CAR();}
	
	/**
	 * best effort convertioon of content to a java String
	 * value. Unlike toString() which may introduce escape characters 
	 * and is used the debugging 
	 * @return
	 */
	public String asString() { throw new DynamicTypingException("String|Atom|OID",getTypeName()); }
	
	public final boolean isAtom() { return this instanceof _Atom_;}
	public String atomValue(){ throw new DynamicTypingException("String|Atom|OID",getTypeName()); }
	
	
	public boolean isString() { return this instanceof _String_;}
	public String stringValue(){ throw new DynamicTypingException("String|Atom|OID",getTypeName()); }
	

	public final boolean isByteArray() {return this instanceof _ByteArray_; }
	public byte[] bytesValue() {  throw new DynamicTypingException("ByteArray",getTypeName()); }

	public final boolean isBoolean() { return this instanceof _Boolean_;}
	public boolean boolValue() {   throw new DynamicTypingException("ByteArray",getTypeName());	}

	public final boolean isNumber() { return this instanceof _Number_; }
	public int intValue() { return (int) doubleValue(); }
	public long longValue() {return (long) doubleValue(); }
	public double doubleValue() {throw new DynamicTypingException("Numeric",getTypeName());};
	
	public Iterator<SExpr> iterator() { throw new DynamicTypingException("NIL or List",getTypeName()); }
	
	public SExpr getMetaInfo() {
		return meta;
	}
	
	public String toString() { 
		try {
			ByteArrayOutputStream bOS= new ByteArrayOutputStream();
			new RRepresentation().printTo(this,bOS,new Object());
			return new String(bOS.toByteArray(),"ASCII");
			} 
		catch (IOException ex) {throw new RuntimeException(ex);} 
	}
	
	public static SExpr ATOM(String name) {
		return ATOM(name,true);
	}
	
	static WeakHashMap<String,SExpr> atomCache= new WeakHashMap<>();
	/**
	 * constructs an ATOM but checks whether the name has any illegal characters
	 * @throws IllegalArgumentException
	 */
	public static SExpr ATOM_WITH_CHECK(String name) {
		if (".".equals(name)) throw new IllegalArgumentException("illegal ATOM name: .");
		for (int i=0;i<name.length();i++) {
			if(!SUtils.isTokenchar[name.charAt(i)]) 
				throw new IllegalArgumentException("illegal charcter in ATOM name: "+name.charAt(i));
		}
		return ATOM(name);
	}
	public static SExpr ATOM(String name,boolean intern) {
		if (intern){
			SExpr result= (SExpr) atomCache.get(name);
			if (result==null){
				result= ATOM(name,null); 
				atomCache.put(name,result);
			}
			return result;
		}
		else
			return ATOM(name,null);
	}

	public static SExpr ATOM(String name, SExpr meta_) {
		return new  _Atom_(name,meta_);
	}
	
	public static SExpr BYTES(byte[] byteArray) {
		return new _ByteArray_(byteArray);
	}
	
	public static SExpr list(SExpr [] values) {
		SExpr result= NIL;
		for (int i= values.length-1; i>=0; i--) result = values[i].cons(result);
		return result;
	}
	
	/**
	 * Corresponds to ( a b c d e . f) Scheme notation
	 */
	public static SExpr listDotSomething(SExpr [] values, SExpr something)
	{
		SExpr result= something;
		for (int i= values.length-1; i>=0; i--) result = values[i].cons(result);
		return result;
	}

	public static SExpr make( String s) {
		return new _String_(s);
	}
	
	private static class _String_ extends SExpr {
		String value;

		_String_ (String s)  { this(s,null);}
		_String_(String s, SExpr junk) { super(junk); assert(s!=null);this.value= s;}
		
		public String toString() {
			return SUtils.escapeString(value);
		}
		
		
		public String asString() {return value;}
		public String stringValue() { return value;}
		
		public boolean equals(Object obj) {
			return (obj instanceof _String_) ? 
				((_String_) obj).value.equals(this.value) 
				:false;
		}
		
		public int hashCode() {
			return value.hashCode();
		}
		
	}
	
	public static SExpr make(long value) {return new _Number_(BigDecimal.valueOf(value));}
	
	private static class _Number_ extends SExpr {
		BigDecimal value;
		
		_Number_ ( BigDecimal value_) {this(value_,null);}
		_Number_ (BigDecimal value_, SExpr meta_) {
			super(meta_);
			this.value = value_;
			}

		public long longValue() { return value.longValueExact();}
		public double doubleValue() { return value.doubleValue();}
	
		public String toString() { return value.toString();}
		public int hashCode() { return value.hashCode();}
		public boolean equals(Object o) {
			return (o==null) ? false:
				(o instanceof _Number_ ) ?
					value.equals(((_Number_)o).value) 
					:false;
			}
		
	}
	
	
	public static SExpr make(double value) {
		return new _Number_(BigDecimal.valueOf(value));
	}
	
	

	private static class _Atom_ extends SExpr {
		String value;

		_Atom_ (String s)  { this(s,null);}
		_Atom_(String s, SExpr junk) { super(junk); assert(s!=null);
			this.value= s;
		}
		
		public String asString() { return value; }
		public String toString() { return value; }
		
		public String atomValue() {return value;}
		
		public boolean equals(Object obj) {
			return (obj instanceof _Atom_) ? 
				((_Atom_) obj).value.equals(this.value) 
				:false;
		}
		
		public int hashCode() { return value.hashCode(); }
	}
	
	public static SExpr make(byte[] bytes) {
		return new _ByteArray_(bytes);
	}
	
	private static class _ByteArray_ extends SExpr {
		byte[] bytes;
		
		
		_ByteArray_ (byte[] bytes_)  {
				super(null);
				this.bytes= (byte[])bytes_.clone();
		}
		
		public String toString() {
			return "|"+Base64.encodeBytes(bytes)+'|';
		}
		
		public SExpr CAR() { 
			throw new NoSuchElementException("cannot CAR an atom");
		}
		
		public SExpr CDR() { throw new NoSuchElementException("cannot CDR an atom"); }
		
		public byte[] bytesValue() {return bytes;}
		
		public boolean equals(Object obj) {
			if (! (obj instanceof _ByteArray_)) return false;
			byte [] objBytes= ((_ByteArray_)obj).bytes;
			if (bytes.length!= objBytes.length) return false;
			for (int i=0;i<bytes.length;i++) {
				if (bytes[i] != objBytes[i]) return false;
			}
			return true;
		}
		
	}

	
	public static void main(String args[]) {
		System.out.println(SExpr.ATOM("Test"));
		System.out.println();
		byte[] x= { 0,1,2,3}; 
		System.out.println(SExpr.BYTES(x));
		}
}
