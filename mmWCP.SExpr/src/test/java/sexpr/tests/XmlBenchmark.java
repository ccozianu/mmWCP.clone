/*
 * Created on May 11, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package sexpr.tests;

import home.ccozianu.benchmark.AbstractBenchmarked;
import home.ccozianu.benchmark.Benchmark;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @author ccozianu
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class XmlBenchmark extends AbstractBenchmarked {

	//String bench;
	byte[] byteArray;
	boolean generateOutput=false;
	int count=100000;
	{
		//ByteArrayOutputStream bout= new ByteArrayOutputStream(20000000);
		if (generateOutput)
		try {
		OutputStream bout= new BufferedOutputStream(new FileOutputStream("benchmark.xml"),512);
		PrintStream ps= new PrintStream(bout);  
		ps.print("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<root> ");
		for (int i=0; i< count; i++){
			ps.print("<y> <Test");
			ps.print(i%2000);
			ps.print(">\n");
			ps.print(i);
			ps.print("</Test");ps.print(i%2000);ps.print(">");
			ps.print("\t<X");	ps.print(i%2000);ps.print(">\n ");
			ps.print(i);
			ps.print("</X");ps.print(i%2000);ps.print(">\n</y>\n");
		}
		/*for (int i=count-1; i >= 0; i--) {
			ps.print("</X");ps.print(i%2000);ps.print(">\n");
			ps.print("</Test");ps.print(i%2000);ps.print(">\n");
			ps.print("</y>\n");
		}*/
		/*for (int i=89; i >= 0; i--)
			builder.append(" </X"+i+"> "+i+" </Test"+i+"> "+i);*/
		ps.print("   </root> ");	
		ps.flush();
		ps.close();
		System.gc();
		System.out.println("Data size (kb)"+(new File("benchmark.xml").length()/1024.0));
		}	
		catch(IOException ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
			throw new RuntimeException(ex);
		}

		try {
			RandomAccessFile file =new RandomAccessFile("benchmark.xml","r");
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
	}
	
	/* (non-Javadoc)
	 * @see home.costin.benchmark.Benchmarked#doAction(int)
	 */
	 
	public void doAction(int repeatCount) throws Exception {
		DocumentBuilderFactory factory =
		  DocumentBuilderFactory.newInstance();
		for (int i=0; i< repeatCount; i++) {
			//BufferedInputStream is= new BufferedInputStream(new FileInputStream("benchmark.xml"),512);
			ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
			DocumentBuilder builder= factory.newDocumentBuilder();
			builder.parse(is);
			is.close();
		}
	}

	public static void main(String[] args) {
		try
		{
		int repeatAction=
			args.length>0 ? Integer.valueOf(args[0]).intValue() : 1;
		int repeatBenchmark=
			args.length>0 ? Integer.valueOf(args[1]).intValue() : 1;

		Benchmark bm= new Benchmark(new XmlBenchmark(), repeatAction, repeatBenchmark); 
		bm.benchmark();
		System.out.println(
			"Benchmark XML,"+repeatAction+','+repeatBenchmark+") : "
				+ home.costin.util.StringFormatter.formatDouble(bm.getResult(),12,',','.')
				+ "ms");
		}
		catch (Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);		
		}		
	}
}
