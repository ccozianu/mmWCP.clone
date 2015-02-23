/**
 * 
 */
package wiki;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.regex.Matcher;


import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.SRepresentation;
import sexpr.util.SExpressions;

public class DefaultHtmlRenderer  {
	
	
	
	
	static class WikiPatternMatcher implements SExpressions.SHandler, Cloneable {
		final static String DISPATCH="(" +
				"((h ?level . ?rest) . onHeader)" +
				"((p . ?rest) . onP)" +
				"((ul . ?rest) . onUL)" +
				"((li . ?rest) . onLI)" +
				"((table . ?rest) . onTable)" +
				"((tr . ?rest) . onTR)" +
				"((td . ?rest) . onTD)" +
				"(hr . onHR)" +
				"((a (href ?hrefValue)) . onExternalLink1)" +
				"((a (href ?hrefValue) ?label ) . onExternalLink2)" +
				"((a (?internalLink ?hrefValue ?version) ?label ) . onInternalLink2)" +				
				"((a (?internalLink ?team) ?label) . onInternalLink2)" +
				"((a ?internalLink ?label) . onInternalLink)"+
				"((chess . ?list) . onChess)"+
				//"(("
				"((pre  ?any) . onPre )" +
				"((viewpoints . ?list ) . onViewpoints ) " +
				"(?any . onDefault)" +
				")";
		
		OutputStream os;
		HtmlEscapedOutput htmlOs;
		
		WikiPatternMatcher(OutputStream os_) {
			this.os= os_;
			this.htmlOs= new HtmlEscapedOutput(os_);
		}
		
		//public PatternMatcher()
		public Object onDefault(SExpr sexpr) throws Exception {
			if (sexpr.isString()) { onString(sexpr);} 
			else if (sexpr.isNil()) { os.write("&nbsp;".getBytes("ASCII"));}
			else try {
				new RRepresentation().printTo(sexpr,htmlOs,new Object());
			} catch(Exception ex) {throw new RuntimeException(ex);}
			return null;
		}
		
		
		public void onP(SExpr list) throws Exception {
			os.write("<p>\n".getBytes("ASCII"));
			SExpressions.forEachDispatch(list, (SExpressions.SHandler) clone());
			os.write("</p>\n".getBytes("ASCII"));
		}
		
		public void onTable(SExpr list) throws Exception {
			os.write("<table>\n".getBytes("ASCII"));
			SExpressions.forEachDispatch(list, (SExpressions.SHandler) clone());
			os.write("</table>\n".getBytes("ASCII"));
		}

		public void onTR(SExpr list) throws Exception {
			os.write("<tr>\n".getBytes("ASCII"));
			SExpressions.forEachDispatch(list, (SExpressions.SHandler) clone());
			os.write("</tr>\n".getBytes("ASCII"));
		}

		public void onTD(SExpr list) throws Exception {
			os.write("<td>\n".getBytes("ASCII"));
			SExpressions.forEachDispatch(list, (SExpressions.SHandler) clone());
			os.write("</td>\n".getBytes("ASCII"));
		}

		public void onUL(SExpr list) throws Exception {
			os.write("<ul>\n".getBytes("ASCII"));
			SExpressions.forEachDispatch(list, (SExpressions.SHandler) clone());
			os.write("</ul>\n".getBytes("ASCII"));
		}
		
		public void onLI(SExpr list) throws Exception {
			os.write("<li>\n".getBytes("ASCII"));
			SExpressions.matchDispatch(list, (SExpressions.SHandler) clone());
			os.write("</li>\n".getBytes("ASCII"));
		}

		public void onChess(SExpr position) throws Exception {
			os.write("<pre>".getBytes("ASCII"));
			SExpressions.forEach(position, new SExpressions.SProcedure() { public void _(SExpr argument) {
					try {
					htmlOs.write((argument.asString()+'\n').getBytes("ASCII"));
					}catch(IOException ex){ throw new WikiException(ex);}
				}});
			os.write("</pre>".getBytes("ASCII"));
		}
		public void onHR() throws Exception {
			os.write("<hr />".getBytes());
		}
		
		public void onHeader(SExpr level, SExpr list) throws Exception {
			String h = "h"+level.intValue();
			os.write(("<"+h+">").getBytes("ASCII"));
			SExpressions.forEachDispatch(list, (SExpressions.SHandler) clone());
			os.write(("</"+h+">\n").getBytes("ASCII"));
		}
		
		public void onExternalLink1(SExpr href) throws IOException{
			onExternalLink2(href,href);
		}
		
		public void onExternalLink2(SExpr href, SExpr label) throws IOException {
			os.write("<a href=\"".getBytes("ASCII"));
			os.write(href.stringValue().getBytes("ASCII"));
			os.write("\" >".getBytes("ASCII"));
			htmlOs.write(label.asString().getBytes("ASCII"));
			os.write(" </a>".getBytes("ASCII"));
		}
		
		public void onInternalLink(SExpr link, SExpr label) throws IOException {
			os.write("<a href=\"display?page=".getBytes("ASCII"));
			os.write( link.asString().getBytes("ASCII"));
			
			os.write("\" >".getBytes("ASCII"));
			htmlOs.write(label.asString().getBytes("ASCII"));
			os.write(" </a>".getBytes("ASCII"));
		}
		
		public void onInternalLink2(SExpr link, SExpr team, SExpr label) throws IOException {
			os.write("<a href=\"display?page=".getBytes("ASCII"));
			os.write( link.asString().getBytes("ASCII"));
			os.write(("&team="+team.asString()).getBytes("ASCII"));
			os.write("\" >".getBytes("ASCII"));
			htmlOs.write(label.asString().getBytes("ASCII"));
			os.write(" </a>".getBytes("ASCII"));
		}

		public void onInternalLink2(SExpr link, SExpr team, SExpr version, SExpr label) throws IOException {
			os.write("<a href=\"display?page=".getBytes("ASCII"));
			os.write( link.asString().getBytes("ASCII"));
			os.write(("&team="+team.asString()).getBytes("ASCII"));
			os.write(("&version="+version.asString()).getBytes("ASCII"));
			os.write("\" >".getBytes("ASCII"));
			htmlOs.write(label.asString().getBytes("ASCII"));
			os.write(" </a>".getBytes("ASCII"));
		}
		
		public void onString(SExpr s) throws IOException {
			htmlOs.write(s.asString().getBytes("ASCII"));
		}
		
		public void onPre(SExpr s) throws IOException {
			//SExpr parsed= 
			os.write("<pre>\n".getBytes("ASCII"));
			htmlOs.write(s.asString().getBytes("ASCII"));
			os.write("\n</pre>".getBytes("ASCII"));
		}
		
		public void onViewpoints(SExpr list) throws Exception {
			//os.write("<hr />".getBytes("ASCII"));
			os.write(("<div id='viewpoints' >\n" +
					"<h3> Alternative ViewPoints </h3>").getBytes("ASCII"));
			SExpressions.matchDispatch(list, (SExpressions.SHandler) clone());
			os.write("</div>".getBytes("ASCII"));
		}
	}
	
	public static void render(String pageName, String input, final OutputStream os) throws IOException {
		SExpr page=WikiTextToSExpr.parse(input);
		render(pageName, page,os);
	}
	
	public static void render(final String pageName, final SExpr page, final OutputStream os) {
		try {
		SExpressions.matchDispatch(page, new SExpressions.SHandler(){
			public static final String DISPATCH="(((page . ?rest) . process))";
			public void process(SExpr list) throws Exception {
				HtmlEscapedOutput htmlOS= new HtmlEscapedOutput(os);
				os.write("<html>\n".getBytes("ASCII"));
				os.write("<head>\n".getBytes("ASCII"));
				os.write("<title>".getBytes("ASCII")); 
				htmlOS.write(pageName.getBytes("ASCII"));
				os.write("</title>".getBytes("ASCII"));
				os.write("<link rel=\"stylesheet\" type=\"text/css\" href=\"wiki.css\" media=\"screen\">".getBytes("ASCII"));
				os.write("</head>\n".getBytes("ASCII"));
				os.write("<body>\n".getBytes("ASCII"));
				os.write("<table cellspacing=0> <tr > <td id='heading' >".getBytes("ASCII"));
				os.write(("<a href=\"reverse?page="+WikiUtils.escapeHtmlAtribute(pageName) + "\" >").getBytes("ASCII"));
				htmlOS.write(pageName.getBytes("ASCII"));
				os.write("</a> </tr> </td>".getBytes("ASCII"));
				os.write("<tr> <td>".getBytes("ASCII"));
				SExpressions.forEachDispatch(list, new WikiPatternMatcher(os));
				os.write("</tr> </td>".getBytes("ASCII"));
				os.write("</table>".getBytes("ASCII"));
				os.write("</body>\n".getBytes("ASCII"));
				os.write("</html>\n".getBytes("ASCII"));
				
			}
		});		
		}catch(Exception ex) {
			throw new WikiException(ex);
		}
	}

	private static final String TESTSTRING = "`(h 1 \"Hello World ThisIsWikiLink\") " +
               "`(a (href \"http://c2.com/cgi/quickChanges?\\\"days=1&min=0\" ) \"Explicit Link\") " +
               "\n And the next paragraph comes here";

	public static void main( String args[]) {
		try {
			SExpr parsed= WikiTextToSExpr.parse(TESTSTRING);
			new RRepresentation().printTo(parsed,System.err,null);
			
			render("TEST PAGE",parsed, System.out);
		}
		catch(Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
	}
}