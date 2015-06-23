/*
 * Created on Apr 21, 2004
 * author: ccozianu
 */
package sexpr;

import home.costin.util.ByteSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sexpr.util.Base64;



/**
 * @author ccozianu
 * This representation is loosely based on Ronald Rivest's 
 * S-Expression proposal
 */

public class RRepresentation implements SRepresentation {
	
	private  static final byte[] NILBYTES= {'(',')'};
	private static SExpr DOT= SExpr.ATOM(".");
	
	public static final int MAX_BYTEARRAY_LENGTH= 10*1024*1024;
	
	

	
	/**
	 * @see sexpr.SExternalRepresentation#fromString(java.lang.String)
	 */
	public SExpr readFrom(String s) {
		try { return readFrom(ByteSource.make(s)); }
		catch (IOException ex) { throw new RuntimeException(ex);}
	}
	
	public SExpr readFrom(byte[] bArray) throws IOException{
		return readFrom(ByteSource.make(bArray));
	}

	/**
	 * @see sexpr.SExternalRepresentation#printTo(sexpr.SExpr, java.io.OutputStream, java.lang.Object)
	 */
	public void printTo(SExpr sexpr, OutputStream os, Object extraPerks) throws IOException {
		if (extraPerks != null) 
			transportOutput(sexpr,os);
		else
			prettyPrint(sexpr,os);
	}
	private void prettyPrint(SExpr sexpr,OutputStream os)  throws IOException{
		reallyPP(sexpr,os,0);
	}
	private void reallyPP(SExpr sexpr,OutputStream os, int level) throws IOException{
		byte[] bArray;
		byte spaces[]= {' ',' '};
		
		for (int i=0;i<level;i++ ) {
			os.write(spaces);
		}
		if (sexpr.isNil()) { 
			os.write(NILBYTES);
			os.write('\n');
			return; }
		else
		if (sexpr.isPair()) {
			os.write('(');os.write('\n');
			SExpr iter= sexpr;
			while (true) {
				SExpr car= iter.CAR();
				SExpr cdr= iter.CDR();
				//TODO: eliminate this recursion
				reallyPP(car, os, level + 1);
				if (cdr.isNil()) break;
				//os.write(' ');
				if (cdr.isPair())	{
					iter=  cdr;
					}
				else  {// this is a pair but not a list
					reallyPP(DOT,os,level + 1);
					os.write(' '); 
				  	reallyPP(cdr, os, level + 1);
				  	break;}
				}
			for (int i=0;i<level;i++ ) {
				os.write(spaces);
			}
			os.write(')');os.write('\n');
			return ;
		}
		
		if ( sexpr.isAtom() ) {
			bArray=sexpr.atomValue().getBytes("ASCII");
			os.write(bArray);os.write('\n');
			return;
			}
		else 
		if (sexpr.isString()) {
			SUtils.printEscapedString(sexpr.stringValue(),os);
			os.write('\n');
			return;
		}
		else if (sexpr.isNumber()) {
			os.write(sexpr.toString().getBytes("ASCII"));
			os.write('\n');
			return;
		}
		else if (sexpr.isBoolean()) {
			if (sexpr.boolValue()) {
				os.write(new byte[] {'#','t'}); 
			}
			else 
				os.write(new byte[] {'#','f'});
			os.write('\n');
			return;
		}
		else if (sexpr.isByteArray()) {
			os.write('|');
			Base64.OutputStream b64os= new Base64.OutputStream(os, Base64.ENCODE );
			b64os.write(sexpr.bytesValue());
			b64os.close();
			os.write('|');
			os.write('\n');
			return;
		}
		throw new RuntimeException("Output not implemented for "+sexpr);		
		
	}
	private void transportOutput(SExpr sexpr,OutputStream os) throws IOException {
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
			if (sexpr.boolValue()) {
				os.write(new byte[] {'#','t'}); 
			}
			else 
				os.write(new byte[] {'#','f'});
			return;
		}
		else if (sexpr.isByteArray()) {
			os.write('|');
			Base64.OutputStream b64os= new Base64.OutputStream(os, Base64.ENCODE );
			b64os.write(sexpr.bytesValue());
			b64os.close();
			os.write('|');
			return;
		}
		throw new RuntimeException("Output not implemented for "+sexpr);		
	}


//
// parsing engine
//
/**
	static class ResultOrContinuation {
		final boolean isResult;
		final SExpr result;
		final ParsingStage cont;	
		private ResultOrContinuation(
			boolean isResult_, SExpr result_, ParsingStage cont_
			) {
			this.isResult= isResult_;
			this.cont= cont_;
			this.result= result_;
			}
		static ResultOrContinuation makeResult(SExpr result) { 
			return new ResultOrContinuation(true,result,null);
		}
		static ResultOrContinuation makeContinuation(ParsingStage stage) {
			return new ResultOrContinuation(false,null,stage);
		}
	}
	*/
	public static abstract class ParsingStage {
		public abstract Object parse() throws IOException;
		public abstract Object continueWithResult(SExpr result) throws IOException ;
	}
	
	static class ParsingAlgorithm {
		final ByteSource bs;
		final ParsingStack stack=new ParsingStack();
		/** this stack implementation is here for performance reasons only */
		class ParsingStack {
			ParsingStage elements[] = new ParsingStage[200];
			int len=200;
			int index=-1;
		
			private void push(ParsingStage stage){
			if (index==len){
				len= len << 1;
				ParsingStage []temp= new ParsingStage[len];
				System.arraycopy(elements,0,temp,0,index+1);
				}
			elements[++index]= stage;
			}
			private ParsingStage pop(){
			ParsingStage result= elements[index];
			elements[index]= null;
			index--;
			
			return result;
			}
			
			private ParsingStage peek(){
			return elements[index];
			}
			private boolean isNonEmpty() {return index>=0;}
			
		}
		
		ParsingAlgorithm(ByteSource bs_) {
			assert(bs_!=null);
			this.bs= bs_;
		}
		
		class TopLevelParser extends ParsingStage {

			public Object parse() throws IOException {
			int c= bs.peekByte();
			while (true) {	
				if (c==-1) {
					break;
				}
				else if (SUtils.isDecdigit[c]) {
					return new NumberParser();
					}
				else if (c=='(') {
					bs.advance();
					return new ListParser();	
				}
				else if (c=='"'){
					return new StringParser();
					}
				else if (c=='#') {
					bs.advance();
					return new SpecialParser();
				}
				else if (c=='|') {
					return new Base64Parser();
				}
				else if (SUtils.isTokenChar(c)) {
					return new AtomParser();
				}
				else if (SUtils.isWhitespace(c)) {
					//just consume whitespaces
					c= bs.advanceAndPeek(); 
					}
				else
					throw new RuntimeException("unexpected char: "+((char)c));
				}
				
			return this;
			}
			
			public Object continueWithResult(SExpr result) {
				return result;
			}
		}
		
		class StringParser extends ParsingStage {
			public Object parse() throws IOException {
				return SExpr.make(SUtils.parseEscapedString(bs));
			}
			
			public Object continueWithResult(SExpr result)
				throws IOException {
					throw new RuntimeException("Invalid state");
			}
		}
		
		class AtomParser extends ParsingStage {
			boolean withDot;
			
			public AtomParser() { this(false);}
			public AtomParser(boolean b) { withDot=b;}


			public Object continueWithResult(SExpr result) throws IOException {
					throw new RuntimeException("Invalid state");
			}

			public Object parse() throws IOException {
				String s= SUtils.parseAtom(bs);
				if (withDot) s= "."+s;
				return SExpr.ATOM(s);
			}

		}
		
		/**
		 * parses SExpressions after the special charcter #
		 * #t #f are booleans
		 * @author ccozianu
		 */
		class SpecialParser extends ParsingStage{
			public Object continueWithResult(SExpr result) throws IOException {
					throw new RuntimeException("Invalid state");
			}

			public Object parse() throws IOException {
				int c;
				if (bs.available()>0){
				c= bs.peekByte();
				if (c== 't' || c=='T'){
					c= bs.advanceAndPeek();
					assert(c==-1 || SUtils.isTokenterminator(c));
					return SExpr.TRUE;
					}
				if (c== 'f' || c=='F'){
						c= bs.advanceAndPeek();
						assert(c==-1 || SUtils.isTokenterminator(c));
						return SExpr.FALSE;
						}
				else if (SUtils.isDecdigit[c]) {
					//parse a number
					//representing the length prefix
					int x= c;
					while (true) {
						c= bs.advanceAndPeek();
						if (c== -1)
							throw new IOException("Invalid EOF while trying to parse byte array");
						else if (SUtils.isDecdigit[c]){
							x= x*10+SUtils.decvalue[c];
							if (x> MAX_BYTEARRAY_LENGTH) {
								throw new IOException("Too long byte array");
								}
							}
						else if (c==':') {
							bs.advance();
							break;
							}
						else throw new IOException("Invalid syntax met while trying to parse byte array: met "+((char)c));
						}
					}
				// parse "#<<" <Terminator> <Text> <Terminator>
				else if (c=='<') {
					c= bs.advanceAndPeek();
					if (c != '<') throw new RuntimeException("Invalid syntax, expecting #<< Term text Term, met " +((char)c));
					SUtils.consumeWhiteSpaces(bs); 
					String term= SUtils.parseAtom(bs);
					SUtils.consumeWhiteSpaces(bs);
					return SExpr.make(SUtils.parseToStopWord(bs,term));
				}
				else throw new RuntimeException("Invalid character after #: "+((char)c));
				}
				return this;
			}
			
		}
		
		final static SExpr[] dummySArray= {};	
		class ListParser extends ParsingStage {
			//ArrayList elements= new ArrayList();
			SExpr.ListConstructor listConstructor= new SExpr.ListConstructor();
			boolean dotEncountered=false;
			SExpr next= null;
		
			
			public Object continueWithResult(SExpr result)
				throws IOException {
				if ( ! dotEncountered) {
					listConstructor.append(result);
				}
				else if (next == null ){
					next= result;
					}
				else
					throw new RuntimeException("Illegal use of . (pair constructor)");
				return parse();
			}
			
			private SExpr constructIt() throws IOException {
				if (! dotEncountered) {
					return listConstructor.make();					
				}
				else {
					if (next == null ) {
						throw new IOException("Illegal use of . (pair constructor)");
					}
					else
						return listConstructor.makeDot(next);
				}
			}
			/* 
			 */
			public Object parse() throws IOException {
				int c= bs.peekByte();
				while (true) {	
					if (c==-1) {
						break;
					}
					if (SUtils.isWhitespace[c]) {
						//just consume whitespaces
						}
					else if (SUtils.isDecdigit[c]) {
						return new NumberParser();
						}
					else if (c=='(') {
						bs.advance();
						return new ListParser();	
					}
					else if (c=='"'){
						return new StringParser();
						}
					else if (c=='#'){
						bs.advance();
						return new SpecialParser();
						}
					else if (c == '.') {
						bs.advance();
						int c1= bs.peekByte();
						if (c1==-1) break;
						if (SUtils.isWhitespace[c1]) {
							dotEncountered=true; //and consume the whitespace
						}
						else if  (SUtils.isTokenchar[c]){ 
							return new AtomParser(true);
						}
					}
					else if (SUtils.isTokenchar[c] ) {
						return new AtomParser();
						}

					else if (c==')') {
							bs.advance();
							return constructIt();
						}
					c= bs.advanceAndPeek(); 
					}
				return this;			
			}
}
		
		class NumberParser extends ParsingStage {
			public Object continueWithResult(SExpr result)
				throws IOException {
					throw new RuntimeException("Invalid state");
			}
			public Object parse() throws IOException {
				return SUtils.parseNumber(bs);
			}
		}

		class Base64Parser extends ParsingStage {
			public Object continueWithResult(SExpr result)
				throws IOException {
					throw new RuntimeException("Invalid state");
			}
			
			public Object parse() throws IOException {
				//TODO:
				return SUtils.parseBase64(bs);
			}
		}
		
		public SExpr parse() throws IOException {
			ParsingStage parser= new TopLevelParser();
			stack.push(parser);
			Object r=null;
			SExpr result= null;
			do{
				parser= stack.peek();
				if (result== null)
					r= parser.parse();
				else {
					r= parser.continueWithResult(result);
					result= null;
				}
				if (r instanceof SExpr) {
					result= (SExpr)r;
					stack.pop();
					}		
				else { //continuation
					ParsingStage cont= (ParsingStage) r;
					if (cont != parser) {
						stack.push(cont);
					}
					else {
						// presumably needs more data
						if (! bs.advance())	 {
							System.err.println("current parser: "+cont);
							System.err.println("stack size:"+stack.len);
							throw new IOException("Unexpected end of file, in parsing");
						}
					}
				}
			} while (stack.isNonEmpty());
			return result;
		}
	}

	/**
	 * @see sexpr.SExternalRepresentation#readFrom(java.io.InputStream)
	 */
	public SExpr readFrom(final ByteSource bs) throws IOException {
		return new ParsingAlgorithm(bs).parse();
	}


	public static void main(String args[]) {
		try {
		SExpr strA= SExpr.make("A"),
			 atomA= SExpr.ATOM("A"),
			 atomB= SExpr.ATOM("B-C"),
			 strB= SExpr.make("B"),
			 strUnicode= SExpr.make("abxc \uA00B\u00AB\u0030\n");
		RRepresentation r= new RRepresentation();
	
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
