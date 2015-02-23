/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package sexpr.tests;

import home.ccozianu.benchmark.AbstractBenchmarked;
import home.ccozianu.benchmark.Benchmark;
import home.costin.util.ByteSource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;

import sexpr.RecursiveParser1;
import sexpr.RRepresentation;
import sexpr.SExpr;


/**
 * @author ccozianu
 *
 */
public class RecursiveParsingSExprBench extends AbstractBenchmarked {

	boolean generateOutput= false;
	int count= 400000;
	byte []byteArray ;
	{
		/**/
		if (generateOutput)
		try {
		//ByteArrayOutputStream bout= new ByteArrayOutputStream(20000000);
		OutputStream bout= new BufferedOutputStream(new FileOutputStream("benchmark.sexp"),512); 
		PrintStream ps= new PrintStream(bout);
		ps.print(" ( root  ");
		for (int i=0; i< count; i++) {
			ps.print("\n\t ( (Test");
			ps.print(i%2000);
			//ps.print(i);
			ps.print(" ");
			ps.print(i);
			ps.print(" ) \n\t\t (X");
			ps.print(i%2000);
			//ps.print(i);
			ps.print(" \"");
			ps.print(i);
			ps.print("\"))");
		}
		ps.print("   ) ");
		ps.flush();	
		ps.close();	
		System.gc();
		}
		catch (IOException ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex);
		}
		//byteArray= bout.toByteArray();
		/*
		try {
		FileOutputStream fout=new FileOutputStream("benchmark.sexp");
		fout.write(byteArray);
		fout.close();
		System.out.println("SExpression is written to file out.");
		}
		catch (IOException ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
		/**/
		/*
		try {
			RandomAccessFile file =new RandomAccessFile("benchmark.sexp","r");
			long size=  file.length(); if (size>Integer.MAX_VALUE) throw new IOException("ionput file too big");
			byteArray = new byte[(int)size]; 
			int bytesRead= file.read(byteArray);
			System.out.println("KB read: "+(bytesRead/1024.0));
			file.close();
			//System.out.println("SExpression is written to file out.");
		}
		catch (IOException ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
		*/		
		System.out.println("Data size (kb)"+(new File("benchmark.sexp").length()/1024.0));
	}
	

	public static void main(String[] args) {
		try
		{
		int repeatAction=
			args.length>0 ? Integer.valueOf(args[0]).intValue() : 1;
		int repeatBenchmark=
			args.length>0 ? Integer.valueOf(args[1]).intValue() : 1;

		Benchmark bm= new Benchmark(new RecursiveParsingSExprBench(), repeatAction, repeatBenchmark,5); 
		System.out.println();
		bm.benchmark();
		System.out.println(
			"Benchmark(SEprBench,"+repeatAction+','+repeatBenchmark+") : "
				+ home.costin.util.StringFormatter.formatDouble(bm.getResult(),12,',','.')
				+ "ms");
		}
		catch (Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);		
		}
	}
	/* (non-Javadoc)
	 * @see home.costin.benchmark.Benchmarked#doAction(int)
	 */
	RecursiveParser1 r= new RecursiveParser1();
	SExpr expr=null;
	public void doAction(int repeatCount) throws IOException{
		for (int i=0; i< repeatCount; i++) {
			//ByteSource bs= ByteSource.make(byteArray);
			
			FileInputStream finput= new FileInputStream("benchmark.sexp");
			PushbackInputStream pushback= new PushbackInputStream(new BufferedInputStream(finput,1024));
			
			expr= r.readFrom(pushback);
			finput.close();
		}
	}

	/* (non-Javadoc)
	 * @see home.costin.benchmark.Benchmarked#afterBenchmarkCycle(double)
	 */
	public void afterBenchmarkCycle(double timing) {
		super.afterBenchmarkCycle(timing);
		try {
		//r.printTo(expr,System.out,null);
		System.out.print("Checking validity:" +testConformance(expr));
		System.out.println();
		System.out.println("VM size:"+(Runtime.getRuntime().totalMemory()/1024.0/1024.0));
		}
		catch (Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
	}
	
	private boolean testConformance(SExpr sexp){
		if (!sexp.isPair() )
			return false;
		if (!sexp.CAR().atomValue().equals("root"))
			return false;
		
		SExpr iterator= sexp.CDR();
		while (!iterator.isNil()) {
			SExpr firstEntry= iterator._1st()._1st();
			if (! firstEntry.isPair()) return false;
			if (!firstEntry.CAR().isAtom()) return false;
			if (!firstEntry.CDR().CAR().isNumber()) return false;
			if (! firstEntry.CDR().CDR().isNil()) return false;
			SExpr secondEntry= iterator._1st()._2nd();
			if (! secondEntry.isPair()) return false;
			if (! secondEntry.CAR().isAtom()) return false;
			if (! secondEntry.CDR().CAR().isString()) return false;
			if (! secondEntry.CDR().CDR().isNil()) return false;
			iterator= iterator.CDR();
		}
		return true;
	}

	 
}
