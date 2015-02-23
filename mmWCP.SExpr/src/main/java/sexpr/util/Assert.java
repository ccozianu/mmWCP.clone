/*
 * Created on May 6, 2004
 *author: ccozianu
 */
package sexpr.util;

/**
 * @author ccozianu
 * created on: May 6, 2004
 */
public class Assert {

	public static void _ (boolean condition){
		if (! condition) throw new RuntimeException("Assertion failed");
	}

}
