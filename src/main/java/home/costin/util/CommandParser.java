package home.costin.util;

import java.io.*;
import java.util.*;
import java.awt.*;

/**
 * CommandParser implements a parser for commands
 * given at a console it automatically invokes the methods in the derived classes based on Java reflection
 * it automatically parse a command reckognizing the first token as the "verb",
 * and the other tokens as arguments,
 * unless they contain an equal sign in which case it takes the token as a property definitions 
 * which can be retrieved later via getProperty(String propertyName)
 * after parsing the command it then automatically invoke the method with the Signature
 * perform&ls;verb&gt;Command(String args[]) passing the apropriate args
 * The Command Parser is case insensitive
 * Derived classes have the only responsibility to implement this naming convention
 */

 
public abstract class CommandParser
{
	StringTokenizer stok;

	private Properties props= new Properties();
	private Hashtable commandMethods = null;

	private final static Object[] argsHolder = new Object[1];

/**
 * Insert the method's description here.
 * Creation date: (9/13/99 2:23:19 PM)
 * @return java.lang.String[]
 * @param array java.lang.Object[]
 */
private static String[] castObjectArrayToStringArray(Object[] array) {
	String [] stringArray= new String[array.length];
	for (int i=0; i< array.length; i++)
		{
			stringArray[i] = (String) array[i];
		}
	return stringArray;
}
/**
 * Check whether the method corresponds to the perform&ls;Verb&gt;Command( String args[])
 * Creation date: (9/12/99 9:28:23 PM)
 * @arg Method method  - the method to be checked
 * @return String &ls;Verb&gt; if the method match the pattern
 * @return null otherwise
 */
private static String checkMethodPattern(java.lang.reflect.Method method) 
{
	String dummyArgs[]= {};
	Class[] parameterTypes = method.getParameterTypes();
	if (parameterTypes.length == 1)
		if (parameterTypes[0].equals(dummyArgs.getClass()))
		{
			String methodName= method.getName();
			if (methodName.startsWith("perform") && methodName.endsWith("Command"))
				return methodName.substring("perform".length(), methodName.length()-"Command".length());
		}
	return null;
}
/**
 * Initialize the private hashtable needed 
 * to dispatch based on the command verbs
 * it searches for methods named perform&ls;Verbname&gt;Command( String args[])
 * and stores them as Method objects in the internal hashtable
 * @return Hashtable - the hashtable linking command "verbs" to corresponding instance methods
 * Creation date: (9/12/99 7:05:14 PM)
 */

private final Hashtable getCommandMethods()
{
	if (commandMethods==null)
	{
		commandMethods = new Hashtable();
		java.lang.reflect.Method methods[] = this.getClass().getMethods();
		for (int i=0; i<methods.length; i++)
		{
			String verb= checkMethodPattern(methods[i]);
			if (verb==null) 
				continue;
			commandMethods.put(verb.toLowerCase(),methods[i]);	
		}
	}
	return commandMethods;
}
protected String getProperty(String name)
{
	return props.getProperty(name.toLowerCase());
}
protected String getProperty(String name, String defaultValue)
{
	return props.getProperty(name.toLowerCase(),defaultValue);
}
/**
 * 
 * @param command java.lang.String
 */
public void parseCommandLine(String commandLine) 
{
	String commandVerb,argument;
	int i;
	Vector args= new Vector();
	
	stok= new StringTokenizer(commandLine," \t\n\r");
	if (!stok.hasMoreElements()) 
		return; // The commandLine is empty

	commandVerb = stok.nextToken().toLowerCase(); // get the verb as the first token on the commandLine

	java.lang.reflect.Method commandMethod= (java.lang.reflect.Method) getCommandMethods().get(commandVerb);
	if (commandMethod == null)
		return; // the verb doesn't have a crresponding method
		
	while (stok.hasMoreElements())
	{
		argument= stok.nextToken();
		i= argument.indexOf('=');
		if (i!=-1)
			{
				String property= argument.substring(0,i).toLowerCase();
				String value= argument.substring(i+1);
				props.put(property,value);
			}
		else
			args.addElement(argument);
	}

	// Invoke the method
	try
	{
		Object [] tempArgs= new Object[args.size()];
		args.copyInto(tempArgs);
		String [] arguments= castObjectArrayToStringArray(tempArgs);
		argsHolder[0]= arguments;
		commandMethod.invoke(this,argsHolder);
	}
	catch(IllegalAccessException ex)
	{
		System.err.println("Exception: "+ex);
		System.err.println("at");
		ex.printStackTrace(System.err);
	}
	catch(java.lang.reflect.InvocationTargetException ex)
	{
		System.err.println("Exception: "+ex);
		System.err.println("at");
		ex.printStackTrace(System.err);
	}
}
}
