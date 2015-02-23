package wiki;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.SExpr.ListConstructor;
import sexpr.matching.XMatcher;
import sexpr.matching.XPatterns;
import sexpr.util.SExpressions;

/**
 * Servlet implementation class for Servlet: Changes
 *
 */
 public class NewChanges 
                 extends javax.servlet.http.HttpServlet 
 {
	 
	 XMatcher transformer= XPatterns.compile(
			 new RRepresentation().readFrom(
			 "($LET ()" +
			 "(($* (?page_name ?team_name ?username ?version ?change_date ?change_comment) " +
			 "		($OUT (tr (td (a (?page_name ?team_name ?version) ?page_name))" +
			 "				  (td ?team_name)" +
			 "				  (td ?username)" +
			 "				  (td ?version)" +
			 "				  (td ?change_date)" +
			 
			 		"))" +
			 "))" +
			 "($OUT (page (table $INNER)))" +
			 ")"
			 ));
	 
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public NewChanges() {
		super();
	}   	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}  	
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}

	private void process(HttpServletRequest request, HttpServletResponse response ) throws ServletException,IOException {
		try {
			String days= request.getParameter("days"); if (days == null) days="1";
			Calendar startTime= Calendar.getInstance();
			startTime.add(Calendar.DAY_OF_YEAR,-Integer.parseInt(days));
			SExpr changeData= WikiMeta.changesSince(startTime);
			SExpr page= transformer.applyTo(changeData).expr; 
			DefaultHtmlRenderer.render("Changes", page,response.getOutputStream());
		}catch(Exception ex) {throw new WikiException(ex);}
	}
}