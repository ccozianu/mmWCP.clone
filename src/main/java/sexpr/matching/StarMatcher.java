
package sexpr.matching;

import sexpr.SExpr;
import sexpr.matching.XPatterns.MatchFailure;

/**
 * ($* PATTERN1 PATTERN2 ... PATTERN_N )
 * matches 
 * ( <elem1_1 elem1_2 ... elem1_n > ... <elem_k_1 elem_k_2 ... elem_k_n> )
 * where PATTERN_i matches elem_j_i for all i<=i<=n, 1<=j<=k 
 * @author Costin Cozianu
 */

public class StarMatcher extends XMatcher implements XPartialMatcher {
	XMatcher [] innerPattern;
	OutMatcher outStmt;
	/*
	 * 
	 public StarMatcher(XMatcher [] inner) {
		if (inner.length==0) throw new IllegalArgumentException("StarMatcher cannot have empty contents ");
		this.innerPattern= inner;
	}
	*/
	
	public StarMatcher(XMatcher [] inner, OutMatcher outStmt_) {
		
		if (inner.length==0) throw new IllegalArgumentException("StarMatcher cannot have empty contents ");
		this.innerPattern= inner;
		this.outStmt = outStmt_;
		
	}
	
	
	@Override
	/**
	 * first we match exhaustively to the right
	 * TODO: set backtracking points along the way
	 */
	public XMatchResult applyTo(SExpr data, XContext ctx) {
		if (!data.isPair()) {
			return new XMatchResult(SExpr.NIL,ctx,data);
		}
		XMatchResult result;
		//SExpr pointer0=data;
		XContext.SavePoint trans= null;
		SExpr pointer0=null;
		SExpr pointer1=data;
		try {
			
			while (true) { //try to match as many as possible to the right
				trans= ctx.savePoint();
				XContext innerContext= new XContext(ctx,true);
				pointer0=pointer1;
				if (pointer1.isPair() ) {
					for (XMatcher current: innerPattern) {
						if( current instanceof XPartialMatcher) {
							XMatchResult result1=current.applyTo(pointer1,innerContext);
							//if (result1.expr != null) ctx.addInnerValue(result1.expr);
							pointer1= result1.leftData;
							}
						else {
							if (! pointer1.isPair() ) throw new MatchFailure();
							XMatchResult result1= current.applyTo(pointer1.CAR(),innerContext);
							//if (result1.expr != null) ctx.addInnerValue(data);
							pointer1 = pointer1.CDR();
							}
						}
					if (outStmt != null) { 
						ctx.addInnerValue(outStmt.compute(innerContext));}
					ctx.commit();
					}
				else {
					result= new XMatchResult(SExpr.NIL,ctx,pointer1);
					break;// EOL or atom (non-list
					}
				}
			
		}
		catch (MatchFailure failure) {
			// it's ok, the matching happened till the last unsuccessful
			ctx.cutTo(trans);
			ctx.commit();
			result= new XMatchResult(SExpr.NIL,ctx,pointer0);
		}
		if (pointer1.isNil() || pointer1.isPair())
			return result;
		throw new MatchFailure();
	}


	
}
