/*
 * Created on Apr 21, 2004
 *
 */
package sexpr;

import home.costin.util.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author ccozianu
 *
 */
public interface SRepresentation {
	
	/**
	 * Prints the sexpression to the outputstream. Extraperks are optional 
	 * and the implementation should support null being passed in
	 * They can be used to specify different formats when the external representation 
	 * is not unique (supports several encodings)
	 */
	public void printTo(SExpr sexpr, OutputStream os, Object extraPerks) throws IOException;
	
	

	/**
	 * Parses an SExpression from the input stream 
	 */	
	public SExpr readFrom(ByteSource bs) throws IOException;
	
	/**
	 * Parses an SExpression from the String 
	 * @param s
	 */
	public SExpr readFrom(String s) throws IOException;
	 
}
