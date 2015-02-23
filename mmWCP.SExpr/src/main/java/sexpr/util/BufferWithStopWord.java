package sexpr.util;

import home.costin.util.StringConstructor;

/**
 * This is a string buffer that builds until a stop word is recognized.
 * the idea is to be able to parse constructs like
 * bash input reditrection
 * cat << EOF
 * contents
 * until the stop word 
 * is recognized
 * EOF
 * @author Costin Cozianu
 */

public class BufferWithStopWord {

	String terminator;
	StringConstructor consumer;
	int start=0; int l=0; int N;
	boolean bufferFull=false;
	byte[] buffer;
	
	public BufferWithStopWord( String terminator_, StringConstructor consumer_) {
		this.terminator= terminator_;
		this.consumer= consumer_;
		N= terminator_.length();
		buffer= new byte[N];
	}
	
	
	/**
	 * this method should be called only after the buffer is full
	 * @param i
	 * @return
	 */
	private int indexTranslate(int i) {
		return (start+i)%N;
	}
	/**
	 * consumes one byte and
	 * returns true if the stream has been recognized
	 * the bytes up to  
	 */
	public boolean push(byte c) {
		if (!bufferFull) return fillBuffer(c);
		else return pushWithBufferFull(c);
	}


	private boolean pushWithBufferFull(byte c) {
		consumer.append((char)buffer[start]);
		buffer[start++]=c;
		start %= N;
		return checkForEnd();
	}


	private boolean fillBuffer(byte c) {
		buffer[l++]=c;
		bufferFull= l==N;
		if (bufferFull)
			return checkForEnd();
		return false;
	}


	private boolean checkForEnd() {
		for (int i=N-1;i>=0;i--) {
			if (buffer[indexTranslate(i)] != terminator.charAt(i))
				return false;
		}
		return true;
	}
	
	/**
	 * minimal test
	 */
	public static void main(String args[]) {
		try {
		byte [] input= "This is a test.EOF, blah blah".getBytes();
		StringConstructor result= new StringConstructor();
		BufferWithStopWord wrb =new BufferWithStopWord("EOF",result);
		for (int i=0; i< input.length; i++) {
			if (wrb.push(input[i])) { //EOF recognized
				System.out.println("Result: "+result.toString());
				return;
				}
			}
		throw new RuntimeException("EOF was not detected");
		}
		catch(Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
	}
}
