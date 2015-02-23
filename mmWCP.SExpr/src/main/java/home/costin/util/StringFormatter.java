package home.costin.util;

/**
 * @deprecated these utilities were created long ago, before modrn printf 
 * was available in Java
 * 
 * This type was created in VisualAge.
 * It holds a few static methods useful for string formatting such as
 * aligning values and strings, filling up strings with a character, etc.
 * Really nothing important
 * @author Costin Cozianu <a href=mailto://ccozianu@yahoo.com>ccozianu@yahoo.com</a>
 */
 
@Deprecated
public class StringFormatter {
	public static int LEFTALIGN   = 0;
	public static int RIGHTALIGN  = 1;
	public static int CENTERALIGN = 2;
	
	static final long powersOfTen[] = {	1l,10l,100l,1000l,10000l,100000l,1000000l, 
									  	10000000l, 100000000l, 1000000000l, 10000000000l,100000000000l,
									  	1000000000000l};
public final static String formatDouble(double value,int decimalPlaces, char thousandSeparator,char decimalSeparator)
{
	StringBuffer sb= new StringBuffer();
	if (decimalPlaces<0) 
		decimalPlaces=0;

	long longpart;
	double decimalpart;
	if ( value==0.0 ) 
		return "0.0";
		
	if ( value<0.0 )
	{
		sb.append("-");
		value = -value;
	}

	longpart= (long) Math.floor(value);
	decimalpart= value- longpart;
	sb.append( formatLong(longpart,thousandSeparator));
	if (decimalPlaces>0)
	{
		sb.append(decimalSeparator);
		for (int i=0; i< decimalPlaces; i++)
		{
			decimalpart*=10;
			int k= (int) decimalpart;
			sb.append(k);
			decimalpart -= k;
		}
	}
	return sb.toString();
}
public final static String formatLong(long value,char thousandSeparator)
{
	if (value == 0)
		return "0";
		
	StringBuffer sb= new StringBuffer();
	boolean appendMinus= false;
	if (value < 0)
	{
		value =- value;
		appendMinus= true;
	}
	
	int figureCount=0;
	while (value > 0)
	{
		sb.append(value%10);
		value /= 10;
		figureCount++;
		if ( figureCount%3 == 0 && value >0 )
			sb.append(thousandSeparator);
	}
	if (appendMinus)
		sb.append('-');
		
	return sb.reverse().toString();
}
public final static String formatString(String value, int size)
{
	return (formatString(value, size, LEFTALIGN));
}
public final static String formatString(String value, int size, int align)
{
	StringBuffer sb= new StringBuffer(size+2);
	int initsize = value.length();
	int d=size-initsize;
	int n=d;
	int i;

	if (d == 0) 
		return (value);
	if (d < 0) 
		return (value.substring(0, size));
	if (align==CENTERALIGN) 
		n/=2;

	for (i = 0; i < n; i++) 
		sb.append(' ');
	String x=sb.toString();
	String val = null;
	
	switch (align)
	{
		case 0:
				val = value+ x;
				break;
		case 2:
				sb. setLength(0);
				sb.append(x).append(value).append(x);
				if (d%2==1) 
					sb.append(' ');
				val= sb. toString();
				break;
		case 1:
		default:
				val = x + value;
				break;
	}

	return (val);
}
public final static String generateString(char c,int n)
{
	StringBuffer sb= new StringBuffer(n);
	int i=0;
	for (i=0; i<n; i++) 
		sb.append(c);
	return sb.toString();
}
/**
 * This method is for testing purposes.
 * Creation date: (5/24/00 10:41:46 AM)
 * @param args java.lang.String[]
 */
public static void main(String[] args) 
{
	System.out.println(" formatLong(0,'.') -> "+ formatLong(0,'.'));
	System.out.println(" formatLong(-2,'.') -> "+ formatLong(-2,'.'));
	System.out.println(" formatLong(-234,'.') -> "+ formatLong(-234,'.'));
	System.out.println(" formatLong(-1456,'.') -> "+ formatLong(-1456,'.'));
	System.out.println(" formatLong(-331456,'.') -> "+ formatLong( -331456, '.'));
	System.out.println(" formatLong(-18383456,'.') -> "+ formatLong( -18383456, '.'));
	System.out.println(" formatLong(0,'.') -> "+ formatLong(0,'.'));
	System.out.println(" formatLong(2,'.') -> "+ formatLong(2,'.'));
	System.out.println(" formatLong(234,'.') -> "+ formatLong(234,'.'));
	System.out.println(" formatLong(1456,'.') -> "+ formatLong(1456,'.'));
	System.out.println(" formatLong(331456,'.') -> "+ formatLong( 331456, '.'));
	System.out.println(" formatLong(18383456,'.') -> "+ formatLong( 18383456, '.'));


	System.out.println(" formatDouble(0,4,'.',',') -> "+formatDouble(0,4,'.',','));
	System.out.println(" formatDouble(PI,4,'.',',') -> "+formatDouble(Math.PI,4,'.',','));
	System.out.println(" formatDouble(10*PI,3,'.',',') -> "+formatDouble(10*Math.PI,3,'.',','));
	System.out.println(" formatDouble(100*PI,3,'.',',') -> "+formatDouble(100*Math.PI,3,'.',','));
	System.out.println(" formatDouble(1000*PI,3,'.',',') -> "+formatDouble(1000*Math.PI,3,'.',','));
	System.out.println(" formatDouble(100000000*PI,3,'.',',') -> "+formatDouble(100000000*Math.PI,3,'.',','));
	System.out.println(" formatDouble(-PI,3,'.',',') -> "+formatDouble(-Math.PI,3,'.',','));
	System.out.println(" formatDouble(-10*PI,3,'.',',') -> "+formatDouble(-10*Math.PI,3,'.',','));
	System.out.println(" formatDouble(-100*PI,3,'.',',') -> "+formatDouble(-100*Math.PI,3,'.',','));
	System.out.println(" formatDouble(-1000*PI,3,'.',',') -> "+formatDouble(-1000*Math.PI,3,'.',','));
	System.out.println(" formatDouble(-100000000*PI,3,'.',',') -> "+formatDouble(-100000000*Math.PI,3,'.',','));

	System.out.println(" |formatString(\"BLA\",10, LEFTALIGN)| -> |"+ formatString("BLA",10,LEFTALIGN)+'|');
	System.out.println(" |formatString(\"BLA\",10, RIGHTALIGN)| -> |"+ formatString("BLA",10,RIGHTALIGN)+'|');
	System.out.println(" |formatString(\"BLA\",10, CENTERALIGN)| -> |"+ formatString("BLA",10,CENTERALIGN)+'|');
	System.out.println(" |formatString(\"BLAB\",10, CENTERALIGN)| -> |"+ formatString("BLAB",10,CENTERALIGN)+'|');

}
}
