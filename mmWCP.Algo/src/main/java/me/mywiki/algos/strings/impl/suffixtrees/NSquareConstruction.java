package me.mywiki.algos.strings.impl.suffixtrees;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



import me.mywiki.algos.strings.SuffixTreeAlgorithms;
import static me.mywiki.algos.strings.SuffixTreeAlgorithms.*;

/**
 * This implementation is sub-optimal (n^2 in time and space wasted)
 * but understanding it is essential in understanding Ukonnen's efficient
 * algorithm
 */
public class NSquareConstruction {
	
	public static SuffixTree construct(String t){
		return new STreeImpl(t);
	}
	
	private static class STreeImpl implements SuffixTree {

		@Override
		public SuffixTreeNode root() {
			return root;
		}

		private STreeImpl(String t_) {
		    
			this.t = t_;
			this.root= new RootNode();
			InitialPseudoNode pseudoN= new InitialPseudoNode(root);
			root.setPredecessor(pseudoN);
			
			// construct the rest of the nodes
			
			// points to the terminating node of the  
			RegularNode top = root;
			
			for (char c: t.toCharArray()) {
			    
			    //borderP starts from the longest suffix , running back on the border to the root 
			    // or even the pseudoNode
			    ThisTreeNodeBase borderP = top;
			    RegularNode prevNewNode = null;
			    SuffixTreeNode nodeWithCTransition;
			    
			    while( (nodeWithCTransition = borderP.findTransition(c) ) == null) {
			        RegularNode newNode= new RegularNode();
			        ((RegularNode)borderP).addTransition(c, newNode);
			        if (prevNewNode != null) {
			            prevNewNode.setPredecessor(newNode);
			        }
			        else { // first iteration we can assign the next top
			            top= newNode;
			        }
			        // update for next iteration
			        prevNewNode = newNode;
			        borderP = borderP.getPredecessor();
			    }
			    //prevNewNode is not null because while has at least 1 iteration
			    prevNewNode.setPredecessor( (RegularNode)nodeWithCTransition );
			}
			
			// after we finish all the iteration we can assign terminal nodes
			// as the border nodes backtracking from the top
			for (RegularNode n= top; n != root; n = (RegularNode)  n.predecessor ) {
			    n.setTerminal(true);
			}
		}
		
		
		
		private final String t;
		private final RootNode root;

		/**
		 * a base class for internal nodes of this tree
		 * it mixes in the state and contract that once we set the predecessor of a suffixNode
		 * it should never be reset,
		 * and we should ask the predecessor of a node, only after we will have
		 * set it
		 */
		private static abstract class ThisTreeNodeBase implements SuffixTreeNode {
			ThisTreeNodeBase predecessor= null;
			
			public ThisTreeNodeBase getPredecessor() { 
				if (predecessor == null) {
					throw new IllegalStateException("Never ask of a predecessor of a node you haven't set yet");
				}
				return predecessor;
			}
			
            public void setPredecessor(ThisTreeNodeBase pred) {
				if (predecessor != null) { throw new IllegalStateException("Changing predecessor is probably wrong"); }
				this.predecessor= pred;
			}
		}

		private static class RootNode extends RegularNode 
		{
		    { super.terminal = true; }
		}
		
		/**
		 * the sole purpose of this artificial pseudo node
		 * is to artificially terminate the loops such as
		 * while( (borderP= borderP.predecessor()).findTransition(c) == null )
		 * as this artificial node is at the bottom of all backchains
		 * and it "has" all the transitions
		 */
		class InitialPseudoNode extends ThisTreeNodeBase  implements SuffixTreeNode {
			
			public InitialPseudoNode(RegularNode root_) {
				this.root= root_;
			}
			
			public SuffixTreeNode findTransition(char c) {
				return root;
			}
			
			public boolean isRoot() {
				throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
			
			public boolean isTerminal() {
				throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
			
			public Collection<Character> transitionChars() { 
				throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
			
			@Override
			public ThisTreeNodeBase getPredecessor() { 
			    throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
			
			@Override
			public void setPredecessor(ThisTreeNodeBase pred) {
			    throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
			
			
			private final RegularNode root;

		}
		
		private static class RegularNode
		                extends ThisTreeNodeBase 
		                implements SuffixTreeNode {
			
			/**
			 * creates an empty terminal node
			 * in the suffix tree construction algorithms, nodes start as empty, terminal nodes
			 */
			public RegularNode() { }
			public void addTransition(char c, RegularNode newNode) {
                this.transitions.putIfAbsent(c, newNode);
            }
			
            @Override
			public SuffixTreeNode findTransition(char c) {  return transitions.get(c); }
			
			@Override
			public boolean isTerminal() { return terminal; }
			
			void setTerminal(boolean b) { this.terminal = b; }
			
			@Override
			public Collection<Character> transitionChars() {
				return transitions.keySet(); }
			
			

			private boolean terminal= true;
			
			private Map<Character,RegularNode> transitions= new HashMap<>();
			
		}
		
		
		
		public static void main(String [] args) {
		    try {
		        String testString= "mississipississipi";
		        SuffixTree result= construct(testString);
		        
		        SuffixTreeAlgorithms.verifySuffixTree(result, testString );
		        System.out.println(result);
		    }
		    catch(Exception ex) {
		        System.err.println("Main caught exception: "+ex);
		        ex.printStackTrace(System.err);
		    }
		    
		}
		
	}
	
}
