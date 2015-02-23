package wiki;

import java.io.IOException;
import java.io.Writer;

public class HtmlEscapedWriter extends Writer {
	
	Writer writer;
	
	public HtmlEscapedWriter(Writer writer_) {
		this.writer= writer_;
	}

	public void write(char[] cbuf, int off, int len) throws IOException {
		for (int j=0;j<len; j++)
			write(cbuf[off+j]);

	}
	
	public void write(int c) throws IOException {
		if (c=='<')	writer.write("&lt;");
		else if (c=='>') writer.write("&gt;");
		else if (c=='&') writer.write("&amp;");
		else writer.write(c);
	}

	public void flush() throws IOException {
		writer.flush();
	}

	public void close() throws IOException {
		writer.close();
	}

}
