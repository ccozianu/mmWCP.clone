/**
 * 
 */
package sexpr.matching;

import sexpr.SExpr;

public abstract class XMatcher {
	public abstract XMatchResult applyTo (SExpr data, XContext env);
	public XMatchResult applyTo(SExpr data) {
		return applyTo(data, new XContext());
	}
}