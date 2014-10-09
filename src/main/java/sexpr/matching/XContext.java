package sexpr.matching;

import java.util.HashMap;

import sexpr.SExpr;
import sexpr.matching.XPatterns.XEnvironment;
import sexpr.util.SBuilder;
import sexpr.util.TreeVisitor;

/**
 * environment and a few other utilities
 * @author ccozianu
 */
class XContext {
	
	public class SavePoint extends Object {
		
		final SavePoint parent;
		private SavePoint(SavePoint parent_) {
			this.parent= parent_;
		}
		private SavePoint() { this.parent= null; }
	}
	
	private SavePoint savePoint= new SavePoint(); 

	Object leftValue;
	
	HashMap patterns = new HashMap();
	private XEnvironment env= new XEnvironment();
	XContext parent;
	boolean inheritVars;
	
	public SExpr result=null;

	public XContext() {		this(null,false); }
	public XContext(XContext parent_) {this(parent_,false);}
	
	public XContext(XContext parent_, boolean inheritVarsImplicitly ) { 
		this.parent = parent_; 
		this.inheritVars= inheritVarsImplicitly;
		}

	
	//TreeVisitor out() { return savePoint;}

	public XMatcher lookupPattern(String keyWord) {
		XMatcher result= (XMatcher)patterns.get(keyWord);
		if (result == null && parent != null) result= parent.lookupPattern(keyWord);
		return result;		}

	public SExpr substVarWithValue(SExpr arg) {
		String name= XPatterns.varNameOf(arg);
		if (name != null) 	{ return valueOf(name);}
		else 				{ return arg; }
	}

	public XContext.SavePoint savePoint() {
		return savePoint= new SavePoint(savePoint);
	}

	/**
	 * commits the current savepoint
	 */
	public void commit() {
		//savePoint.up();
		//SExpr value= savePoint.value();
		savePoint= savePoint.parent;
		/*if ((savePoint != null) && (value != null) ) {
			savePoint.onLeaf(value);
			}
		else {
			if (result==null)
				result= value;
			else
				throw new IllegalStateException("Commit called when the value was already assigned");
		}*/
			
	}

	public void cutTo(SavePoint saved) {
		while (savePoint != null) {
			if (savePoint == saved) break;
			savePoint= savePoint.parent;
		}
		if (savePoint != saved) throw new IllegalArgumentException("SavePoint was not active on stack");
	}

	/**
	 * if the variable represented by name is not bound, bind it with the data
	 * otherwise verify that its value is equal to data
	 * @throws MatchFailure if verification fails
	 * @param name
	 * @param data
	 */
	public void addOrVerifyIfPresent(String name, SExpr data) {
		SExpr oldValue= valueOf(name);
		if (oldValue== null) { set(name,data); }
		else {
			if (!oldValue.equals(data)) throw new XPatterns.MatchFailure();
		}
	}

	private void set(String name, SExpr data) {env.put(name,data); }
	
	public SExpr valueOf(String varName) {
		SExpr result= env.get(varName);
		if (result==null && inheritVars && parent != null ) result= parent.valueOf(varName);
		return result; }
	
	
	SBuilder innerBuilder= new SBuilder(); {innerBuilder.down();}
	SExpr inner=null;
	/**
	 * adds the resulting value of an inner pattern matching
	 * @param expr
	 */
	public void addInnerValue(SExpr expr) {
		innerBuilder.onLeaf(expr);
	}
	
	public SExpr innerOut() {
		if (inner== null){
			innerBuilder.up();
			inner= innerBuilder.value();
		}
		return inner;
	}
		

	
}