package wiki;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sexpr.SExpr;
import sexpr.SExpr.ListConstructor;
import sexpr.util.SExpressions;

import static wiki.WikiConstants.*;

/**
 * Servlet implementation class for Servlet: Changes
 *
 */
 public class Changes 
                 extends javax.servlet.http.HttpServlet 
                 implements javax.servlet.Servlet
 {
     
    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Changes() {
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
			
			final SExpr.ListConstructor changeRows= new SExpr.ListConstructor();
			SExpressions.forEach( changeData, new SExpressions.SProcedure() { public void _ (SExpr arg) { 
				SExpressions.matchDispatch( 
					arg, 
					new SExpressions.SHandler(){ public static final String DISPATCH="(((?page_name ?team_name ?username ?version ?change_date ?change_comment) . _ ))";
						public void _(SExpr page, SExpr team, SExpr user, SExpr version, SExpr changeDate, SExpr changeComment) { 
							changeRows.append( S_TR.cons( SExpr.list( new SExpr[] {
										S_TD.cons(WikiUtils.internalLink(page,team,version).cons(SExpr.NIL)),
										S_TD.cons(team.cons(SExpr.NIL)),
										S_TD.cons(user.cons(SExpr.NIL)),
										S_TD.cons(changeDate.cons(SExpr.NIL))})
									));
							changeRows.append( S_TR.cons(S_TD.cons(changeComment.cons(SExpr.NIL)).cons(SExpr.NIL)));
					}});}});
			
			SExpr page= S_PAGE.cons(
					S_TABLE.cons(changeRows.make()).cons(SExpr.NIL));
			DefaultHtmlRenderer.render("Changes", page,response.getOutputStream());
		}catch(Exception ex) {throw new WikiException(ex);}
	}
}