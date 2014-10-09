package home.ccozianu.benchmark.jvm;

import home.ccozianu.benchmark.AbstractBenchmarked;
import home.ccozianu.benchmark.Benchmark;
import home.ccozianu.benchmark.Benchmarked;

/**
 * Insert the type's description here.
 * Creation date: (5/30/2001 5:08:09 PM)
 * @author: 
 */
public class EuclidBenchmark
	extends AbstractBenchmarked
	implements Benchmarked
{
	int numberOne;
	int numberTwo;
	int result;
	int complexity=0;

	public void afterBenchmarkCycle(double timing)
	{
			System.out.println("cmmdc ("+numberOne+','+numberTwo+")="+result+"complexity="+complexity +" timing="+timing);
	}
/**
 * Insert the method's description here.
 * Creation date: (5/30/2001 5:08:09 PM)
 * @param repeatCount int
 */
public void doAction(int repeatCount) 
{
	//int result=0;
	for (int i=0;i<repeatCount;i++)
	{
		complexity=0;
		int a=numberOne;
		int b=numberTwo;
		int c= a%b;
		while (c !=0 )
		{
			a=b;
			b=c;
			c=a%b;
			complexity++;
		}
		
		result=b;
	}
	//System.out.println("cmmdc ("+numberOne+','+numberTwo+")="+result);
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

		Benchmark bm8= new Benchmark(new EuclidBenchmark(), repeatAction, repeatBenchmark); 
		bm8.benchmark();
		System.out.println(
			"Benchmark(Euclid,"+repeatAction+','+repeatBenchmark+") : "
				+ home.costin.util.StringFormatter.formatDouble(bm8.getResult(),12,',','.')
				+ "ms");
	}
	catch (Throwable ex)
	{
		System.err.println(ex);
		ex.printStackTrace(System.err);
	}
}
	public void recycle()
	{
		complexity=0;
	}
/**
 * Insert the method's description here.
 * Creation date: (5/30/2001 5:08:09 PM)
 */
public void setUp() 
{
	java.util.Random random=new java.security.SecureRandom();
	numberOne= 1000000+ random.nextInt(2000000);
	numberTwo= 1000000+ random.nextInt(2000000);
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
