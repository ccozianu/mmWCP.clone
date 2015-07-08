package me.mywiki.algos.strings;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;

import me.mywiki.algos.strings.SuffixTreeAlgorithms.SuffixTree;
import me.mywiki.algos.strings.SuffixTreeAlgorithms.SuffixTree.SuffixTreeNode;

public class SuffixTreeAlgorithms {
	
	public static interface SuffixTree {
		
		interface SuffixTreeNode {
			boolean isTerminal();
		    
			/**
			 * TODO: verify whether we really need this method
			 */
		    Collection<Character> transitionChars();
		    
		    /**
		     * returns null if there's no transition for character c
		     */
		    SuffixTreeNode findTransition(char c);
		}
		
		SuffixTreeNode root();
	}
	
	public static boolean containsAsSuffix(SuffixTree sTree, CharSequence s) {
		SuffixTreeNode itr= sTree.root();
		for (int i=0; i< s.length(); i++) {
			if ( ( itr= itr.findTransition(s.charAt(i))) == null)
			{
				return false;
			}
		}
		return itr.isTerminal();
	}

    public static void verifySuffixTree( SuffixTree stree, CharSequence s ) {
        final int N= s.length();
        for (int i=N-1; i>=0; i-- )
        {
            CharSequence suffix= s.subSequence(i, N);
            if (! containsAsSuffix(stree, suffix)) {
                throw new AssertionError("Suffix verification failed for: " + suffix +" suffix of: " + stree);
            }
        }
    }
}
