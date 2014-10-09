package sexpr.util;

import sexpr.SExpr;

/**
 * pointer to the current node of a traversal path
 * @author Costin Cozianu
 */

public class SPointer {
	final SExpr current;
	final SPointer parent;
	public SPointer (SExpr current_, SPointer parent_) {
		this.current= current_;
		this.parent= parent_;
	}
}
