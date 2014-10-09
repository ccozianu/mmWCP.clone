package home.ccozianu.benchmark;

/**
 * Insert the type's description here.
 * Creation date: (8/14/2001 3:40:48 PM)
 * @author: 
 */
public abstract class Sort extends AbstractBenchmarked {
	protected Object originalArray[], workArray[];
	protected int numberOfObjects;

	protected  java.util.Random random= new java.util.Random();
/**
 * SortBenchmark constructor comment.
 */
public Sort() {
	this(100);
}
/**
 * SortBenchmark constructor comment.
 */
public Sort(int numberOfObjects) {
	super();
	this.numberOfObjects= numberOfObjects;
}
/**
 * Insert the method's description here.
 * Creation date: (8/14/2001 3:40:48 PM)
 * @param repeatCount int
 */
public void doAction(int repeatCount) 
{
	for (int i=0;i<repeatCount;i++)
	{
		System.arraycopy(originalArray,0,workArray,0,originalArray.length);
		doSort();
	}
}
/**
 * Insert the method's description here.
 * Creation date: (8/14/2001 3:48:58 PM)
 */
protected abstract void doSort();
	protected Object generateRandomObject()
	{
		return generateRandomString();
	}
/**
 * Insert the method's description here.
 * Creation date: (8/14/2001 3:50:09 PM)
 * @return java.lang.String
 */
public String generateRandomString() {
	char[] buffer = new char[random.nextInt(15)+5];
	for (int i=0;i<buffer.length; i++)
		buffer[i]= (char) ('a'+random.nextInt(13));
	return new String(buffer);
}
/**
 * Insert the method's description here.
 * Creation date: (8/14/2001 5:46:54 PM)
 * @param args java.lang.String[]
 */
public static void main(String[] args) 
{
	try
	{
	Benchmark bm0=	new Benchmark(
						new Sort()
							{
								public void doSort()
								{
									for (int i=0; i<numberOfObjects-1;i++)
										for (int j=i+1;j<numberOfObjects;j++)
											if (((Comparable) workArray[i]).compareTo(workArray[i]) > 0)
												{
													Object temp= workArray[i];
													workArray[i]= workArray[j];
													workArray[j]= temp;
												}
									
								}
							},
						100,
						10
					);
	bm0.benchmark();
	bm0.printResult(System.out);
	Benchmark bm1=	new Benchmark(
						new Sort()
							{
								public void doSort()
								{
									java.util.Arrays.sort(workArray);
								}
							},
						100,
						10
					);
	bm1.benchmark();
	bm1.printResult(System.out);
	}
	catch (Exception ex) {
		System.err.println(ex);
		ex.printStackTrace(System.err);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (8/14/2001 3:44:05 PM)
 */
public void setUp() 
{
	originalArray= new Object[numberOfObjects];
	workArray= new Object[numberOfObjects];
	for (int i=0; i< numberOfObjects; i++)
	{
		originalArray[i]= generateRandomObject();
	}
}
}
