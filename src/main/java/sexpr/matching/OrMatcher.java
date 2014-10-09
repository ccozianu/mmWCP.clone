package sexpr.matching;

import java.util.List;

import sexpr.SExpr;
import sexpr.matching.XPatterns.MatchFailure;

public class OrMatcher extends XMatcher {
	List<XMatcher> innerPatterns;

	public OrMatcher(List<XMatcher> args) {
		// TODO Auto-generated constructor stub
		this.innerPatterns= args;
	}

	@Override
	public XMatchResult applyTo(SExpr data, XContext ctx) {

		XContext.SavePoint trans= null;
		XMatchResult result=null;
		for( XMatcher p: innerPatterns) {
			trans= ctx.savePoint();
			try {
				result=p.applyTo(data);
				break;
			} catch ( MatchFailure failure) {
				ctx.cutTo(trans);
			}
		}
		if (result==null) throw new MatchFailure();
		
		return result;
	}

}
