package home.ccozianu.benchmark;

/**
 * Insert the type's description here.
 * Creation date: (7/12/2001 4:24:38 PM)
 * @author: 
 */
public abstract class AbstractBenchmarked 
	implements Benchmarked 
{
/**
 * AbstractBenchmarked constructor comment.
 */
public AbstractBenchmarked() {
	super();
}
/**
 * to be invoked immediately after the benchmark cycle
 * the Benchmarked can do whatever action (ex: log the results)
 * Creation date: (7/12/2001 4:24:38 PM)
 */
public void afterBenchmarkCycle(double timing) 
{
	
}
/**
 * Insert the method's description here.
 * Creation date: (7/12/2001 4:24:38 PM)
 * @param repeatCount int
 */
public abstract void doAction(int repeatCount)  throws Exception;
	public void recycle()
	{
		// do nothing
		
	}
/**
 * Insert the method's description here.
 * Creation date: (7/12/2001 4:24:38 PM)
 */
public void setUp() {}
}
