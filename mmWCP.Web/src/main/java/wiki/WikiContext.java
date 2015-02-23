package wiki;

import home.costin.util.ByteSource;

import java.io.Serializable;
import java.sql.SQLException;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import sexpr.SExpr;


/**
 * Class representing the context of processing a wiki request
 * - a WikiContext object is instantiated by the first code in the processing chain
 *    , currently WikiFilter
 */

public class WikiContext implements Serializable {
	
	String user_;   	public final String user()		{ return user_; } public void setUser(String username) { this.user_= username; }
	String page_;		public final String page()		{ return page_;}
	String team_;		public final String team() 		{ return team_; }
	Integer version_;	public final Integer version() 	{ return version_;} public void setVersion(Integer version) { this.version_= version; }
	
	String revisionID() {return team_+"."+version_ ; }

	transient SExpr pageExp_; 	final SExpr  pageSExpr() { return pageExp_;}
	
	
	transient String textSource_;  	final String textSource() {return textSource_;}
	
	static Object fromRequest(HttpServletRequest request) {
		WikiContext result= new WikiContext();
		Cookie cookies[]= request.getCookies();
		result.user_ =request.getParameter("username");
		if (result.user_ == null) {
			if (cookies!=null)
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("username"))
					result.user_= cookie.getValue();
				}
			if (result.user_ == null) {
				try { result.user_ = WikiMeta.anonymousUser(request.getRemoteAddr());	}
				catch (SQLException ex) {throw new WikiException("Cannot create anon user for IP:" + request.getRemoteAddr(),ex);}
				}
		}
		
		result.page_ = request.getParameter("page"); if (result.page_ != null) result.page_= WikiMeta.normalize(result.page_);
		result.version_= request.getParameter("version")!= null ? Integer.valueOf(request.getParameter("version")):null;
		result.textSource_ = request.getParameter("textSource");
		result.team_ = request.getParameter("team");
		if (result.team_==null) result.team_="0";
		return result;
	}

	public void setSExpr(SExpr expr) { this.pageExp_ = expr; }

	 
	
}
