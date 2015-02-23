package wiki;

import sexpr.SExpr;
import home.costin.util.StringConstructor;

import static wiki.WikiConstants.*;

public class WikiUtils 
{
	
	public static String escapeHtmlAtribute(String value) {
		StringConstructor sc= new StringConstructor();
		for (char c: value.toCharArray()) {
			if (c!='"') {sc.append(c);}
			else { sc.append('%');sc.append(Integer.toHexString(c));}
		}
		return sc.toString();
	}
	
	public static SExpr internalLink(SExpr page, SExpr team, SExpr version) {
		return S_A.cons( 
				page.cons( team.cons(version.cons(SExpr.NIL)))
				    .cons( page.cons(SExpr.NIL)));
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
