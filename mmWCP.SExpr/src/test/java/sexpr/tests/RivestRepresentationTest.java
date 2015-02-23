/*
 * Created on May 10, 2004
 *
 */
package sexpr.tests;

import home.costin.util.ByteSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sexpr.RecursiveParser1;
import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.Utils;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author ccozianu
 */
public class RivestRepresentationTest extends TestCase {

	RRepresentation r= new RRepresentation();

	/**
	 * Constructor for RivestRepresentationTest.
	 * @param name
	 */
	public RivestRepresentationTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestRunner.run(RivestRepresentationTest.class);
	}



	final public void testPrintTo() throws IOException{
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
		r.printTo(SExpr.list(new SExpr[] {atomA, strA, SExpr.make(1234567890123456789l), SExpr.TRUE} ),System.out,null); 
		System.out.println();	
	}

	final public void testReadFrom() throws IOException {
		RecursiveParser1 r= new RecursiveParser1();
		SExpr e=r.readFrom(Utils.pushbackOfString("\"Test\""));
		assertEquals(e,SExpr.make("Test"));
		System.out.println(e);
		
		e= r.readFrom(" \n\t123");
		assertEquals(e,SExpr.make(123));	
		
		e= r.readFrom(" test ");
		assertEquals(e,SExpr.ATOM("test"));
		
		e= r.readFrom("( Test \"x\n\" 123.45)");
		assertEquals(e,SExpr.ATOM("Test").cons(SExpr.make("x\n").cons(SExpr.make(123.45).cons(SExpr.NIL))));
		r.printTo(e,System.out,null);
		System.out.println();
		
		e= r.readFrom("( Test \"x\n\" #t (atom 1 2 (#f 3))123.45e-300 )");
		SExpr test=SExpr.ATOM("Test").cons(SExpr.make("x\n").cons(
		SExpr.TRUE.cons((SExpr.ATOM("atom").cons(SExpr.make(1
							).cons(SExpr.make(2).cons(SExpr.FALSE.cons(SExpr.make(3).cons(SExpr.NIL)).cons(SExpr.NIL)))))
						.cons(SExpr.make(123.45e-300).cons(SExpr.NIL)))));
		r.printTo(test, System.out,null);
		System.out.println();
		assertEquals(e,test);
		r.printTo(e,System.out,null);
		System.out.flush();

	}

	final public void testReadByteArray() throws IOException {

		byte[] testBytes= new byte[]{0,1,2,3,4,5,6,7,8,9,
									 20,21,22,23,24,25,26,27,28,29,30};
		SExpr ref= SExpr.BYTES(testBytes);
		ByteArrayOutputStream bOs= new ByteArrayOutputStream();
		r.printTo(ref,bOs,null);
		byte[] encodedBytes= bOs.toByteArray();
		System.out.println();
		System.out.println(new String(encodedBytes,"ASCII"));
		SExpr readExp= r.readFrom(encodedBytes);
		assertEquals(ref,readExp);
		
	}
	
	public void testReadFromFile() throws IOException {
		ByteSource bs= ByteSource.make(this.getClass().getResourceAsStream("/Test1.txt"));
		SExpr sexpr= r.readFrom(bs);
		r.printTo(sexpr,System.out,null);
	}
	
	public void testDot() {
		System.out.println(r.readFrom("(a .b) "));
	}
}
