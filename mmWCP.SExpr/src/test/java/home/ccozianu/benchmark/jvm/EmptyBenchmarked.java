package home.ccozianu.benchmark.jvm;

import home.ccozianu.benchmark.AbstractBenchmarked;
import home.ccozianu.benchmark.Benchmarked;

/**
 * Insert the type's description here.
 * Creation date: (5/21/2001 12:24:59 AM)
 * @author: 
 */
public class EmptyBenchmarked 
	extends AbstractBenchmarked
	implements Benchmarked
{
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 12:24:59 AM)
 * @param repeatCount int
 */
public void doAction(int repeatCount) 
{
	for (int i=0;i<repeatCount;i++)
		;
}
}
