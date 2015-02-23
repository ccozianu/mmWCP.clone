/*
 * Created on Jun 4, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package sexpr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

/**
 * @author ccozianu
 *
 */
public class Utils {

	public static PushbackInputStream pushbackOfString(String s) throws IOException{
		return new PushbackInputStream(new ByteArrayInputStream(s.getBytes("ASCII")));
	}
	
}
