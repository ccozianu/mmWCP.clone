package wiki;

import home.costin.util.ByteSource;

import java.io.IOException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
//import static java.util.regex.Pattern.*;
//the above does not work in Eclipse yet
import java.util.regex.Pattern;

import javax.swing.ListCellRenderer;


import sexpr.RRepresentation;
import sexpr.SExpr;
//import sexpr.SExpr.*;
import sexpr.SExpr.ListConstructor;
import sexpr.util.SExpressions;

/**
 * Defines the transofrmation from a text 
 * as edited by the user in a html textbox
 * To an s-expression
 * The spec for this transformation are as follows:
 *  
 */
public class WikiTextToSExpr {
	
	static RRepresentation sParser= new RRepresentation();
	final static SExpr EOP= SExpr.ATOM("/p"),
						P= SExpr.ATOM("p");
	
	//static Pattern WikiStyleLink

	public static SExpr parse(final String s) throws IOException {
		SExpr.ListConstructor split=new SExpr.ListConstructor();
		int i=0;
		String remaining= s;
		//to begin with extract the S-Expressions
		while ((i=remaining.indexOf('`')) != -1) {
			if (i>0)
				parseUrlsAndNewLines(remaining.substring(0,i),split);
			remaining= remaining.substring(i+1); //swallow the `
			ByteSource bs= ByteSource.make(remaining);
			SExpr se= sParser.readFrom(bs); 
			if (!se.isNil()) // avoid NIL 
				split.append(se);
			int readCount= bs.getReadBytesCount();
			//skip one whitespace if that's what terminated
			// the sexpr
			char c;
			if (Character.isWhitespace(c=s.charAt(readCount))
					&& c!='\n' //\n is very important !
						)
				readCount++;
			if (readCount<remaining.length())
				remaining= remaining.substring(readCount);
			else 
				remaining="";
		}
		if (remaining.trim().length()>0) {
			parseUrlsAndNewLines(remaining,split);
		}
		SExpr list= split.make();
		
		final ListConstructor page= new ListConstructor();
		page.append(SExpr.ATOM("page"));
		final ListConstructor currentP= new ListConstructor();
		currentP.append(P);
		SExpressions.forEach(list, 
			new SExpressions.SProcedure() { public void _(SExpr arg) {
				if (arg.isNil()) return;
				if (EOP.equals(arg)) {
					SExpr thisP=currentP.make();//this also resets currentP
					currentP.append(P);
					if (!thisP.CDR().isNil())
						page.append(thisP);  
				}
				else {
					currentP.append(arg);
				}
			}});
		//last P does not get an EOP marker
		SExpr thisP=currentP.make();//this also resets currentP
		if (!thisP.CDR().isNil())
			page.append(thisP);  
			/**/

		return page.make();
	}
	
	private static ThreadLocal pURL= new ThreadLocal();
	private static Pattern getUrlPattern() {
		Pattern result= (Pattern)pURL.get();
		if (result==null) {
			pURL.set( 
			result= Pattern.compile(
					"([a-z]{3,5}+://[^ ]+)" 
					+"|((?:[A-Z][a-z]+){2,})" 
					+"|(([A-Za-z0-9]*_[A-Za-z0-9]*)+)"
				));
			}
		return result;
	}
	
	private static void parseUrlsAndNewLines(String text, ListConstructor accumulator) {
		Matcher m= getUrlPattern().matcher(text);
		int beginIndex=0;
		int lastIndex=text.length(); // the start index of the region before a URL match
		while (m.find()) {
			lastIndex= m.start();
			if (lastIndex>beginIndex){ // avoid appending empty strings
				parseNewLines(text.substring(beginIndex,lastIndex),accumulator);
				//accumulator.append(SExpr.make(text.substring(beginIndex,lastIndex)));
				}
			beginIndex= m.end();	
			String matched=m.group(0);
			if (m.group(1) != null ) { //external URL
				accumulator.append(SExpr.ATOM("a").cons(
									(SExpr.ATOM("href").cons(SExpr.make(matched).cons(SExpr.NIL)))
									.cons(SExpr.NIL)));
				}
			else if (m.group(2)!= null ) { // CamelCase
				accumulator.append(SExpr.ATOM("a").cons(
								   			SExpr.make(WikiMeta.normalizeUpperCaseTitle(matched)).cons(
								   			SExpr.make(matched).cons(
								   		    SExpr.NIL))));
				}
			else if (m.group(3)!=null) {
				accumulator.append(SExpr.ATOM("a").cons(
											SExpr.make(WikiMeta.normalizeUnderscoredTitle(matched)).cons(
											SExpr.make(matched)
											.cons(SExpr.NIL))));
			}
		}
		// at the end append the last portion left
		if (beginIndex<text.length()) {
			parseNewLines(text.substring(beginIndex,text.length()),accumulator);
			//accumulator.append(SExpr.make(text.substring(beginIndex,text.length())));
		}
	}
	
	
	
	private static void parseNewLines(String text, ListConstructor accumulator) {
		StringTokenizer strtok= new StringTokenizer(text,"\n");
		boolean firstTimeLatch=false;
		while (strtok.hasMoreTokens()){
			if (firstTimeLatch) {
				accumulator.append(SExpr.ATOM("/p"));//end of paragraph marker
			}
			else {firstTimeLatch=true;}
			String next=strtok.nextToken();
			if (next.trim().length()>0) {
				accumulator.append(SExpr.make(next));
				// paragraph end marker
			}
		}
	}

	public static void main(String args[]) {
		String demo= "`(h 1 \"This is a wiki\" )\n"
			+" And an SExpr inside the text `( ala bala () ((ab cd) (x y)) )"
			+" And an SExpr on its own paragraph \n `(oo)"
			+" And a WardsWikiLink inside the text\n" 
			+" And consecutive empty lines should be removed \n\n\t \n \t\n"
			
			+"Ok test. `() Yup\"ee ."
			+" And new style links X_Y +\n"
			+"And even an external URL http://www.c2.com/cgi/wiki ."
			+ "And more text`b x`/b `i `/i";
		try {
			SExpr parsed= parse(demo);
			sParser.printTo(parsed,System.out,null);
			System.out.flush();
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.err.println(ex);
		}
	}
}
