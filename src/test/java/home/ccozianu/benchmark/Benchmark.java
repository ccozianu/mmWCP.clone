package home.ccozianu.benchmark;

import home.ccozianu.benchmark.jvm.*;

/**
 * Insert the type's description here.
 * Creation date: (5/20/2001 9:23:04 PM)
 * @author: 
 */
public class Benchmark {
	private int numOfThreads= 1;
	private double result=0;
	private boolean executed=false;
	private Benchmarked target;
	private int targetRepeatCount;
	private int benchmarkRepeatCount;
	
	public Benchmark(Benchmarked target, int targetRepeatCount, int benchmarkRepeatCount ){
		this(target,targetRepeatCount,benchmarkRepeatCount,1);
	}
	
/**
 * BenchMark constructor comment.
 */
public Benchmark(Benchmarked target, int targetRepeatCount, int benchmarkRepeatCount, int numOfThreads) 
{
	if (targetRepeatCount<=0 || benchmarkRepeatCount<=0 || target == null)
		throw new IllegalArgumentException();
	this.target= target;
	this.targetRepeatCount= targetRepeatCount;
	this.benchmarkRepeatCount= benchmarkRepeatCount;
}
/**
 * Insert the method's description here.
 * Creation date: (5/20/2001 9:31:11 PM)
 * @param targte tests.benchmark.Benchmarked
 * @param repeatCount int
 */
public void benchmark() throws Exception 
{
	if (executed)
		throw new IllegalStateException("The benchmark is already executed");
	target.setUp();
	for ( int i=0; i<benchmarkRepeatCount; i++)
	{
		long time1= System.currentTimeMillis();
		target.doAction(targetRepeatCount);
		long time2= System.currentTimeMillis();
		double timeElapsed= ((double) time2-time1)/targetRepeatCount;
		if (timeElapsed==0)
			throw new RuntimeException("Could not measure benchmark. please increase the repeat count");
		target.afterBenchmarkCycle(timeElapsed);
		result+=timeElapsed;

		target.recycle();
		System.gc();
	}
	result/=benchmarkRepeatCount;
	executed= true;
}
/**
 * Insert the method's description here.
 * Creation date: (5/20/2001 11:24:27 PM)
 * @return double
 */
public double getResult() {
	if (!executed)
		throw new IllegalStateException("the benchmark has not been executed");
	return result;
}
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 12:26:36 AM)
 * @param args java.lang.String[]
 */
public static void main(String[] args)
{
	if (args.length<2)
	{
		showUsage();
		System.exit(-1);
	}
	try
	{
		int repeatAction=Integer.valueOf(args[0]).intValue();
		int repeatBenchmark=Integer.valueOf(args[1]).intValue();
		Benchmark bm0= new Benchmark(new EmptyBenchmarked(), repeatAction, repeatBenchmark);
		bm0.benchmark();
		bm0.printResult(System.out);
		Benchmark bm1= new Benchmark(new AlmostEmptyBenchmarked(), repeatAction, repeatBenchmark);
		bm1.benchmark();
		bm1.printResult(System.out);
		Benchmark bm2= new Benchmark(new InvokeBaseVirtual(), repeatAction, repeatBenchmark);
		bm2.benchmark();
		bm2.printResult(System.out);
		Benchmark bm3= new Benchmark(new InvokeBaseVirtual1(), repeatAction, repeatBenchmark);
		bm3.benchmark();
		bm3.printResult(System.out);
		Benchmark bm4= new Benchmark(new InvokeInterfaceVirtual(), repeatAction, repeatBenchmark);
		bm4.benchmark();
		bm4.printResult(System.out);
		Benchmark bm5= new Benchmark(new InvokeInterfaceVirtual1(), repeatAction, repeatBenchmark);
		bm5.benchmark();
		bm5.printResult(System.out);
		
		Benchmark bm6= new Benchmark(new InvokeBaseReflectiveVirtual(), repeatAction, repeatBenchmark);
		bm6.benchmark();
		bm6.printResult(System.out);
		
		
		Benchmark bm7= new Benchmark(new TypeCheck(), repeatAction, repeatBenchmark); 
		bm7.benchmark();
		bm7.printResult(System.out);

		Benchmark bm8= new Benchmark(new EuclidBenchmark(), repeatAction, repeatBenchmark); 
		bm8.benchmark();
		bm8.printResult(System.out);
		
	}
	catch (Throwable ex)
	{
		System.err.println(ex);
		ex.printStackTrace(System.err);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (5/20/2001 11:24:27 PM)
 * @return double
 */
public void printResult( java.io.PrintStream out) {
	if (!executed)
		throw new IllegalStateException("the benchmark has not been executed");
	out.println(
			"Benchmark("+ target.getClass()+','+targetRepeatCount+','+benchmarkRepeatCount+") : \n"
				+ home.costin.util.StringFormatter.formatDouble(getResult(),12,',','.')
				+ "ms");
}
/**
 * Insert the method's description here.
 * Creation date: (5/21/2001 12:46:18 AM)
 */
private static void showUsage() 
{
	System.out.println("java "+Benchmark.class.getName()+" <repeatAction> " + " <repeatBenchmark> ");
}
}
