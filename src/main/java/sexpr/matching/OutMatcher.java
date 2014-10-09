package sexpr.matching;

import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.SUtils;
import sexpr.util.SBuilder;
import sexpr.util.SExpressions;

public class OutMatcher {

	final SExpr outTree;
	
	/**
	 * $OUT expression can be expressed as either ($OUT . <value_or_var> )
	 * or ($OUT <value_or_var> )
	 * lists have to be constructed explicitly, ex ($OUT (?var1 ?var2) )
	 * @param args
	 */
	public OutMatcher(SExpr args) { 
		this.outTree= args.isPair() ? args.CAR() : args; 
	}

	/**
	 * data should be always null
	 * otherwise we disregard it anyways
	 */
	public SExpr compute(final XContext ctx) {
		// TODO Auto-generated method stub
		//SBuilder builder= new 
		Object out= SExpressions.walk(outTree,new SBuilder() {
			
			public void onLeaf(SExpr arg) {
				if (arg.equals(SExpr.ATOM("$INNER"))) {
					SExpr innerList = ctx.innerOut();
					if (innerList != null ) {
						SExpressions.forEach(innerList, new SExpressions.SProcedure(){ public void _(SExpr element) {
							//TODO: clarify this recursion
							onLeaf(element);
							}});
					}
					System.out.println(ctx.innerOut());
				}
				else {
				arg= ctx.substVarWithValue(arg);
				super.onLeaf(arg);
				}
			}
			});

		return (SExpr)out;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
