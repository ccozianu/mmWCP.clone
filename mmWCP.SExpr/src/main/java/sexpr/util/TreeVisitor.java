/**
 * 
 */
package sexpr.util;

import sexpr.SExpr;

public interface TreeVisitor {
	
	public  static final TreeVisitor NIL= new TreeVisitor() {
		int level=0; 
		boolean leafAtLevelZeroAllowed=true;
		boolean closed=false;
		
		public void down() {
			if (closed) throw new IllegalStateException();
			level++; 
			leafAtLevelZeroAllowed= false; 
			}
		
		public void onLeaf(SExpr arg0) {
			if (closed) throw new IllegalStateException();
			if (level == 0) {
				if (leafAtLevelZeroAllowed) { leafAtLevelZeroAllowed= false; closed=true; }
				else throw new IllegalStateException();
			}
			}
		
		public void up() {
			if (closed) throw new IllegalStateException();
			level--; 
			if (level < 0) throw new IllegalStateException();
			if (level==0) closed= true;
			}
		
		public Object value() {
			if(!closed) throw new IllegalStateException(); 
			return null;
			}
	};
	
	/**
	 * goes down the hierarchy
	 */
	public void down();
	
	/**
	 * goes up the tree 
	 */
	public void up();

	
	/**
	 * visit a leaf
	 */
	public void onLeaf(SExpr content);
	
	
	/**
	 * @return the value that was computed by visiting the tree at the end of the iteration
	 */
	public Object value();
}