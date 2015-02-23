/*
 * Created on May 5, 2004
 *author: ccozianu
 */
package home.costin.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Writer;

/**
 * This class abstracts sources of bytes from both synchronous
 * InputStream and asynchronous I/O sources
 */

public abstract class ByteSource {
	
	
	/**
	 * a capsule class use to communicate all available bytes 
	 * between ByteSource and its clients
	 */
	public class AvailableData {
		private AvailableData(int capacity)
			{ source = new byte[capacity];}
			
		private AvailableData(byte []source_, boolean copy) {
			source= copy ? (byte[])source_.clone(): source_;
		}
		
		private byte[] source;
		
		/**
		 * the caller is mandated not to modify the bytes in the array
		 * in absense of <b>const</b> in java the returned array should be 
		 * considered unmodifiable
		 */
		public byte[] getSourceArray()
			 { return source;}
		
		private int startPos=0;
		public final int getStart() 
			{return startPos;}
		
		int count=0;	
		public final int getCount()
			{ return count;}
	}
	
	public static ByteSource make(InputStream is) throws IOException {
		//return new InputStreamByteSource(is);
		/**/if (is instanceof BufferedInputStream)
			return new DumbInputStreamByteSource(is);
		else 
			return new DumbInputStreamByteSource(new BufferedInputStream(is,1024));
			/**/
	}
	
	/**
	 * precondition: make sure that String 
	 * contains only ASCII characters !!!
	 * @author ccozianu
	 */
	public static ByteSource make(String s) {
		try { return make(new ByteArrayInputStream(s.getBytes("ASCII"))); }
		catch (IOException e) {throw new RuntimeException(e);}	
	}
	

	public static ByteSource make(byte[] array){
		return new ArraySource(array);
	}
	
	
	/**
	 * returns the current byte available in the stream without consuming it
	 * or -1 there's nothing else to peek
	 */
	public abstract int peekByte() throws IOException;
	
	//public abstract AvailableData peekAllAvailable()throws IOException;
	
	/**
	 * returns how many bytes will be available 
	 * for consequent reads without blocking 
	 */
	public abstract int available() throws IOException;
	
	/**
	 * advances the stream with one position
	 * @return false if EOF is met by this op, true otherwise
	 * @throws IOException
	 */ 
	public abstract boolean advance() throws IOException; 
	
	/**
	 * advances and peeks the next byte
	 */
	public abstract int advanceAndPeek() throws IOException;

	/**
	 * advances the stream with n position
	 * @return false if EOF is met by this op, true otherwise
	 * @throws IOException
	 */
	public abstract boolean advance(int n) throws IOException;
	
	/**
	 * returns the next character in the stream, consuming it
	 * @throws IOException
	 */
	public abstract int consumeOne() throws IOException;
	
	/**
	 * return the number of bytes read 
	 */
	public abstract int getReadBytesCount();
	
	/**
	 * returns all the data available, while consuming it
	 */
	/*public AvailableData consumeAllAvailable() throws IOException {
		AvailableData result= peekAllAvailable();
		if (result!=null) {
			advance(result.count);
		}
		return result;
	}*/
	
	private static class DumbInputStreamByteSource extends ByteSource {
		int lastRead= -1;
		int byteCount= 0;
		//boolean firstTimeReadFlag=true;
		InputStream is;

		DumbInputStreamByteSource(InputStream is_) throws IOException{
			this.is= is_;
			lastRead= doRead();
		}
		
		public boolean advance() throws IOException {
			if (lastRead != -1) byteCount++;
			else return false;
			lastRead= doRead();
			return (lastRead != -1);
		}
		
		private int doRead() throws IOException {
			int result= is.read();
			if (result == -1) is.close();
			//else byteCount++;
			return result;
		}

		public int advanceAndPeek() throws IOException {
			if (advance())
				return lastRead;
			else 
				return -1;
		}
		
		public int available() throws IOException {
			// TODO Auto-generated method stub
			return 1+is.available();
		}
		
		public int consumeOne() throws IOException {
			int result =lastRead;
			if (lastRead!= -1)
				advance();
			return result;
		}
		
		public boolean advance(int n) throws IOException {
			for (int i=0;i<n; i++) {
				if (advance()) continue;
				else return false;
			}
			return true;
		}
		
		public int getReadBytesCount() {
			return byteCount;
		}
		
		/*public AvailableData peekAllAvailable() throws IOException {
			AvailableData data= new AvailableData(1);
			data.source[0]= lastRead;
			return data;
		}*/
		
		public int peekByte() throws IOException {
			return lastRead;
		}
		
		
	}
	
	private static class InputStreamByteSource extends ByteSource {
		AvailableData buffer= new AvailableData(512);
		int byteCount=0;
		InputStream is;
		InputStreamByteSource(InputStream is_) {
			this.is= is_;
		}
		
		private int x;
		private boolean getMoreData() throws IOException {
			int count_ = is.read(buffer.source, 0, buffer.source.length);
			System.err.println("Request no:"+(++x)+" retrieved:"+count_);
			if (count_== -1) {
				//also set the end of stream condition in buffer
				buffer.startPos= buffer.count;
				return false;
			}
			if (count_ == 0){ // this normally should not happen
				//try to read at least one char
				int c= is.read();
				if (c==-1) return false;
				buffer.source[0]= (byte)c;
				buffer.startPos=0;
				int n= is.read(buffer.source,1,buffer.source.length-1);
				if (n>0) {
					buffer.count = 1 + n;
				}
				else 
					buffer.count=1;
				return true;  
			}
			else{ // successful read
				buffer.startPos=0;
				buffer.count= count_;
				return true;
			}
		}
		
		public boolean advance() throws IOException{
			return advance(1);
		}
		
		public int advanceAndPeek() throws IOException{
			if (advance())
				return peekByte();
			else return -1;
		}
		
		public int available() throws IOException{
			return buffer.startPos< buffer.count?
					buffer.count - buffer.startPos:
					is.available(); 
		}

		/**
		 * this advances the stream with n position
		 * if the positions before the n-th have not been 
		 * read using peek methods, they are lost
		 * @see home.costin.util.ByteSource#advance(int)
		 */
		public boolean advance(final int n) throws IOException {
			int N=n;
			while (true) {
			if (buffer.startPos+n < buffer.count) {
				buffer.startPos +=n;
				byteCount += n;
				return true;
				}
			else {
				N -= (buffer.count- buffer.startPos);
				byteCount += (buffer.count - buffer.startPos);
				if (! getMoreData()) return false;
				}
			}
		}

		/**
		 * return whatever is available in the stream, while consuming it
		 * @see sexpr.util.ByteSource#consumeAllAvailable()
		 */
		/*public AvailableData consumeAllAvailable() {
			return null;
		}*/

		/**
		 * @see home.costin.util.ByteSource#consumeOne()
		 */
		public int consumeOne() throws IOException {
			if (buffer.startPos == buffer.count) {
				if (! getMoreData())
					return -1;
			}
			int result= buffer.source[buffer.startPos++];
			byteCount ++;
			if (buffer.startPos == buffer.count){
				getMoreData();
				//restore the invariant
			}
			return result;
		}

		/**
		 * the flag that sets the condition that upon construction
		 * this object is positioned on the first byte in the stream
		 * without trigerring a blocking read upon construction
		 */
		private boolean firstTimeReadFlag=true;

		/**
		 * @see home.costin.util.ByteSource#peekAllAvailable()
		 */
		public final AvailableData peekAllAvailable() throws IOException{
			if (firstTimeReadFlag){
				firstTimeReadFlag= false;
				if (! getMoreData() )
					return null;
			}
			return buffer;
		}

		/**
		 * returns the current available byte in the stream
		 * or -1 if the end of the stream is reached or there's nothing to peek
		 * @see home.costin.util.ByteSource#peekByte()
		 */
		public final int peekByte() throws IOException {
			if (peekAllAvailable() == null 
				|| buffer.startPos== buffer.count)
				return -1;
			return buffer.source[buffer.startPos];
		}
		
		public int getReadBytesCount() {
			return byteCount;
		}

	}
	
	
	private static class ArraySource extends ByteSource {
		
		byte[] source;
		int pos=0;
		int length;
		
		ArraySource(byte [] b) {
			this.source= b;
			length= b.length;
		}
			
		public boolean advance() throws IOException {
			return ((++pos)<length) ;
		}

		/* (non-Javadoc)
		 * @see sexpr.ByteSource#advance(int)
		 */
		public boolean advance(int n) throws IOException {
			// TODO Auto-generated method stub
			return (pos+=n) < length ;
		}

		/* (non-Javadoc)
		 * @see sexpr.ByteSource#advanceAndPeek()
		 */
		public int advanceAndPeek() throws IOException {
			// TODO Auto-generated method stub
			advance();
			return peekByte();
		}

		/* (non-Javadoc)
		 * @see sexpr.ByteSource#consumeAllAvailable()
		 */
		public AvailableData consumeAllAvailable() throws IOException {
			// TODO Auto-generated method stub
			AvailableData result= new AvailableData(source,false);
			result.startPos= pos;
			result.count= length;
			pos= length;
			return result;
		}

		/* (non-Javadoc)
		 * @see sexpr.ByteSource#consumeOne()
		 */
		public int consumeOne() throws IOException {
			// TODO Auto-generated method stub
			return pos<length ? source[pos++] : -1 ;
		}

		/* (non-Javadoc)
		 * @see sexpr.ByteSource#peekAllAvailable()
		 */
		/*public AvailableData peekAllAvailable() throws IOException {
			AvailableData result= new AvailableData(source,false);
				result.startPos= pos;
				result.count= length;
				return result;
		}*/
		
		public int available() {return length-pos; }

		public int peekByte() throws IOException {
			return pos<length? source[pos] : -1;
		}
		
		public int getReadBytesCount() {
			return pos;
		}
	}

	public void transferTo(OutputStream os) throws IOException {
		int c;
		while ((c=consumeOne())!=-1) os.write(c);
	}
	
	public void transferAsASCII(Writer os) throws IOException {
		int c;
		while ((c=consumeOne())!=-1) os.write((char)c);
	}

}
