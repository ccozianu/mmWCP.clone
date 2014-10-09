package home.ccozianu.benchmark.jvm;

/**
 * Insert the type's description here.
 * Creation date: (2/15/2001 12:30:00 AM)
 * @author: 
 */
public class BenchmarkTimeAndFor {
/**
 * run the benchmark trying to measure the performance of
 * System.currentTimeMillis()
 * and for (int i=0; i&lt; &lt;const&gt; i++)
 */
public static void main(String[] args)
{
	int i= 0;
	int j= 0;

	for (int iter= 1; iter <= 9; iter++)
	{
		j= 0;
		System.out.println("Iter:" + iter);
		long x10= System.currentTimeMillis();
		for (i= 0; i < 1000000; i++)
		{
			; // do nothing
		}
		long x11= System.currentTimeMillis();
		long d1= x11 - x10;
		System.out.print("1*10^6 -> " + d1);

		long x20= System.currentTimeMillis();
		for (i= 0; i < 2000000; i++)
		{
			; // do nothing
		}
		long x21= System.currentTimeMillis();
		long d2= x21 - x20;
		System.out.print("\t2*10^6 -> " + d2);

		long x30= System.currentTimeMillis();
		for (i= 0; i < 3000000; i++)
		{
			; // do nothing
		}
		long x31= System.currentTimeMillis();
		long d3= x31 - x30;
		System.out.print("\t3*10^6 -> " + d3);

		long x40= System.currentTimeMillis();
		for (i= 0; i < 4000000; i++)
		{
			; // do nothing
		}
		long x41= System.currentTimeMillis();
		long d4= x41 - x40;
		System.out.print("\t4*10^6 -> " + d4);

		long x50= System.currentTimeMillis();
		for (i= 0; i < 5000000; i++)
		{
			; // do nothing
		}
		long x51= System.currentTimeMillis();
		long d5= x51 - x50;
		System.out.print("\t5*10^6 -> " + d5);
		System.out.println("\n" + j);
	}
}
}
