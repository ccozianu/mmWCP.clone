package wikitest;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import wiki.DefaultHtmlRenderer;

/**
 * Servlet implementation class for Servlet: Hello
 *
 */
 public class Hello extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {

		private static final String TESTSTRING = "`(h 1 \"Hello World ThisIsWikiLink\") `hr " +
		"`(a (href \"http://c2.com/cgi/quickChanges?\\\"days=1&min=0\" ) \"Explicit Link\") " +
		"\n And the next paragraph comes here";
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}  	
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}  
	
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		DefaultHtmlRenderer.render("Hello World",TESTSTRING,response.getOutputStream());
	}
	
}