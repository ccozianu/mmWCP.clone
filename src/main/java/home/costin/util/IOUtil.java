package home.costin.util;

/**
 * Insert the type's description here.
 * Creation date: (12/13/2000 3:42:57 PM)
 * @author: Administrator
 */
public class IOUtil {
	public static byte[] readFully(java.io.InputStream is) throws java.io.IOException
	{
		java.io.ByteArrayOutputStream os= new java.io.ByteArrayOutputStream(512);
		byte [] buffer= new byte[512];
		int lastReadCount=-1;
		while ((lastReadCount= is.read(buffer))!=-1)
				os.write( buffer, 0, lastReadCount);
		return os.toByteArray();
	}
	public static byte[] readFully(java.io.InputStream is, int maxByteCount) throws java.io.IOException
	{
		java.io.ByteArrayOutputStream os= new java.io.ByteArrayOutputStream(512);
		byte [] buffer= new byte[512];
		int lastReadCount=-1;
		int totalReadCount=0;
		while ((lastReadCount= is.read(buffer))!=-1)
		{
			totalReadCount += lastReadCount;
			if (totalReadCount > maxByteCount)
				throw new java.io.IOException(" Maximum number of bytes exceeded :"+ IOUtil.class +".readFully(InputStream, int)");
			os.write( buffer, 0, lastReadCount);
		}
		return os.toByteArray();
	}
}
