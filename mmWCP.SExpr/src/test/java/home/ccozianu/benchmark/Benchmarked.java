package home.ccozianu.benchmark;

/**
 * Insert the type's description here.
 * Creation date: (5/20/2001 9:28:06 PM)
 * @author: 
 */
public interface Benchmarked {
/**
 * to be invoked immediately after the benchmark cycle
 * the Benchmarked can do whatever action (ex: log the results)
 * Creation date: (7/12/2001 4:22:45 PM)
 * timing is in milliseconds
 */
void afterBenchmarkCycle( double timing);
/**
 * Insert the method's description here.
 * Creation date: (5/20/2001 9:28:55 PM)
 * @param repeatCount int
 */
void doAction(int repeatCount) throws Exception;
/**
 * Insert the method's description here.
 * Creation date: (7/12/2001 4:42:47 PM)
 */
void recycle();
/**
 * Insert the method's description here.
 * Creation date: (5/20/2001 11:22:53 PM)
 */
void setUp();
}
