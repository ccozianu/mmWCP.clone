package sexpr.matching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.util.SExpressions;

/**
 * a class for an extended pattern matching mechanism
 * to be used especially for wiki templates
 * 
 */

public class XPatterns {
	
	/**
	 * value wrapper for the function compile
	 */
	static final SExpressions.SFunction<XMatcher> compiler= new SExpressions.SFunction<XMatcher> (){ public XMatcher _(SExpr arg) {
			return compile(arg);
		}}; 
	
	public static class MatchFailure extends RuntimeException { }
	
	/**
	 * defines the type of a fold operator (function) that accumulates the 
	 * results over pattern matching
	 * to be used by the client of a Pattern matching operation
	 * @author ccozianu
	 */
	public static interface foldOperator { Object _ ( Object left, SExpr right); }
	
	static class XEnvironment extends HashMap<String, SExpr>{
		
	}
	
	static final String match0="($LET () ?any ($OUT ?any))";
	static final String match1="($LET () (td . ?content) ( $OUT (td ?content)))";
	static final String match2= 
			"($LET (" +
				"($td (td . _ ) ($OUT ?_) )" +
				"($chess (chess . rest) ($native ) )"+
				"($any ?any ?any)"+
				"($even-any (?any ?any) ?any )"+
				"(tr (tr . ($* $td)) (\"<tr >\\n\" $*  \"</tr>\\n\"))" +
				"(all ($OR $tr $td $chess $any) )"+
				"(repeat-all ($* all))"+
			")" +
			"($repeat-all)" +
						")";

	
	
	private static XMatcher compileSpecial(final String keyWord, SExpr arguments) {
		if (keyWord.equalsIgnoreCase("let")) {
			return new LetMatcher(arguments);
		}
		else if (keyWord.equals("*")) {
			return compileStar(arguments);
		}
		else if (keyWord.equalsIgnoreCase("or")) {
			return new OrMatcher(SExpressions.collectAsList(arguments,compiler));
		}
		return new XMatcher() { public XMatchResult applyTo(SExpr data, XContext env) {
				XMatcher matcher= env.lookupPattern(keyWord);
				if (matcher==null) throw new MatchFailure();
				return matcher.applyTo(data, new XContext(env));
			}};
	}
	



	private static StarMatcher compileStar(SExpr arguments) {
		ArrayList result= new ArrayList();
		OutMatcher out= null;
		while (!arguments.isNil()) {
			SExpr patternEx=arguments.CAR(); 
			if (patternEx.isPair() && patternEx._1st().equals(SExpr.ATOM("$OUT"))) {
				out= new OutMatcher(patternEx.CDR());
				break;
			}
			else {
				result.add(simpleCompile(arguments.CAR()));
			}
			arguments= arguments.CDR();
		}
		return new StarMatcher((XMatcher[])result.toArray(new XMatcher[]{}),
				 				out);
	}


	private static XMatcher compileVariable(final String name) {
		class XVarMatcher extends XMatcher { public XMatchResult applyTo(SExpr data, XContext ctx) {
			ctx.addOrVerifyIfPresent(name, data);
			/*SExpr boundValue= ctx.env.get(name);
			if ( boundValue == null) {
					ctx.env.put(name,data);
			}
			else { 
				//ensure that the value we meet is the same
				//as that previously bound by matching
				if (! boundValue.equals(data)) throw new MatchFailure();
			}*/
					
			return new XMatchResult(SExpr.NIL,ctx);
		}}
		return new XVarMatcher();
	}
	
	private static XMatcher simpleCompile(final SExpr pattern) {
		if (!pattern.isPair() ) { return compileSingleton(pattern);}
		SExpr _1st= pattern.CAR();
		if (_1st.isAtom() 
				&& _1st.atomValue().charAt(0)=='$') {
			return compileSpecial(_1st.atomValue().substring(1),pattern.CDR());
		}
		
		return SEQUENCE(simpleCompile(_1st), simpleCompile(pattern.CDR()));
	}

	
	private static XMatcher SEQUENCE(final XMatcher carM, final XMatcher cdrM) {
		return new XMatcher() {
			@Override
			public XMatchResult applyTo(SExpr data, XContext ctx) {
				
				if (carM instanceof XPartialMatcher) {
					XMatchResult result= carM.applyTo(data,ctx);
					data=result.leftData;
				}
				else {
					if (data.isNil())
						throw new MatchFailure();
					carM.applyTo(data.CAR(),ctx);
					data= data.CDR();
				}
				return cdrM.applyTo(data,ctx);
			}
		};
	}

	//
	// universal matcher
	private static final SExpr ANY= SExpr.ATOM("_");
	private static XMatcher compileSingleton(final SExpr pattern) {
		if (isVariable(pattern)) {
			//if  
			return compileVariable(pattern.atomValue()); 
		}
		
		if (ANY.equals(pattern)){
			return new XMatcher(){@Override
			public XMatchResult applyTo(SExpr data, XContext ctx) {
				return new XMatchResult(data,ctx);
			}};
		}
		
		return new XMatcher() { public XMatchResult applyTo(SExpr data, XContext ctx) {
				if (! pattern.equals(data)) { throw new MatchFailure();}
				return new XMatchResult(SExpr.NIL,ctx);
			}
		};
	}


	private static boolean isVariable(final SExpr arg) {
		return arg.isAtom() && arg.atomValue().charAt(0)=='?';
	}

	public static XMatcher compile(final SExpr pattern ) {
		return simpleCompile(pattern);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
		RRepresentation parser= new RRepresentation();
		
		System.out.println("Testing pattern0: ");
		SExpr pattern0= parser.readFrom(match0);
		System.out.println(compile(pattern0).applyTo(SExpr.make("test"),new XContext()));
		
		SExpr pattern= parser.readFrom(match2);
		parser.printTo(pattern,System.out,null);
		}catch(Exception ex ) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}

	}

	/**
	 * returns the name of the var as string if the S-Expression is a variable
	 * or null otherwise
	 */
	public static String varNameOf(SExpr arg) {
		// TODO Auto-generated method stub
		if (arg.isAtom()) {
			String s= arg.atomValue();
			if (s.length() >=2 
				&& s.charAt(0)== '?'
				&& s.charAt(1) != '?' ) {
				return s;
			}
		}
		return null;
	}

}
