package wiki;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.Filter;import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.postgresql.ds.PGPoolingDataSource;
;

public class WikiFilter implements Filter {
 
	static ThreadLocal wikiCtx= new ThreadLocal();
	public static WikiContext wikiContext() { return (WikiContext) wikiCtx.get();}
		
	
	static ThreadLocal dbConnection_ = new ThreadLocal();
	static PGPoolingDataSource source = new PGPoolingDataSource();
	static {
	        System.err.println("Wiki Filter initializer ... ");
		try {
			source.setDataSourceName("A Data Source");
			source.setServerName("127.0.0.1");
			source.setPortNumber(15432);
			source.setDatabaseName("wcp");
			source.setUser("wcp0");
			source.setPassword("blah");
			//source.
			source.setMaxConnections(10);
			Class.forName("org.postgresql.Driver");
			System.err.println("Postgresql driver loaded");
		}
			catch(Exception e) {
				System.err.println(e);
			}			
		}
	public static Connection dbConnection() throws SQLException {
		Connection result = (Connection) dbConnection_.get();
		if (result==null) {
			dbConnection_.set(result= source.getConnection());
			result.setAutoCommit(false);
			System.err.println("got db connection");
		}
		return result;
	}
	
	private static void closeDbConnection() {
		Connection c= (Connection) dbConnection_.get();
		try {		if (c != null  && (!c.isClosed())) {
					c.close();
			        }}
			catch (SQLException ex) {
				System.err.println(ex);
				ex.printStackTrace(System.err);
			}
			finally { dbConnection_.set(null);}
	} 
		
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
		wikiCtx.<WikiContext>set(WikiContext.fromRequest((HttpServletRequest)request));
		((HttpServletResponse) response).addHeader("Content Type","text/html");
		((HttpServletResponse)response).addCookie(new Cookie("username",((WikiContext)wikiCtx.get()).user()));
		chain.doFilter(request,response);
		}
		finally {
			wikiCtx.set(null);
			closeDbConnection();
		}
	}
	
	
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	public static void commit() throws SQLException {
		if (dbConnection_.get() != null) {
			((Connection) dbConnection_.get()).commit();
		}
	}

}
