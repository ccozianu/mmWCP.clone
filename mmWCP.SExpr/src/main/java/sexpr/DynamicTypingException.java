/*
 * Created on May 3, 2004
 *
 */
package sexpr;

/**
 * An exception thrown when the content of an SExpression is other 
 * than what the caller expects.
 * For example calling CAR on an ATOM
 * @author ccozianu
 */
public class DynamicTypingException extends RuntimeException {
	
	public DynamicTypingException(String expected, String real ){
		super("Operation applies to: ("+expected+") found : "+ real);		
	}

}
