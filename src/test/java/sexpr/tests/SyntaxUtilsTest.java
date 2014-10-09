/*
 * Created on May 8, 2004
 *
 */
package sexpr.tests;

import home.costin.util.ByteSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

import sexpr.SExpr;
import sexpr.SUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * @author ccozianu
 */
public class SyntaxUtilsTest extends TestCase {

	/**
	 * Constructor for SyntaxUtilsTest.
	 * @param name
	 */
	public SyntaxUtilsTest(String name) {
		super(name);
	}

	public static void main(String[] args) {
		TestRunner.run(SyntaxUtilsTest.class);
	}

	String escapeTests[] = {
		"Test",
		"Test\n.",
		"Test\u0000",
		"Test , \" \r\t\n\b\u007F"
	};
	final public void testEscapeString() throws IOException {
		for (int i=0; i<escapeTests.length;i++) {
			assertEquals(escapeTests[i],
						 SUtils.parseEscapedString(ByteSource.make(
						 	SUtils.escapeString(escapeTests[i]))));
		}
	}

	final public void testEscapeWithLength() {
		//TODO Implement escapeWithLength().
	}

	final public void testPrintEscapedString() {
		//TODO Implement printEscapedString().
	}

	final public void testPrintChar() {
		//TODO Implement printChar().
	}

	/*
	 * Test for String parseEscapedString(ByteSource)
	 */
	final public void testParseEscapedStringByteSource() {
		//TODO Implement parseEscapedString().
	}

	Number x[] = {
		new Integer(-1), new Integer(23),
		new Long(12345678910l),new Long(-12345678910l),
		new Double(1e-290), new Double (1234.56),
		new Double(1.345678901e-200),new Double(1.345678901e200)
	};
	
	final public void testParseNumber() throws IOException {
		//TODO Implement parseNumber().
		assertEquals(SExpr.make(1),SUtils.parseNumber(ByteSource.make("1")));
		assertEquals(SExpr.make(123456),SUtils.parseNumber(ByteSource.make("123456 ")));
		assertEquals(SExpr.make(123456789012345l),SUtils.parseNumber(ByteSource.make("123456789012345")));
		assertEquals(SExpr.make(1.2),SUtils.parseNumber(ByteSource.make("1.2")));
		assertEquals(SExpr.make(0.2),SUtils.parseNumber(ByteSource.make(".2")));
		assertEquals(SExpr.make(0.12345678901),SUtils.parseNumber(ByteSource.make(".12345678901")));
		assertEquals(SExpr.make(123.456789012345),SUtils.parseNumber(ByteSource.make("123.456789012345")));
		assertEquals(SExpr.make(1.5e-200),SUtils.parseNumber(ByteSource.make("1.5e-200")));
		assertEquals(SExpr.make(1.5e246),SUtils.parseNumber(ByteSource.make("1.5e246")));
		assertEquals(SExpr.make(.235e246),SUtils.parseNumber(ByteSource.make(".235e246")));
		assertEquals(SExpr.make(1.23456789012345e-12),SUtils.parseNumber(ByteSource.make("1.23456789012345e-12")));
		assertEquals(SExpr.make(1.23456789012345e-12),SUtils.parseNumber(ByteSource.make("1.23456789012345e-12)")));
	}

	final public void testParseNumber1() throws IOException {
		//TODO Implement parseNumber().
		assertEquals(SExpr.make(1),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("1".getBytes("ASCII")))));
		assertEquals(SExpr.make(123456),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("123456 ".getBytes("ASCII")))));
		assertEquals(SExpr.make(123456789012345l),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("123456789012345".getBytes("ASCII")))));
		assertEquals(SExpr.make(1.2),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("1.2".getBytes("ASCII")))));
		assertEquals(SExpr.make(0.2),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream(".2".getBytes("ASCII")))));
		assertEquals(SExpr.make(0.12345678901),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream(".12345678901".getBytes("ASCII")))));
		assertEquals(SExpr.make(123.456789012345),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("123.456789012345".getBytes("ASCII")))));
		assertEquals(SExpr.make(1.5e-200),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("1.5e-200".getBytes("ASCII")))));
		assertEquals(SExpr.make(1.5e246),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("1.5e246".getBytes("ASCII")))));
		assertEquals(SExpr.make(.235e246),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream(".235e246".getBytes("ASCII")))));
		assertEquals(SExpr.make(1.23456789012345e-12),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("1.23456789012345e-12".getBytes("ASCII")))));
		assertEquals(SExpr.make(1.23456789012345e-12),SUtils.parseNumber(new PushbackInputStream(new ByteArrayInputStream("1.23456789012345e-12)".getBytes("ASCII")))));
	}

}
