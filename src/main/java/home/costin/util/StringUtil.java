package home.costin.util;

/**
 * Insert the type's description here.
 * Creation date: (12/13/2000 2:25:21 PM)
 * @author: Administrator
 */
public class StringUtil {
/**
 * Insert the method's description here.
 * Creation date: (12/13/2000 2:36:38 PM)
 * @param args java.lang.String[]
 */
public static void main(String[] args) 
{
	System.out.println("Testing functionality of " +StringUtil.class);
	System.out.println("replaceFirst(\"Access\",\"cc\",\"ss\" -> "+StringUtil.replaceFirst("Access","cc","ss"));
	System.out.println("replaceLast(\"Excessiveness\",\"ess\",\"ell\" -> "+StringUtil.replaceLast("Excessiveness","ess","ell"));
	System.out.println("replaceAll(\"Excessivenesse\",\"ess\",\"ell\" -> "+StringUtil.replaceAll("Excessivenesse","ess","ell"));
}
/**
 * Insert the method's description here.
 * Creation date: (12/13/2000 2:29:25 PM)
 * @return java.lang.String
 * @param arg java.lang.String
 * @param what java.lang.String
 * @param with java.lang.String
 */
public static String replaceAll(String arg, String what, String with) {
	StringBuffer sb= new StringBuffer();
	int index= -1;
	
	while ( (index= arg.indexOf(what)) != -1 )
	{
		sb.append(arg.substring(0,index)).append(with);
		arg= arg.substring(index+what.length());
	}
	sb.append(arg);
	return sb.toString();
}
/**
 * Insert the method's description here.
 * Creation date: (12/13/2000 2:29:25 PM)
 * @return java.lang.String
 * @param arg java.lang.String
 * @param what java.lang.String
 * @param with java.lang.String
 */
public static String replaceFirst(String arg, String what, String with) {
	int index= arg.indexOf(what);
	return arg.substring(0,index)+with+arg.substring(index+what.length());
}
/**
 * Insert the method's description here.
 * Creation date: (12/13/2000 2:29:25 PM)
 * @return java.lang.String
 * @param arg java.lang.String
 * @param what java.lang.String
 * @param with java.lang.String
 */
public static String replaceLast(String arg, String what, String with) {
	int index= arg.lastIndexOf(what);
	return arg.substring(0,index)+with+arg.substring(index+what.length());
}
}
