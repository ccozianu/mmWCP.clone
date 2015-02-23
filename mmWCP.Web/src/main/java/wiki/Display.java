package wiki;

import home.costin.util.ByteSource;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.mywiki.WikiApp;
import me.mywiki.j2ee.J2EEWebInit;
import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.util.SExpressions;
import static wiki.WikiConstants.*;

/**
 * Implements the display view of a wiki page
 */
public class Display 
                extends HttpServlet 
{
    WikiApp theApp;
    @Override
    public void init() throws ServletException {
        this.theApp= J2EEWebInit.theApp(getServletContext());
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		process(request,response);
	}
	
	private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
		try {
		final WikiContext ctx= WikiFilter.wikiContext();
		SExpr pageContent= ctx.pageSExpr(); // when it comes after an edit
		SExpr versionInfo= WikiMeta.findBestVersionMatch(ctx.page(),ctx.user(),ctx.team());
		
		if (pageContent == null) { // get it from the page
			IStorage storage= theApp.wikiStorage();
			
			Integer version= ctx.version() != null ? ctx.version() 
							: versionInfo.CAR().intValue();
			if (version==null) { // page does not exist
				getServletContext().getRequestDispatcher("edit.jsp").forward(request,response);
				return;
			}
			ByteSource bs= storage.open(ctx.page(), "sexpr",ctx.team()+"."+version);
			if (bs==null) {
				getServletContext().getRequestDispatcher("/edit.jsp").forward(request,response);
				return;
			}
			pageContent= new RRepresentation().readFrom(bs);
		}
		
		SExpressions.appendToList(pageContent.CDR(),SExpr.ATOM("hr"));
		SExpressions.appendToList(pageContent.CDR(),SExpr.list(new SExpr[] {
				S_A,
				SExpr.ATOM("href").cons(SExpr.make("edit.jsp?page="+ctx.page()
							+"&team="+ctx.team()
						).cons(SExpr.NIL)),
				SExpr.make("Edit this page")
				}));
		SExpressions.appendToList( pageContent.CDR(),
				SExpr.ATOM("viewpoints").cons(
				SExpr.ATOM("ul").cons(
				SExpressions.map(
						versionInfo.CDR(),
						// the arg will be the team_name string
						new SExpressions.SFunction() {public SExpr _ (SExpr teamName) {
							return 	SExpr.ATOM("li").cons(SExpr.ATOM("a").cons(
												(SExpr.make(ctx.page())
													.cons(teamName.cons(SExpr.NIL))).cons(teamName.cons(SExpr.NIL))));
							}} ))));
		//SExpressions.
		DefaultHtmlRenderer.render(ctx.page(), pageContent,response.getOutputStream());
		response.flushBuffer();
		} catch(Exception ex) { throw new WikiException(ex);}
	}
	
}
