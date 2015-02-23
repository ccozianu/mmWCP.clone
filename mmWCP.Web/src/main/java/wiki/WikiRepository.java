/*
 * Created on Jun 11, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package wiki;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Costin Cozianu
 *
 */
public interface WikiRepository {
	
	public class WriteConnection{
		OutputStream source; public OutputStream source() {return source;} 
		OutputStream rendered; public OutputStream rendered() {return rendered;}
	}

	public InputStream open(String[] idPath);
	public InputStream open(String[] idPath, String team, int version);
	
	public WriteConnection store(String[] id, String team, int version);
}
