package home.ccozianu.benchmark.jvm;

import home.ccozianu.benchmark.AbstractBenchmarked;
import home.ccozianu.benchmark.Benchmarked;

/**
 * Insert the type's description here.
 * Creation date: (5/21/2001 1:02:10 AM)
 * @author: 
 */
public class InvokeInterfaceVirtual1
	extends AbstractBenchmarked
	implements Benchmarked 
{
	TestInterface obj=new TestDerived1();
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 1:02:10 AM)
 * @param repeatCount int
 */
public void doAction(int repeatCount) 
{
	for (int i=0;i<repeatCount;i++)
	{
		obj.doNothing();
	}
}
}
