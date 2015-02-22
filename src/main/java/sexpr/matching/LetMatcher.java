package sexpr.matching;

import sexpr.SExpr;

/**
 * Matcher for 
 * ($LET ( <pattern defs>* ) matchExpr [($OUT ...)])
 */
public class LetMatcher extends XMatcher {
	
	final SExpr defList;
	final SExpr pattern;
	final XMatcher toMatch;
	final SExpr rest;
	final OutMatcher outStmt;
	
	/**
	 * From a list ($LET ( <pattern defs> * ) matchExpr [($OUT ...)])
	 * The framework will match and consume $LET <br>
	 *     while this constructor will get the rest: <br>
	 *     (( <pattern defs> * ) matchExpr [($OUT ...)
	 */
	public LetMatcher(final SExpr arguments) {
		defList= arguments.CAR();
		pattern=  arguments.CDR().CAR();
		toMatch= XPatterns.compile(pattern);
		rest = arguments.CDR().CDR();
		if (!rest.isNil()) {
			assert(rest.CAR().isPair());
			assert(rest.CAR().CAR().equals(SExpr.ATOM("$OUT")));
			outStmt= new OutMatcher(rest.CAR().CDR());
			}
		else 
			outStmt= null;
		}

	
	@Override
	public XMatchResult applyTo(SExpr data, XContext ctx) {
		XMatchResult result= toMatch.applyTo(data,ctx);
		if (outStmt != null ) {
			SExpr outValue= outStmt.compute(ctx);
			result= new XMatchResult(outValue,ctx);
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
