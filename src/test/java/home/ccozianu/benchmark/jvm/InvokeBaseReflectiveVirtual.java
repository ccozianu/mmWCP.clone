package home.ccozianu.benchmark.jvm;

import home.ccozianu.benchmark.AbstractBenchmarked;
import home.ccozianu.benchmark.Benchmarked;

/**
 * Insert the type's description here.
 * Creation date: (5/21/2001 1:02:10 AM)
 * @author: 
 */
public class InvokeBaseReflectiveVirtual
	extends AbstractBenchmarked
	implements Benchmarked 
{
	TestBase obj;
	public final static Object emptyArgs[] = new Object[]{};
	java.lang.reflect.Method testMethod;
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 1:02:10 AM)
 * @param repeatCount int
 */
public void doAction(int repeatCount) 
{
	for (int i=0;i<repeatCount;i++)
	{
		try
		{
			testMethod.invoke(obj,emptyArgs);
		}
		catch(Exception ex)
		{
			throw new RuntimeException("Exception: "+ex);
			
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 1:02:10 AM)
 */
public void setUp() 
{
	obj= new TestDerived();
	try
	{
	testMethod= TestBase.class.getMethod("doNothing", new Class[]{});
	}
	catch(NoSuchMethodException ex)
	{
		throw new RuntimeException(ex.getMessage());
	}
}
}
