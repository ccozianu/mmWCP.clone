package sexpr.util;

import sexpr.SExpr;

/**
 * builds an S-Expression by receiving TreeVisitor commands
 * at first this is unoptimized and creates some garbage
 * @author ccozianu
 */
public class SBuilder implements TreeVisitor {
	TreeVisitor root= new InitBuilder();
	
	public static abstract class ValueCapsule implements TreeVisitor { //value capsule
		public void down() {throw new RuntimeException("invalid tree visiting sequence");}
		public void up() {throw new RuntimeException("invalid tree visiting sequence");}
		public void onLeaf(SExpr arg0) {throw new RuntimeException("invalid tree visiting sequence"); }
		public abstract Object value();
	}
	
	public class InitBuilder  implements TreeVisitor {
		public void down() { root= new ListBuilder(null); }
		public void up() {throw new RuntimeException("invalid tree visiting sequence"); }
		public void onLeaf(final SExpr arg) { root= new ValueCapsule() { public Object value() { return arg; }}; }
		public Object value() { 
			return null;
			//throw new RuntimeException("invalid tree visiting sequence");
			}
	}
	
	public class ListBuilder implements TreeVisitor {
		ListBuilder parent;
		SExpr.ListConstructor lc= new SExpr.ListConstructor();
		ListBuilder(ListBuilder parent_) { this.parent= parent_;}

		public void down() { root= new ListBuilder(this); }

		public void onLeaf(SExpr arg) { lc.append(arg); }

		public void up() {   
			if ( parent!= null ) {	parent.lc.append(lc.make());
									root= parent;	}
			else { /* root level*/ root= new ValueCapsule() { public Object value() { return ListBuilder.this.value(); }};}
			}

		public Object value() { return lc.make(); }
		
	}
	
	public void down() { root.down(); }
	
	public void up() { root.up();}
	
	public void onLeaf(SExpr arg) { root.onLeaf(arg); }
	
	public SExpr value() {
		return (SExpr) root.value();
	}
}