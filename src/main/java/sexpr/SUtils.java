/*
 * Created on May 3, 2004
 *
 */
package sexpr;

import home.costin.util.ByteSource;
import home.costin.util.StringConstructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;


import sexpr.util.Base64;
import sexpr.util.BufferWithStopWord;


/**
 * @author ccozianu
 */
public class SUtils {
	static final char hexChars[] = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };

	public static String escapeString(String s) {
		try{
			ByteArrayOutputStream out= new ByteArrayOutputStream(s.length());
			printEscapedString(s,out);
			return (new String(out.toByteArray(),"ASCII"));}
		catch (IOException ex) { throw new RuntimeException(ex);}		
	}
	private static char checkSumBase16(int n) {
		int result=0;
		while (n>0) {
			result |= n & 0xf;
			n >>=4;
		}
		return (char)result;
	}
	public static String escapeWithLength(String s) {
		String s1= escapeString(s);
		int l=s1.length();
		return "#"+l+':'+ s1 +"#"+checkSumBase16(l);
	}

	
	public static void printEscapedString(String s, OutputStream out)  throws IOException{
		out.write('"');
		for (int i=0;i<s.length();i++) {
			printEscapedChar(s.charAt(i),out);
		}
		out.write('"');
	}
	
	public static void printEscapedChar(char c, OutputStream out) throws IOException {
		 if (c=='"' || c=='\\' || c=='\'') { out.write('\\');out.write( c); return;}
		 if (c=='\n' ) { out.write('\\');out.write( 'n'); return; }
		 if (c=='\t' ) { out.write('\\');out.write('t'); return; }
		 if (c=='\r' ) { out.write('\\');out.write('r'); return; }
		 if (c=='\f')  { out.write('\\');out.write('f'); return; }
		if (c=='\b')  { out.write('\\');out.write('b'); return; }
		
		// pseudo-alphabetical characters printed as is
		if (c>=' ' && c <= 126) { out.write((int)c); return; }
		
		
		//ASCII8 characters printed in hex, for familiarity
		if (c<256) { out.write('\\');out.write('x'); out.write( hexChars[c>> 4]); out.write(hexChars[c & 0xF]); return; }
		
		// or else Unicode \uFFFF (four hexadecimal for unicode)
		out.write('\\');out.write('u');
		out.write(hexChars[c>>12]);
		out.write(hexChars[(c>>8) & 0xf]);
		out.write(hexChars[(c>>4) & 0xf]);
		out.write(hexChars[c & 0xf]);
	}
	
	
	
	public static String parseEscapedString(InputStream input) throws IOException  {
		int c= input.read();
		if (c!='"')
			throw new IOException("Error parsing string literal: did not find opening \" ");
		StringConstructor sb= getABuffer();
		while ((c= input.read()) != '"') {
			 if (c==-1) throw new IOException("Error parsing string literal: Unexpected EOF");
			 if (c!='\\')
			 	sb.append((char)c);
			 else {
			 	int x= input.read();
			 	switch(x) {
			 		case 'n' : sb.append('\n'); break;
					case 't' : sb.append('\t'); break;
					case 'r' : sb.append('\r'); break;
					case 'f' : sb.append('\f'); break;
					case 'b' : sb.append('\b'); break;
					case '\\': sb.append('\\'); break;
					case '\'': sb.append('\''); break;
					case '\"': sb.append('\"'); break;
					case 'x': //read hexadecimal
							{
								int d1= readHexDigit(input);
								int d2= readHexDigit(input);
								sb.append((char)((d1<<4) + d2));
								break;	
							}
					case 'u': //read unicode
							{
								int d1= readHexDigit(input);
								int d2= readHexDigit(input);
								int d3= readHexDigit(input);
								int d4= readHexDigit(input);
								sb.append((char)((d1<<12) | (d2 <<8) | (d3 << 4) | d4));
								break;	
							}
							
					default: throw new IOException("Parsing string literal, invalid \\ escape sequence");
			 	}
			 }
		}
		return sb.toString();
	}

	private static ThreadLocal sConstructor= new ThreadLocal();
	
	public static StringConstructor getABuffer() {
		StringConstructor result= (StringConstructor) sConstructor.get();
		if (result==null){
			result= new StringConstructor();
			sConstructor.set(result);
			}
		result.reset();
		return result;
	}
	
	public static String parseEscapedString(ByteSource input) throws IOException  {
		int c= input.consumeOne();
		if (c!='"')
			throw new IOException("Error parsing string literal: did not find opening \" ");
		//StringConstructor sb= new StringConstructor();
		StringConstructor sb= getABuffer();
		while ((c= input.consumeOne()) != '"') {
			 if (c==-1) throw new IOException("Error parsing string literal: Unexpected EOF");
			 if (c!='\\')
				sb.append((char)c);
			 else {
				int x= input.consumeOne();
				switch(x) {
					case 'n' : sb.append('\n'); break;
					case 't' : sb.append('\t'); break;
					case 'r' : sb.append('\r'); break;
					case 'f' : sb.append('\f'); break;
					case 'b' : sb.append('\b'); break;
					case '\\': sb.append('\\'); break;
					case '\'': sb.append('\''); break;
					case '\"': sb.append('\"'); break;
					case 'x': //read hexadecimal
							{
								int d1= readHexDigit(input);
								int d2= readHexDigit(input);
								sb.append((char)((d1<<4) + d2));
								break;	
							}
					case 'u': //read unicode
							{
								int d1= readHexDigit(input);
								int d2= readHexDigit(input);
								int d3= readHexDigit(input);
								int d4= readHexDigit(input);
								sb.append((char)((d1<<12) | (d2 <<8) | (d3 << 4) | d4));
								break;	
							}
							
					default: throw new IOException("Parsing string literal, invalid \\ escape sequence");
				}
			 }
		}
		//TODO: remove debug
		String result= sb.toString();
		if (result.equals("7405")){
			result.toLowerCase();
		}
		return sb.toString();
	}
	

	static final long LONG_THRESHOLD_MAX= Long.MAX_VALUE / 10;
	static final double DOUBLE_THRESHOLD_MAX= Double.MAX_VALUE;
	
	static final double dTest= 1.012345678012345678901234567890123456789e-100;

	/**
	 * expects |<base64-encoding>|
	 * @param bs
	 * @return
	 */
	public static SExpr parseBase64(ByteSource bs) throws IOException {
		ByteArrayOutputStream bos= new ByteArrayOutputStream();
		int c= bs.peekByte();
		if (c!='|') throw new RuntimeException("expecting |");
		while (true) {
			c= bs.advanceAndPeek();
			if (c==-1)
				throw new RuntimeException("unexpected EOF");
			if (c=='|') {
				bs.advance();
				break;
			}
			bos.write(c);
		}
		byte[] bArray= bos.toByteArray();
		return SExpr.BYTES(Base64.decode(bArray,0,bArray.length));
	}
	
	/**
	 * [-][<digits>][.][<digits>][e|E<digits>]
	 * at least one of the digits have to be present before 
	 * @return a number SExpression
	 */
	
	public static SExpr parseNumber(ByteSource bs) throws IOException{
		long y=0;
		int sign= 1;
		int decimalCount=0;
		int c= bs.peekByte();
		int exp=0;
		int expSign=1;
		if (c=='-') { sign=-1; c= bs.advanceAndPeek(); }
		if (c== -1 || (!isDecdigit[c] && c!= '.' ))
			throw new IOException("Unexpected number syntax, got : "+((char) c));

		boolean decimalPart=false;
		while (true) {
			if (c== -1 || isTokenterminator(c)) { //finnish
							if (! decimalPart)
								return SExpr.make(y*sign); //just a long
							else 
								return SExpr.make(new BigDecimal(BigInteger.valueOf(y*sign), decimalCount).doubleValue());
							}
			if (isDecdigit[c]) {
				y= y*10 +decvalue[c];
				if (decimalPart)
					decimalCount++;
				if (y > LONG_THRESHOLD_MAX) { // too large a number
					throw new IOException("too large a number");
					}
				
				}
			else if (c=='.') { 
				decimalPart= true; // now we read the decimal part
				}
			else if ( c=='e' || c=='E') {break;} // go read the exponent
			else throw new IOException("Invalid syntax for number, encountered "+c);
			c= bs.advanceAndPeek();
			}
		
		//by this time we arrived to e/E
		// or else an exception was thrown or return happened
		c= bs.advanceAndPeek(); 
		if (c=='-')  { //negative exponent
			expSign=-1;
			c=bs.advanceAndPeek();
		}
		if (c== -1 || !isDecdigit[c]) // read at least one digit after E
			throw new IOException("Invalid syntax for number, encountered "+c);
		//read maximum 2 more digits on the exponent
		exp=decvalue[c];
		for (int i=0;i<2;i++) {
			 c=bs.advanceAndPeek();
			 if (c==-1 || isTokenterminator(c)) break;
			 if (isDecdigit[c])
			 	exp= exp*10 + decvalue[c]; 
			 else
				throw new IOException("Invalid syntax for number, encountered "+c);
			}
		if (!isTokenterminator(c)) // try to read at least a whitespace or EOF after 3 digits 
		{
			c= bs.advanceAndPeek();
			if (c!=-1 && !isTokenterminator(c)) throw new IOException("Invalid syntax for number, waiting for whitespace");
		}
		if (exp >300)
			throw new IOException("too large a number");
		// finally check against overflow
		int finalExponent= expSign*exp-decimalCount;
		if (finalExponent < 0){
			return SExpr.make(new BigDecimal(BigInteger.valueOf(y),-finalExponent).doubleValue());
		}
		else {
			return SExpr.make(BigDecimal.valueOf(y).movePointRight(finalExponent).doubleValue());
		}
	}

	public static SExpr parseNumber(PushbackInputStream is) throws IOException{
		long y=0;
		int sign= 1;
		int decimalCount=0;
		int c= is.read();
		int exp=0;
		int expSign=1;
		if (c=='-') { sign=-1; c= is.read(); }
		if (c== -1 || (!isDecdigit[c] && c!= '.' ))
			throw new IOException("Unexpected number syntax, got : "+((char) c));

		boolean decimalPart=false;
		do {
			if (c== -1 || isTokenterminator[c]) { //finnish
				if (c!=-1) {is.unread(c);} 
				if (! decimalPart)
					return SExpr.make(y*sign); //just a long
				else 
					return SExpr.make(new BigDecimal(BigInteger.valueOf(y*sign), decimalCount).doubleValue());
				}
			if (isDecdigit[c]) {
				y= y*10 +decvalue[c];
				if (decimalPart)
					decimalCount++;
				if (y > LONG_THRESHOLD_MAX) { // too large a number
					throw new IOException("too large a number");
					}
				}
			else if (c=='.') { 
				decimalPart= true; // now we read the decimal part
				}
			else if ( c=='e' || c=='E') {break;} // go read the exponent
			else throw new IOException("Invalid syntax for number, encountered "+c);
		c=is.read();
		} while (true);
		
		
		//by this time we arrived to e/E
		// or else an exception was thrown or return happened
		c= is.read(); 
		if (c=='-')  { //negative exponent
			expSign=-1;
			c=is.read();}
			
		if (c== -1 || !isDecdigit[c]) // read at least one digit after E
			throw new IOException("Invalid syntax for number, encountered "+c);
			
		//read maximum 2 more digits on the exponent
		exp=decvalue[c];
		for (int i=0;i<2;i++) {
			 c=is.read();
			 if (c==-1 || isTokenterminator[c]) break;
			 if (isDecdigit[c])
				exp= exp*10 + decvalue[c]; 
			 else
				throw new IOException("Invalid syntax for number, encountered "+c);
			}
		
		if (c != -1 && isTokenterminator[c]) {is.unread(c);}
		else if (!isTokenterminator(c)) // try to read at least a whitespace or EOF after 3 digits 
		{
			c= is.read();
			if (c!=-1 && !isTokenterminator[c]) throw new IOException("Invalid syntax for number, waiting for whitespace");
		}
		
		if (exp >300)
			throw new IOException("too large a number");
		// finally check against overflow
		int finalExponent= expSign*exp-decimalCount;
		if (finalExponent < 0){
			return SExpr.make(new BigDecimal(BigInteger.valueOf(y),-finalExponent).doubleValue());
		}
		else {
			return SExpr.make(BigDecimal.valueOf(y).movePointRight(finalExponent).doubleValue());
		}
	}

	/**
	 * advances the source until the first whitespace is met
	 */
	public static void consumeWhiteSpaces(ByteSource bs) throws IOException {
		while(isWhitespace(bs.advanceAndPeek())) ; 
	}
	
	/**
	 * parses the next element as ATOM from the ByteSource
	 */
	public static final String parseAtom(ByteSource bs) throws IOException {
		StringConstructor bout= getABuffer();
		int c= bs.peekByte();
		//read at least one token char
		if (c == -1 || !isTokenChar(c))
			throw new IOException("Cannot have empty atoms");
		while (true) {
			if (c == -1 || isTokenterminator[c])
				break;
			else if (isTokenchar[c])
				bout.append((char)c);
			else
				throw new IOException("invalid token character in ATOM : "+((char)c));
			c=bs.advanceAndPeek();
		}
		return bout.toString();
	}

	public static final SExpr parseAtom(PushbackInputStream is) throws IOException {
		StringConstructor bout= getABuffer();
		int c= is.read();
		//read at least one token char
		if (c == -1 || ! isTokenchar[c])
			throw new IOException("Cannot have empty atoms");
			
		do {
			if (isTokenterminator[c]){
				is.unread(c);
				break; }
			else if (isTokenchar[c]){
				bout.append((char)c);}
			else
				throw new IOException("invalid token character in ATOM : "+((char)c));
		} while ((c=is.read())!=-1);
		
		return SExpr.ATOM(bout.toString());
	}
	
//	static final double tenpowers[] = { 1.0e1, 1.0e2, 1.0e4,1.0e8,1.0e16,1.0e32,1.0e64,1.0e128,1.0e256};
//	static final double tenNegpowers[] = { 1.0e-1, 1.0e-2, 1.0e-4,1.0e-8,1.0e-16,1.0e-32,1.0e-64,1.0e-128,1.0e-256};
//	static final int twopowers[] = { 1, 2,4,8,16,32,64,128,256};
/*	private static double power10(int n){
		double result=1;
		double powers[]= tenpowers; 
		if (n<0){
			n *= -1;
			powers= tenNegpowers;
		}
		for (int i= 8 ; i>0 ; i --) {
			if ((n & twopowers[i]) != 0)
				result *= powers[i];
		}
		return result;
	}
*/	
	
	private static int readHexDigit(InputStream is) throws IOException {
		int d1= is.read(); 
		if (d1 == -1) throw new EOFException();
		if (!isHexdigit[d1]) throw new IOException("Parsing string literal, expected hex digit");
		return hexvalue[d1];		
	}

	private static int readHexDigit(ByteSource bs) throws IOException {
		int d1= bs.consumeOne(); 
		if (d1 == -1) throw new EOFException();
		if (!isHexdigit[d1]) throw new IOException("Parsing string literal, expected hex digit");
		return hexvalue[d1];		
	}	
	static char upper[]=new char[256];            		/* upper[c] is upper case version of c */
	static boolean isWhitespace[]=new boolean[256];     /* whitespace[c] is true if c is whitespace */
	static boolean isTokenterminator[]= new boolean[256];
	static boolean isDecdigit[]=new boolean[256];       /* decdigit[c] is true if c is a dec digit */
	static int decvalue[]=new int[256];           		/* decvalue[c] is value of c as dec digit */
	static boolean isHexdigit[]=new boolean[256];       /* hexdigit[c] is nonzero if c is a hex digit */
	static int hexvalue[]=new int[256];         		/* hexvalue[c] is value of c as a hex digit */
	static boolean isBase64digit[]=new boolean[256];    /* base64char[c] is nonzero if c is base64 digit */
	static int base64value[]=new int[256];      		/* base64value[c] is value of c as base64 digit */
	static boolean isTokenchar[]=new boolean[256];      /* tokenchar[c] is true if c can be in a token */
	static boolean isAlpha[]=new boolean[256];          /* alpha[c] is true if c is alphabetic A-Z a-z */
	static char hexdigits[]=  "0123456789ABCDEF".toCharArray();
		
	//private static void  intializeCharTables() {
	static {
		int i=0;
		  for (i=0;i<256;i++) upper[i] = (char)i;
		  for (i='a'; i<='z'; i++) upper[i] = (char) (i - 'a' + 'A');
		  for (i=0;   i<=255; i++) 
			{ isAlpha[i] = isDecdigit[i] = isWhitespace[i] = isBase64digit[i] = isTokenterminator[i] = false;} 
		  isWhitespace[' ']  = isWhitespace['\n'] = isWhitespace['\t'] =true;
		  isWhitespace['\r'] = isWhitespace['\f'] = true;
		  
		  isTokenterminator[' ']  = isTokenterminator['\n'] = isTokenterminator['\t'] =true;
		  isTokenterminator['\r'] = isTokenterminator['\f'] = isTokenterminator['(']=isTokenterminator[')']=true;
		  
		  for (i='0';i<='9';i++) 
			{ isBase64digit[i] = isHexdigit[i] = isDecdigit[i] = true;
			  decvalue[i] = hexvalue[i] = i-'0';
			  base64value[i] = (i-'0')+52;
			}
			
		  for (i='a';i<='f';i++)
			{ isHexdigit[i] = isHexdigit[upper[i]] = true;
			  hexvalue[i] = hexvalue[upper[i]] = i-'a'+10;
			}
			
		  for (i='a';i<='z';i++) 
			{ isBase64digit[i] = isBase64digit[upper[i]] = true;
			  isAlpha[i] = isAlpha[upper[i]] = true;
			  base64value[i] = i-'a'+26;
			  base64value[upper[i]] = i-'a';
			}
		  isBase64digit['+'] = isBase64digit['/'] = true;
		  base64value['+'] = 62;
		  base64value['/'] = 63;
		  base64value['='] = 0;
		  
		  for (i=0;i<255;i++) isTokenchar[i] = false;
		  for (i='a';i<='z';i++) isTokenchar[i] = isTokenchar[upper[i]] = true;
		  for (i='0';i<='9';i++) isTokenchar[i] = true;
		  isTokenchar['-'] = true;
		  isTokenchar['.'] = true;
		  isTokenchar['/'] = true;
		  isTokenchar['_'] = true;
		  isTokenchar[':'] = true;
		  isTokenchar['*'] = true;
		  isTokenchar['+'] = true;
		  isTokenchar['='] = true;
		  isTokenchar['$'] = true;
		  isTokenchar['?'] = true;
		  isTokenchar['&'] = true;
	}
	
	public static boolean isTokenChar(int c) {
		return c<255 && c>=0 && isTokenchar[c];
	}
	
	public static boolean isWhitespace(int c) {
		return c<255 && c>=0 && isWhitespace[c];
	}
	
	public static boolean isTokenterminator(int c){
		return c<255 && c>0 && isTokenterminator[c];
	}
		
		
	public static void main(String[] args) {
		try
		{
			char [] x= {'a','b','\n', '\t','\r','\b','\f', '"',' ',  '+',  '\1',23,'\32','\126','~',127,'A','Z','\uffff','\u3456', 256,0xFFFF};
			String s= new String(x);
			String s1= escapeString(s);
			System.out.println(s1);
			byte newBytes[] =s1.getBytes();
			System.out.write(newBytes);
			String s2= parseEscapedString(ByteSource.make(new ByteArrayInputStream(newBytes)));
			System.out.println("\nCorrectness criteria: "+ s.equals(s2));

			printEscapedString(s2,System.out);
			System.out.println();
			printEscapedString(parseEscapedString(ByteSource.make(System.in)),System.out);
			System.out.println();
			System.out.println(parseNumber(ByteSource.make(System.in)));
			//System.out.println(Double.parseDouble(".23e246"));
		}
		catch(Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
	}

	
	// parses the current
	public static String parseToStopWord(ByteSource bs, String term) throws IOException {
		//CircularBuffer
		StringConstructor result= new StringConstructor();
		BufferWithStopWord wBuffer= new BufferWithStopWord(term,result);
		int c;
		while((c= bs.consumeOne()) != -1) {
			if (wBuffer.push((byte)c))
				return result.toString();
		}
		throw new RuntimeException("unexpected end of file,waiting for terminator");
	}
		
}
