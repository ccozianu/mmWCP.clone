package sexpr.matching;

import sexpr.SExpr;

/**
 * the result of matching a pattern against data can be 
 * a pair of environment containing bound variables (name -> SExpr) maps
 * and/or an SExpr of the pattern has also term rewriting clause
 * @author ccozianu
 */
public class XMatchResult {
	public final SExpr expr; 
	final XContext ctx;
	public final SExpr leftData;
	XMatchResult(SExpr expr_, XContext ctx_) {this (expr_,ctx_,null);};
	
	XMatchResult(SExpr value_, XContext ctx_, SExpr leftData_ ){
		this.expr= value_; this.ctx = ctx_; this.leftData= leftData_;
	}
	
	/**
	 * utility method, forwards to env
	 */
	public SExpr valueOf(String varName) { return ctx.valueOf(varName); };
}