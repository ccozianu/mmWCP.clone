package wiki;

import home.costin.util.ByteSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.mywiki.WikiApp;
import me.mywiki.j2ee.J2EEWebInit;
import sexpr.RRepresentation;
import sexpr.SExpr;

/**
 * Servlet implementation class for Servlet: Save
 *
 */
 public class Save extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
     
    private WikiApp theApp;

    /* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public Save() {
		super();
	}
	
	@Override
	public void init() throws ServletException {
	    super.init();
	    this.theApp= J2EEWebInit.theApp(getServletContext());
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
	
	protected void process(HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {
		WikiContext ctx= WikiFilter.wikiContext();
		
		try {
		
		if (ctx.textSource_ != null) {
			SExpr expr= WikiTextToSExpr.parse(ctx.textSource());
			ctx.setSExpr(expr);
			Integer version= WikiMeta.checkPageVersionForSave( ctx.page(),  ctx.team(), ctx.user(), ctx.version());
			ctx.setVersion(version);
		
			IStorage storage= theApp.wikiStorage();
			storage.save(ByteSource.make(ctx.textSource()), ctx.page(),"src",ctx.revisionID());
			ByteArrayOutputStream bOS= new ByteArrayOutputStream();
			new RRepresentation().printTo(expr,bOS,null);
			ByteSource bs= ByteSource.make(new ByteArrayInputStream(bOS.toByteArray()));
			storage.save(bs, ctx.page(), "sexpr",ctx.revisionID());
			WikiFilter.commit();
		}
		getServletContext().getRequestDispatcher("/display").forward(request,response);
		}catch (Exception ex) {
			throw new WikiException(ex);
		}
	}
}