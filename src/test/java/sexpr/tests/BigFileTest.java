/*
 * Created on May 20, 2004
 *
 */
package sexpr.tests;

import home.costin.util.ByteSource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PushbackInputStream;

import sexpr.RecursiveParser1;
import sexpr.RRepresentation;
import sexpr.SExpr;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author ccozianu
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BigFileTest extends TestCase {

	/**
	 * Constructor for BigFileTest.
	 * @param arg0
	 */
	public BigFileTest(String arg0) {
		super(arg0);
	}

	public static void main(String[] args) {
		TestRunner.run(BigFileTest.class);
	}

	ByteSource bs;
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		{
		byte[] byteArray;
		ByteArrayOutputStream bout= new ByteArrayOutputStream(10000000);
		PrintStream ps= new PrintStream(bout);
		ps.print(" ( root  ");
		for (int i=0; i< 400000; i++) {
			ps.print("\n\t ( (Test");
			ps.print(i%2000);
			//ps.print(i);
			ps.print(" ");
			ps.print(i);
			ps.print(" ) \n\t\t (X");
			ps.print(i%2000);
			//ps.print(i);
			ps.print(" \"");
			ps.print(i);
			ps.print("\"))");
		}
		ps.print("   ) ");
		ps.flush();		
		
		byteArray= bout.toByteArray();
		System.out.println("generated data: "+ (byteArray.length/1024.) +"kb.");

		try {
		FileOutputStream fout=new FileOutputStream("stresstest.sexp");
		fout.write(byteArray);
		fout.close();
		System.out.println("SExpression is written to file out.");
		}
		catch (IOException ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
		/**/
		}
		Runtime.getRuntime().gc();		
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test for SExpr readFrom(ByteSource)
	 */
	public void testReadFromByteSource() throws IOException {
			//ByteSource bs= ByteSource.make(byteArray);
			
			FileInputStream finput= new FileInputStream("stresstest.sexp");
			ByteSource bs= ByteSource.make(finput);
			RRepresentation r= new RRepresentation();
			SExpr expr= r.readFrom(bs);
			finput.close();
			_testConformance(expr);
	}
	
	public void testRecursiveRead() throws IOException {
			//ByteSource bs= ByteSource.make(byteArray);
			
			FileInputStream finput= new FileInputStream("stresstest.sexp");
			PushbackInputStream pushback= new PushbackInputStream(new BufferedInputStream(finput,1024));
			RecursiveParser1 r= new RecursiveParser1();
			SExpr expr= r.readFrom(pushback);
			finput.close();
			_testConformance(expr);
	}
	
	private boolean _testConformance(SExpr sexp){
		assertTrue(sexp.isPair());
		assertTrue(sexp.CAR().atomValue().equals("root"));
		
		SExpr iterator= sexp.CDR();
		while (!iterator.isNil()) {
			SExpr firstEntry= iterator._1st()._1st();
			assertTrue(firstEntry.isPair());
			assertTrue(firstEntry.CAR().isAtom());
			assertTrue(firstEntry.CDR().CAR().isNumber());
			assertTrue(firstEntry.CDR().CDR().isNil());
			SExpr secondEntry= iterator._1st()._2nd();
			assertTrue(secondEntry.isPair()) ;
			assertTrue(secondEntry.CAR().isAtom()) ;
			assertTrue(secondEntry.CDR().CAR().isString());
			assertTrue( secondEntry.CDR().CDR().isNil()) ;
			iterator= iterator.CDR();
		}
		return true;
	}

}
