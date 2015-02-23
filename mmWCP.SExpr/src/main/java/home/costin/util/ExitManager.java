package home.costin.util;

import java.util.*;

/**
 * This was from my old jdk 1.1 libraries
 * Since JDK 1.7 use runtime.addShutdownHook
 * This type was created in VisualAge.
 */
@Deprecated
public class ExitManager 
{
	private static List cleanups=new ArrayList();
	private static int exitTimeOut=30000;
	private static Object semafor= new Object();

	static
	{
		try
		{
			exitTimeOut= Integer.valueOf(
							System.getProperty("home.costin.util.ExitManager.exitTimeout",
												"30000")
						 ).intValue();
			
		}
		catch(Exception ex)
		{
			System.err.println("ExitManager loaded with exception: "+ex);
		}
		System.out.println("ExitManager loaded successfully.");
	}

	private static class CleanThread implements Runnable
	{
		public void run()
		{
			Iterator itr= cleanups.iterator();
			while (itr.hasNext())
			{
				Cleanup op= (Cleanup) itr.next();
				try
				{
					System.err.println("Cleanup : "+op);
					op.cleanup();
				}
				catch (Throwable ex)
				{
				}
			}
			synchronized(semafor)
			{
				semafor.notify();
			}
		}
	}
public static synchronized void addCleanup(Cleanup op)
{
	if (!cleanups.contains(op))
		cleanups.add(op);
}
public static synchronized void exit(int code)
{
	Thread cleanupThread=new Thread(new CleanThread());
	try
	{
		synchronized(semafor)
		{
			cleanupThread.start();
						Thread.yield();
			semafor.wait(exitTimeOut);
		}
	}
	catch(Exception ex)
	{
		//
	}
	System.exit(code);
}
/**
 * Sample test program to verify the functionality of the class.
 * @param args java.lang.String[]
 */
public static void main(String args[]) 
{
	/*try
	{
	home.costin.jdbc.util.JdbcConnectionManager conManager= home.costin.jdbc.util.DefaultJdbcConnectionManager.instance();
	java.sql.DriverManager.registerDriver((java.sql.Driver)Class.forName("interbase.interclient.Driver")
												.newInstance());
	java.sql.Connection connection1= conManager.getConnection(
													new home.costin.jdbc.util.JdbcConnectionData(
														"jdbc:postgresql://server1/proiect",
														"sysdba",
														"masterkey"
														)		
													);
	java.sql.Connection connection2= conManager.getConnection(
													new home.costin.jdbc.util.JdbcConnectionData(
														"jdbc:interbase://server1/proiect",
														"postgres",
														"postgres"
														)		
													);
	java.sql.Connection connection3= conManager.getConnection(
													new home.costin.jdbc.util.JdbcConnectionData(
														"jdbc:postgresql://server1/proiect",
														"postgres",
														"postgres"
														)		
													);
	}
	catch(Exception ex)
	{
		System.out.println(ex);
	}
	System.out.println("Trying to exit ");
	*/
	exit(0);
}
public static synchronized void removeCleanup(Cleanup op)
{
	cleanups.remove(op);
}
}
