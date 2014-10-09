package sexpr.util;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sexpr.RRepresentation;
import sexpr.SExpr;
import sexpr.SExpr.ListConstructor;
import sexpr.matching.XMatcher;

/**
 * various EssExpression
 * algorithmic and language goodies
 * @author Costin Cozianu
 */
public class SExpressions {
	
	public static interface SProcedure {
		public void _(SExpr argument) ;
	}
	
	public static interface SFunction <Result> {
		public Result  _ (SExpr arg)  ;
	}
	
	/**
	 * classes implementing this interface
	 * can and should also have a static final Object[][] DISPATCH=
	 * {{"(p . ?rest )","onP" }
	 *  {"/b","onEndBold")} }
	 *  mapping a SExpr structure to match, with ?xxx denoting variable binding
	 * 			mapping
	 *  to the name of the method to be called. If all matches fail for particular SExpr
	 *  onDefault will be called 
	 *  the index 0 on eacxh row can be either a string or an S-Expression
	 * @author Costin Cozianu
	 */
	public static interface SHandler {}
	
	
	public interface SMethod  {
		Object _(Object o, SExpr s) ;
	}
	
	public static Object matchDispatch(SExpr sexpr, SHandler match) {
		Class clazz= match.getClass();
		SMethod invoker= getInvoker(clazz);
		return invoker._(match, sexpr);
	}
	
	static class MatchMethodEntry { SExpr toMatch; Method m; int arity;
		public MatchMethodEntry(SExpr toMatch_, Method m_, int arity_) {
			this.m= m_;
			this.toMatch= toMatch_;
			this.arity= arity_;
		}}
	
	private static SMethod getInvoker(Class clazz) {
		final MatchMethodEntry[] dispTable= lookupDispTableForClass(clazz);
		
		return new SMethod() { public Object _ (Object o, SExpr arg) {
				for (int i=0; i<dispTable.length; i++) {
					if (dispTable[i]!=null ) {
					SExpr bindings= MATCH(arg,dispTable[i].toMatch);
					if (bindings!=null) {
						return doInvoke(o,dispTable[i].m, bindings);
					}}}
				throw new RuntimeException("no match found");
			}};
	}
	
	protected static Object doInvoke(Object o, Method method, SExpr bindings)  {try {
		final ArrayList list= new ArrayList();
		//to begin with assume the bindings are in strict positional order
		forEach(bindings, new SProcedure(){public void _(SExpr arg) {
			list.add(arg.CDR()); 
			}});
		Object[] params= list.toArray();
		
			return method.invoke(o,params); 
	} catch(Exception ex) {throw new RuntimeException(ex); }}

	/**
	 * tries to match data against the pattern
	 * @param data
	 * @param pattern
	 * @return
	 */
	public static SExpr MATCH(SExpr data, SExpr pattern) {
		try {
			ListConstructor bindings= new ListConstructor();
			doMatch(data,pattern, bindings);
			return bindings.make();
		}
		catch(Shortcut s) {
			return null;
		}
	}
	
	private static void doMatch(SExpr data, SExpr pattern, ListConstructor accumulator) throws Shortcut {
		if (pattern.isAtom()){
			String name= pattern.atomValue();
			if(name.charAt(0)=='?') {
				accumulator.append(SExpr.ATOM(name.substring(1)).cons(data));
				return;
			}
			else if (!data.isAtom() || !data.atomValue().equals(name)){
				Shortcut.throwIt();
			}
			return;
		}
		else if (pattern.isPair()) {
			if (!data.isPair()) Shortcut.throwIt();
			doMatch(data.CAR(),pattern.CAR(),accumulator);
			doMatch(data.CDR(),pattern.CDR(),accumulator);
			return;
		}
		// the regular case
		if (!pattern.equals(data)) Shortcut.throwIt();
	}

	// keep a per thread lookup cache
	static ThreadLocal dispTablesForClasses= new ThreadLocal(); 
	private static MatchMethodEntry[] lookupDispTableForClass(final Class clazz) {
		HashMap map= (HashMap)dispTablesForClasses.get();
		if (map==null) {dispTablesForClasses.set(map=new HashMap());}
		
		MatchMethodEntry [] result= (MatchMethodEntry[]) map.get(clazz);
		if (result==null ) {
		result= new MatchMethodEntry[] {}; 
		try { 
		Object val= AccessController.doPrivileged( new PrivilegedAction() {public Object run() { 
			try {
				Field f=clazz.getDeclaredField("DISPATCH");
				if (f!= null) {
					f.setAccessible(true);
					return f.get(null);
				}
				return null;
			} catch (Exception ex) {
				throw new RuntimeException(ex);}
			}});
		if (val!=null) {
			if ( val instanceof Object[][]) { 
				result = arrayToDispatchTable(clazz, (Object[][])val);
			}
			else if (val instanceof String) {
				result= sexprToDispatchTable(clazz,new RRepresentation().readFrom((String)val));
			}
		}
			
		}catch(Exception ex) {
			result= new MatchMethodEntry[] {};
		}
		map.put(clazz,result);
		}
		return result;
	}

	private static MatchMethodEntry[] sexprToDispatchTable(final Class clazz, final SExpr aList) {
		final ArrayList result= new ArrayList();
		forEach(aList, new SProcedure(){public void _(SExpr arg) {
			result.add(findDispatchMethod(clazz,arg.CDR().atomValue(),arg.CAR()));
		}});
		return (MatchMethodEntry[]) result.toArray(new MatchMethodEntry[]{});
	}

	private static MatchMethodEntry[] arrayToDispatchTable(Class clazz, Object[][] dispValue) throws NoSuchMethodException {
		MatchMethodEntry[] table;
		int N= dispValue.length;
		table= new MatchMethodEntry[N];
		for (int i=0;i<N;i++){
			Object[] row= dispValue[i];
			SExpr se= row[0] instanceof SExpr ? (SExpr)	row[0]
			                                   : new RRepresentation().readFrom(row[0].toString());
			table[i]= findDispatchMethod(clazz,row[1].toString(),se); 
		}
		return table;
	}

	private static MatchMethodEntry findDispatchMethod(Class clazz, String name, SExpr toMatch ) {
		Method[] allMethods= clazz.getMethods();
		outer:
		for (int i=0;i<allMethods.length;i++) {
			Method m= allMethods[i];
			if (m.getName().equals(name)) { 
				// check that all params are S-expressions 
				Class []types= m.getParameterTypes();
				for (int j=0; j<types.length;j++) {
					if ( ! SExpr.class.isAssignableFrom(types[j]))
						continue outer;
				}
				final MatchMethodEntry result= new MatchMethodEntry(toMatch,m,0);
				forAllLeafsDo(toMatch, new SProcedure () { public void _(SExpr arg) {
					if 	(arg.isAtom() && arg.atomValue().charAt(0)=='?') result.arity++;
					}});
				if (result.arity!=types.length) {
					continue outer;
				}
				m.setAccessible(true);
				return result;
			}
		}
		return null;
	}
	/**
	 * Calls the procedure for all the elements in the list
	 * @param expr
	 * @param proc
	 */
	public static void forEach(SExpr list, SProcedure proc){
		SExpr head=list;
		while (!head.isNil()) {
			proc._(head.CAR());
			head=head.CDR();
		}
	}
	
	public static void forEachDispatch(SExpr list, SHandler matcher) throws Exception {
		SExpr head=list;
		while (!head.isNil()) {
			matchDispatch(head.CAR(),matcher);
			head=head.CDR();
		}
	}
	
	/**
	 * executes an in-order traversal
	 * and calls the procedure for all leafs, including NIL leafs
	 */
	public static void forAllLeafsDo(SExpr expr, SProcedure proc) {
		if (!expr.isPair()) {
			proc._(expr);
		}
		else {
			forAllLeafsDo(expr.CAR(),proc);
			forAllLeafsDo(expr.CDR(),proc);
		}
	}
	
	public static SExpr assocValue(SExpr assocList, SExpr key) {
		SExpr head;
		while (!(head=assocList.CAR()).isNil()) {
			if (head.isPair() &&
				key.equals(head.CAR())
				)
				return head.CDR();
		}
		return SExpr.NIL;
	}

	public static void main(String[] args) {
		RRepresentation r= new RRepresentation();
		String[] tests= {
				"()"
				,"(A)"
				,"(p 1 2 3)"
				,"((1 1) (\"my\" 1 2 3) Yupee)"
		};
		class MyMatch implements SHandler {
		public final static String DISPATCH="(" +
				"(() . onNil)" +
				"((p . ?rest) . onP)" +
				"(((1 ?x) (\"my\" . ?y ) ?z ) . onXYZ)" 
				;
			public void onNil() {
				System.out.println("Yuppee NIL handler was called");
				System.out.println("~");
			}
			
			public void onXYZ(SExpr x, SExpr y, SExpr z) {
				System.out.println("onXYZ called:");
				System.out.println("X: " +x);
				System.out.println("Y: " +y);
				System.out.println("Z: " +z);
				System.out.println("~");
			}
			
			public void onP(SExpr rest) {
				System.out.println("Yuppeee, P handler is called");
				System.out.println("~");
			}
			public Object onDefault(SExpr arg0) {
				System.out.println("default method called");
				System.out.println(arg0);
				System.out.println("~");
				return null;
			}
		} MyMatch matcher= new MyMatch();

		try {
		for (int i=0; i< tests.length; i++ ){
			matchDispatch(r.readFrom(tests[i]),matcher);
			}
		}catch (Exception ex) {
			System.err.println(ex);
			ex.printStackTrace(System.err);
		}
	}

	
	public static boolean contains(SExpr list, SExpr element) {
		SExpr i=list ;
		while (! i.isNil()) {
			if (i.CAR().equals(element)) {
				return true;
			}
			i=i.CDR();
		}
		return false;
	}
	/**
	 * @return a pair of lists with uniqe elements 
	 * the first is list1 - list2, the second is list2 - list1
	 */
	public static SExpr disjunction(SExpr list1, SExpr list2) {
		//list1.
		return difference(list1,list2).cons(difference(list2,list1));
	}
	
	public static SExpr difference(SExpr list1, SExpr list2) {
		SExpr i= list1;
		SExpr result= SExpr.NIL;
		if (!i.isNil())
		do {
			SExpr elem=i.CAR();
			if (contains(list2,elem) || contains(result,elem)) continue;
			result = elem.cons(result);
		} while (! (i=i.CDR()).isNil());
		return result;
	}

	/**
	 * the list has to be non-empty
	 * @param list
	 * @param element
	 */
	public static void appendToList(SExpr list, SExpr element) {
		// TODO Auto-generated method stub
		SExpr i= list;
		while (!i.CDR().isNil()) {i=i.CDR() ;}
		i.setCDR(element.cons(SExpr.NIL));
	}

	public static void appendList(SExpr list, SExpr list2) {
		// TODO Auto-generated method stub
		SExpr i= list;
		while (!i.CDR().isNil()) {i=i.CDR() ;}
		i.setCDR(list2);
	}

	public static SExpr map(SExpr list, SFunction <SExpr> function) throws Exception {
		if (list.isNil()) return SExpr.NIL;
		return function._(list.CAR()).cons(map(list.CDR(),function));
	}
	
	
	/**
	 * TODO: derecursify it
	 */
	public static Object walk (SExpr tree, TreeVisitor visitor) {
		recursiveVisit(tree, visitor);
		return visitor.value();
		}

	private static void recursiveVisit(SExpr tree, TreeVisitor visitor) {
		if (tree.isPair () || tree.isNil() ) {
			visitor.down();
			for (SExpr i= tree; ! i.isNil(); i= i.CDR()) { recursiveVisit(i.CAR(),visitor);}
			visitor.up();
			}
		else { visitor.onLeaf(tree); }
	}

	public static <X> List<X> collectAsList(SExpr list, SFunction<X> transform) {
		List<X> result= new ArrayList<X>();
		for( SExpr element: list){
			result.add(transform._(element));
		}
		return result;
	}

}
