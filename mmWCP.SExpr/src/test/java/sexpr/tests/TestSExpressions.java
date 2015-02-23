package sexpr.tests;

import java.util.List;

import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.util.SBuilder;
import sexpr.util.SExpressions;
import junit.framework.TestCase;

public class TestSExpressions extends TestCase {
	RRepresentation parser=new RRepresentation();
	public static void main(String[] args) {
		junit.textui.TestRunner.run(TestSExpressions.class);
	}

	/*
	 * Test method for 'sexpr.util.SExpressions.map(SExpr, SFunction)'
	 */
	public void testMap() throws Exception{
		
		SExpr input= parser.readFrom("((1 2 3) (4 5))");
		SExpr expected= parser.readFrom("(6 9)");
		assertEquals(
				expected, 
				SExpressions.map(input, new SExpressions.SFunction() { public SExpr _(SExpr list)  {
						final int s[]={0};
						SExpressions.forEach(list, new SExpressions.SProcedure(){public void _ (SExpr elem) {
							s[0] += elem.intValue();
						}});
						return SExpr.make(s[0]);
					}}));
	}
	public void testWalk() throws Exception{
		
		
		SExpr input= parser.readFrom("((1 2 3) (4 5))");
		SExpr expected= input;
		
		assertEquals(
				expected, 
				SExpressions.walk(input,new SBuilder()));
	}
	
	public void testCollectAsList () {
		SExpr input= parser.readFrom("(1 2 3 4 5)");
		List<String> result= SExpressions.collectAsList(input, new SExpressions.SFunction<String> (){public String _(SExpr arg)  {
			return arg.toString();
		}});
		assertEquals(5,result.size());
		assertEquals(result.get(4),"5");
	}
	

}
