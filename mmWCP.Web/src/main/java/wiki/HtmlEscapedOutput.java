package wiki;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class HtmlEscapedOutput extends OutputStream {
	OutputStream os;
	public HtmlEscapedOutput(OutputStream os_) { this.os= os_; }
	static final byte [][] escapes= new byte[256][];
	static { try {
		escapes['<']="&lt;".getBytes( StandardCharsets.UTF_8 );
		escapes['>']="&gt;".getBytes( StandardCharsets.UTF_8);
		escapes['&']="&amp;".getBytes( StandardCharsets.UTF_8 );
	} catch(Exception ex) {throw new RuntimeException(ex);}}
	
	public void write(int b) throws IOException {
		if (escapes[b] != null)
			os.write(escapes[b]);
		else
			os.write(b);
	}
	
	public void close()  throws IOException {
		os.close();
	}
}
