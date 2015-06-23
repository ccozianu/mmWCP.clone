/*
 * Created on Apr 21, 2004
 *author: ccozianu
 */
package sexpr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;





/**
 * This class represents a simple recursive parser
 * don't try it at home lest you want your stack to
 * blow up in cases
 * main purpose is to compare performance
 * @author ccozianu
 * created on: Apr 21, 2004
 */
public class RecursiveParser1 {
	
	private  static final byte[] NILBYTES= {'(',')'};
	private static SExpr DOT= SExpr.ATOM(".");
	
	/**
	 * don't allow arrays greater than 10MB by design
	 */
	public static final int MAX_BYTEARRAY_LENGTH= 10*1024*1024;
	
	
	/**
	 * @see sexpr.SExternalRepresentation#fromString(java.lang.String)
	 */
	public SExpr readFrom(String s) throws IOException {
		return readFrom(s.getBytes("ASCII"));
	}
	
	public SExpr readFrom(byte[] bArray) throws IOException{
		return readFrom(new PushbackInputStream(new ByteArrayInputStream(bArray)));
	}

	/**
	 * @see sexpr.SExternalRepresentation#printTo(sexpr.SExpr, java.io.OutputStream, java.lang.Object)
	 */
	public void printTo(SExpr sexpr, OutputStream os, Object extraPerks)
		throws IOException {
		if (extraPerks == null) { 
			transportOutput(sexpr,os);
			return;
		}
		// extraperks not implemented
		throw new RuntimeException("Extra perks not implemented");
	}
	
	static final byte[] _true_ = {'#','t'}; 
	static final byte[] _false_ = {'#','f'}; 
					  
	private static void transportOutput(SExpr sexpr,OutputStream os) throws IOException {
		byte[] bArray;
		if (sexpr.isNil()) { 
			os.write(NILBYTES);
			return; }
		else
		if (sexpr.isPair()) {
			os.write('(');
			SExpr iter= sexpr;
			while (true) {
				SExpr car= iter.CAR();
				SExpr cdr= iter.CDR();
				//TODO: eliminate this recursion
				transportOutput(car, os);
				if (cdr.isNil()) break;
				os.write(' ');
				if (cdr.isPair())	{
					iter=  cdr;
					}
				else  {// this is a pair but not a list
					transportOutput(DOT,os);
					os.write(' '); 
				  	transportOutput(cdr, os);
				  	break;}
				}
			os.write(')');
			return ;
		}
		
		if ( sexpr.isAtom() ) {
			bArray=sexpr.atomValue().getBytes("ASCII");
			os.write(bArray);
			return;
			}
		else 
		if (sexpr.isString()) {
			SUtils.printEscapedString(sexpr.stringValue(),os);
			return;
		}
		else if (sexpr.isNumber()) {
			os.write(sexpr.toString().getBytes("ASCII"));
			return;
		}
		else if (sexpr.isBoolean()) {
			os.write(sexpr.boolValue() ? _true_ : _false_ );
			return;
		}
		throw new RuntimeException("Output not implemented for "+sexpr);		
	}


	/**
	 * @see sexpr.SExternalRepresentation#readFrom(java.io.InputStream)
	 */
	public SExpr readFrom(final PushbackInputStream is) throws IOException {

		class ParseString {	public SExpr _() throws IOException {
				return SExpr.make(SUtils.parseEscapedString(is));
			}
		} final ParseString parseString= new ParseString();
		
		class AtomParser_ {	public SExpr _() throws IOException {
				return SUtils.parseAtom(is);} }
		final AtomParser_ AtomParser= new AtomParser_();
		
		class NumberParser_ {	public SExpr _() throws IOException {
				return SUtils.parseNumber(is);
		}} final NumberParser_ NumberParser= new NumberParser_(); 

		/**
		 * parses SExpressions after the special charcter #
		 * @author ccozianu
		 */
		class ParseSpecial_ {

			public SExpr _() throws IOException {
				int c=is.read();
				if (c==-1){ throw new IOException("Unexpected eof in parsing special");}
				if (c== 't' || c=='T'){
					c= is.read();
					assert(c==-1 || SUtils.isTokenterminator[c]);
					if (c!=-1) is.unread(c);
					return SExpr.TRUE;
					}
				else if (c== 'f' || c=='F'){
					c= is.read();
					assert(c==-1 || SUtils.isTokenterminator[c]);
					if (c!=-1) is.unread(c);
					return SExpr.FALSE;
					}
				else if (SUtils.isDecdigit[c]) {
					int x= c;
					while ((c=is.read())!=-1) {
						if (SUtils.isDecdigit[c]){
							x= x*10+SUtils.decvalue[c];
							if (x> MAX_BYTEARRAY_LENGTH) {
								throw new IOException("Too long byte array");
								}
							}
						else if (c==':') {
							//TODO make sure the async algorithm also got it right
							byte[] buffer= new byte[x];
							int count=0, l=0;
							while ((l=is.read(buffer, count, x-count))!= -1 ){
								count +=l;
								if (count == x ) break;	}
							return SExpr.BYTES(buffer);
							}
						else throw new IOException("Invalid syntax met while trying to parse byte array: charcter"+((char)c));
						}
					throw new IOException("Invalid EOF while trying to parse byte array");
					}
				else throw new RuntimeException("Invalid character after #:"+c);
				//return this;
		}} final ParseSpecial_ parseSpecial= new ParseSpecial_();

		class ListParser_ {
			/* 
			 */
			public SExpr _() throws IOException {
				SExpr.ListConstructor listConstructor= new SExpr.ListConstructor();
				boolean dotEncountered=false;
				SExpr next= null;
				int c;
				while ((c=is.read())!=-1) {	
					if (SUtils.isWhitespace[c]) {
						//just consume whitespaces
						}
					else if (SUtils.isDecdigit[c]) {
						is.unread(c);
						listConstructor.append(NumberParser._());
						}
					else if (c=='(') {
						listConstructor.append(_());	
					}
					else if (c=='"'){
						is.unread(c);
						listConstructor.append(parseString._());
						}
					else if (c=='#'){
						listConstructor.append(parseSpecial._());
						}
					else if (SUtils.isTokenchar[c]) {
						is.unread(c);
						listConstructor.append(AtomParser._());
						}
					else if (c==')') {
						//construct it
						if (! dotEncountered) { return listConstructor.make();}
						else { if (next == null ) { throw new IOException("Illegal use of . (pair constructor)");}
							   else return listConstructor.makeDot(next);
							   }
						}
					}
				throw new IOException("Unexpected end of file, waiting for ) to close a list");			
		}} final ListParser_ ListParser= new ListParser_();

	
		// 
		int c;
		while ((c= is.read()) != -1) {	
			if (SUtils.isWhitespace[c]) {
				//just consume whitespaces
				}
			else if (SUtils.isDecdigit[c]) {
				is.unread(c);
				return NumberParser._();
				}
			else if (c=='(') {
				return ListParser._();	
			}
			else if (c=='"'){
				is.unread(c);
				return parseString._();
				}
			else if (c=='#') {
				return parseSpecial._();
			}
			else if (SUtils.isTokenchar[c]) {
				is.unread(c);
				return AtomParser._();
			}
		}
		throw new IOException("Unexpected EOF trying to read something");
	}


	public static void main(String args[]) {
		try {
		SExpr strA= SExpr.make("A"),
			 atomA= SExpr.ATOM("A"),
			 atomB= SExpr.ATOM("B-C"),
			 strB= SExpr.make("B"),
			 strUnicode= SExpr.make("abxc \uA00B\u00AB\u0030\n");
		RecursiveParser1 r= new RecursiveParser1();
	
		r.printTo( SExpr.ATOM("test").cons(SExpr.NIL),System.out, null);
		System.out.println();
		r.printTo( atomA.cons(atomB),System.out, null);
		System.out.println();
		r.printTo(SExpr.list(new SExpr[] { atomA, atomB} ),System.out,null); 
		System.out.println();
		r.printTo(SExpr.list(new SExpr[] {atomA, strA} ),System.out,null); 
		System.out.println();
		r.printTo(SExpr.list(new SExpr[] {atomA, strA, SExpr.make(1.23456789012345678901e300)} ),System.out,null); 
		System.out.println();
		r.printTo(SExpr.list(new SExpr[] {atomA, strA, SExpr.make(1234567890123456789l)} ),System.out,null); 
		System.out.println();
		}
		catch(Exception ex) {
			System.out.println(ex); 
			ex.printStackTrace(System.err);
		}
	}
}
