package home.ccozianu.benchmark.jvm;

import home.ccozianu.benchmark.AbstractBenchmarked;

/**
 * Insert the type's description here.
 * Creation date: (5/21/2001 12:24:59 AM)
 * @author: 
 */
public class AlmostEmptyBenchmarked extends AbstractBenchmarked
{
	long test=0;
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 12:24:59 AM)
 * @param repeatCount int
 */
public void doAction(int repeatCount) 
{
	for (int i=0;i<repeatCount;i++)
		test++;
}
}
