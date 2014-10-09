package sexpr.tests;

import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.util.SExpressions;
import junit.framework.TestCase;

public class MatchTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(MatchTest.class);
	}

	
	String [][]matchTests= {
			{"()","()"}
			,{"()","?x"}
			,{"(a)","?x"}
			,{"a","?x"}
			,{"(a . 1)","(?x . ?y)"}
			,{"(p 1 2)","(p . ?y)"}
			,{"(\"p\" (1 2 3 4))","(?x  (1 . ?y))"}
	};
	/*
	 * Test method for 'sexpr.util.SExpressions.MATCH(SExpr, SExpr)'
	 */
	public void testMATCH() throws Exception {
		RRepresentation r= new RRepresentation();
		for (int i=0;i<matchTests.length;i++) {
			SExpr binding= SExpressions.MATCH( r.readFrom(matchTests[i][0]),
											   r.readFrom(matchTests[i][1]));
			System.out.println(binding);
		}
	}

}
