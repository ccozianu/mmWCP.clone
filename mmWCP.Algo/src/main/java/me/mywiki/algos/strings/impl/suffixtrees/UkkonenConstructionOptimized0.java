package me.mywiki.algos.strings.impl.suffixtrees;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.mywiki.algos.strings.SuffixTreeAlgorithms;
import me.mywiki.algos.strings.SuffixTreeAlgorithms.SuffixTree;

public class UkkonenConstructionOptimized0 {

    public static SuffixTree construct(String t){
        return new STreeImpl(t);
    }
    
    private static class STreeImpl implements SuffixTree {
        
        
        
        public class ForkNode extends ThisTreeNodeBase {
                        
            public ForkNode(int strFromStart_, int strToEnd_ ) {
                super(strFromStart_, strToEnd_);
            }
            
            private Map<Character, ThisTreeNodeBase> 
                map=  new HashMap<>();
    
            private ThisTreeNodeBase prev= null;

            @Override
            public SuffixTreeNode findTransition(char c) {
                ThisTreeNodeBase next= map.get(c);
                return next ;
            }
            
            @Override
            public RefPoint transitionToReference(char c) {
                ThisTreeNodeBase next= map.get(c);
                if (next== null) {
                    throw new IllegalStateException("transitionToReference called for character '" +c +"' not at this point: "+ this);
                }
                return new RefPoint(next);
            }
            
            @Override
            public Collection<Character> transitionChars() { return map.keySet(); }
            
            
            @Override
            public SplitResult testAndSplit( RefPoint ref, int theStrPos ) {
                assert( ref.base == this
                        && ref.offsetFromBase == 0);
                char c= theString.charAt(theStrPos);
                if (map.containsKey(c)) {
                    return new NoSplit( );
                }
                else {
                    // normally next character would be exactly this.strToEnd
                    // but we jump instead to theStrPos, therefore the start that leads to the segment
                    // is :
                    // LinearNode constructor will also attached to this.map
                    new LinearNode( this, c, this.strFromStart + (theStrPos - this.strToEnd));
                    return new ForkNewBranch( this , c );
                }
            }

            /**
             * In the construction only fork have predecessors other fork, 
             * except we also have InitialPseudoNode
             * @param pred_
             */
            public void setPredecessor(ThisTreeNodeBase pred_) {
                assert( ( pred_ instanceof ForkNode && 
                          pred_.substringTo().equals( this.precedingStr())) 
                        || (this == root && pred_ instanceof InitialPseudoNode) );
                this.prev= pred_;
            }
            
            public ThisTreeNodeBase getPredecessor() {
                return this.prev;
            }
            

            /**
             * The string preceding strToHere, i.e. the string that if we enumerate
             * from suffix tree root should lead us to our predecessor node
             */
            private CharSequence precedingStr() {
                return theString.subSequence(strFromStart+1, strToEnd);
            }
            
 
        }

        public class LinearNode extends ThisTreeNodeBase {
            
            // start position in enclosing instance's theString
            private int startPos;
            private int count;
            
            //this field will not be assigned until the final construction of the tree
            private BitSet terminalLetters= new BitSet();
            
            /**
             * Internal  objects for iterating over a linear node,
             * they start at index 1, since the first transition is over
             * the parent LinearNode object, and end after the last character of the LinearNode
             * 
             */
            class LinearNodeItr extends ThisTreeNodeBase {
                final int offSet;
                                
                public LinearNodeItr( int offSet_) { 
                    super( LinearNode.this.strFromStart, 
                           LinearNode.this.strToEnd + offSet_ );
                    assert (offSet_ <= count);
                    this.offSet= offSet_; 
                }
                
                @Override
                public SuffixTreeNode findTransition(char c) {
                    
                    return 
                            (offSet < count) 
                            ? ( (c==theString.charAt(startPos+offSet)) 
                                    ? (  (offSet < count - 1 ) 
                                         ? new LinearNodeItr(offSet+1)
                                         : next )
                                    : null)
                            : (next != null)
                              ? next.findTransition(c)
                              : null;
                }
                
                @Override
                public boolean isTerminal() { 
                    return (offSet == count) || terminalLetters.get(offSet); 
                }
                
                @Override
                public Collection<Character> transitionChars() {
                    return Collections.singletonList(theString.charAt( startPos + offSet ));
                }
                
                @Override
                public RefPoint transitionToReference(char c) {
                    throw new UnsupportedOperationException("not on this pseudo node");
                }
                
                @Override
                public SplitResult testAndSplit( RefPoint inOutReference,
                                             int nextPosition) 
                {
                    throw new RuntimeException("Not implemented yet");
                }
                
            }
            
            /**
             * A LinearNode starts as an open segment, but later can be split
             * in which case we need to have a pointer to the next node
             */
            public final ForkNode comingFrom;
            public ThisTreeNodeBase next= null;


            /**
             */
            public LinearNode( ForkNode parent, char toHere, int strNewStartToHere ) {
                //super( parent.strFromStart + (strNewStartToHere-parent.strToEnd-1), strNewStartToHere + parent.distanceFromRoot() );
                super( strNewStartToHere, parent.strToEnd + 1 + (strNewStartToHere - parent.strFromStart) );
                assert( parent.map.get(toHere) == null);
                this.startPos= parent.strToEnd + 1+ (strNewStartToHere - parent.strFromStart);
                parent.map.put(toHere,this);
                this.comingFrom = parent;
                this.count = Integer.MAX_VALUE;
            }

            @Override
            public SplitResult testAndSplit( RefPoint ref,
                                             int nextPosition ) 
            {
                assert(ref.base == this);
                
                char charAtRef= this.charAt(ref.offsetFromBase);
                char newTestChar = theString.charAt(nextPosition);
                
                if (newTestChar == charAtRef) {
                    // no need to split as we have the transition
                    return new NoSplit();
                }
                else {
                    assert ( ref.offsetFromBase >= 0 );
                    ForkNode fork= new ForkNode(this.strFromStart, this.strToEnd + ref.offsetFromBase);
                    
                    // create the remainder of the existing segment as a branch
                    { LinearNode existingAfterSplit= new LinearNode( fork, charAtRef, this.strFromStart );
                      existingAfterSplit.count= ( this.count  == Integer.MAX_VALUE ) 
                                                    ? Integer.MAX_VALUE
                                                    : this.count - ref.offsetFromBase - 1;
                      if (existingAfterSplit.count == 0) {
                        // we don't create an empty segment just link to the next node
                        assert(next != null) ; //all segments with finite length have a next
                        fork.map.put(charAtRef,next);
                     }
                      else {
                          existingAfterSplit.next= this.next;
                      }
                    }
                    // the new segment constructor attaches it to the fork
                    new LinearNode( fork, newTestChar, this.strFromStart + (nextPosition - startPos - ref.offsetFromBase));
                    
                    //truncate the current segment to the new fork poi
                    if (ref.offsetFromBase==0) {
                        //this has to disappear, replaced with the fork
                        this.comingFrom.map.put(theString.charAt(this.strToEnd-1), fork);
                    }
                    else {
                        this.count = ref.offsetFromBase;
                        this.next= fork;
                        // TODO: continue HERE;
                        //we should return both successor capsule, so they get assigned their predecessor
                    }
                    return new SegmentSplitResult( fork, charAtRef, newTestChar );
                }
            }
            
            @Override
            public Collection<Character> transitionChars() {
                return Collections.singletonList(theString.charAt(startPos));
            }

            @Override
            public SuffixTreeNode findTransition(char c) {
                char result= theString.charAt(startPos);
                return result == c 
                        ? new LinearNodeItr(1)
                        : null;
            }

            @Override
            public RefPoint transitionToReference(char c) {
                if (charAt(0) != c) {
                    throw new IllegalStateException("Cannot transition with char: "+ c + " "+ this);
                }
                return new RefPoint(this,1);
            }
            
             boolean extendsIndefinitely() {
                return next == null;
            }
            
            RefPoint closeToRight() {
                this.count= theString.length() - this.startPos;
                if (this.count > 0) { 
                    terminalLetters.set(count-1);
                }
                else {
                    this.markTerminal();
                }
                return new RefPoint(this, this.count);
            }


            /**
             * translate coordinate relative to this LinearNode into
             * the original string and return the charAt
             * @param idx
             * @return
             */
            public char charAt(int idx) {
                assert (idx < count );
                return theString.charAt(this.startPos + idx);
            }

            public void markTerminal(int offsetFromBase) {
                if (this.count == Integer.MAX_VALUE) {
                    this.count = theString.length()- this.startPos;
                }
                if (offsetFromBase == 0){
                    this.markTerminal();
                }
                else if (offsetFromBase == count) {
                     assert(next == null);
                     next = new Terminus(this.strFromStart);
                }
                else {
                    this.terminalLetters.set(offsetFromBase);
                }
            }
            
        }

        @Override
        public SuffixTreeNode root() {
            return root;
        }

        private STreeImpl(String input) {
            
            this.theString = input ;
            this.root= new ForkNode(0,0);
            
            InitialPseudoNode pseudoN= new InitialPseudoNode(root);
            root.setPredecessor( pseudoN);
            root.markTerminal();
            
            
            // points to the terminating node of the
            // we use an array of one element as this is the easiest way in Java 
            // to emulate an IN OUT parameter (blah**  in c++, var param in Pascal or IN OUT in Ada and PL/SQL
            // as we pass an active point ref in and we get the next active point ref out
            RefPoint ap =  new RefPoint( root ) ;
            SplitResult loopResult;
            for ( int i=0; 
                  i < theString.length();
                  // this is transitioned to the end of the loop
                  //ap = ap.transitionToReference(theString.charAt(i)), 
                  i++ ) 
            {
                debug("Round : "+i+ " char:"+theString.charAt(i));
                SegmentSplitResult prevSplit= null;
                do 
                {
                    debug(" Start active point: " +ap);
                    // create a new branching node for current active point
                    // because we're missing theString.charAt(i)
                    // the returned point is the bifurcation
                    loopResult = ap.testAndSplitFor(i);
                    // if we need to assign the backlink for previously created node
                    if (loopResult instanceof SegmentSplitResult ) {
                        SegmentSplitResult newSplit= (SegmentSplitResult) loopResult;
                        if (prevSplit != null){
                            prevSplit.node.setPredecessor(newSplit.node);
                            debug( "Added backlink: " + prevSplit.node + " -> " + newSplit.node);
                        }
                        prevSplit= newSplit;
                    }
                    else if (loopResult instanceof ForkNewBranch) {
                        if (prevSplit != null) {
                            prevSplit.node.setPredecessor(ap.base);
                            debug("Added backlink: " + prevSplit.node
                                    + " -> " + ap.base);
                        }
                        // next nodes on the backlink trail will also be a forks
                        prevSplit= null;
                    }
                    
                    if (loopResult.wasSplit) {
                        ap= ap.predecessor();
                    }
                } while (loopResult.wasSplit);
                debug("End point: " + ap);
                
                //position active point for the next round
                if (prevSplit != null) {
                    //prevSplit.node.predecessor= loopResult.node;
                    assert( ap.base instanceof ForkNode );
                    prevSplit.node.setPredecessor(ap.base);
                    debug("Added backlink: " + prevSplit.node
                            + " -> " + ap.base);
                }/**/
                ap = ap.transitionToReference(theString.charAt(i));
                
            }
            closeTheTreeConstruction(ap);
        }

        //TODO: wire this to a system property
        boolean debug=false;
        private void debug(String s) {
           if (debug)
               System.err.println(s);
        }

        /**
         * At the end of the construction loops,
         * Runs the final run through the frontier
         * and mark terminal nodes
         */
        private void closeTheTreeConstruction(RefPoint ap) {

            // find the top of the tree, by enumerating the input string
            // starting with the first character
            // until we reach an open ended branch
            if (theString.length()==0) {
                return;
            }
            //Navigate to find the topmost "leaf"
            //TODO: we can optimize this, updating the top during the tree construction
            ThisTreeNodeBase top = root;
            for (   int i=0; 
                    ! ( top instanceof LinearNode
                              && ((LinearNode)top).extendsIndefinitely()); 
                    top=  (ThisTreeNodeBase) top.findTransition( theString.charAt(i) ), i++  ) 
            ;

            // start from the top and iterate until we reach root (which is marked terminal from construction)
            RefPoint borderItr= ((LinearNode)top).closeToRight();
            for ( ; 
                  ! ( borderItr.base == root && borderItr.offsetFromBase ==0 ) ;
                  borderItr = borderItr.predecessor() ) 
            {
                borderItr.markTerminal();
            }
        }


        private final String theString;
        private final ForkNode root;
    
        

        private static RefPoint normalizeReference( RefPoint base_, int startIndex_, int count_) 
        {
           throw new RuntimeException("Not implemented");
        }
        
        
        
        /**
         * Reference to a point in the SuffixTree which can be a node
         * os in case of a word transition edge , somewhere inside the transition string
         * invariant ( this.base.realNode instanceOf LinearNode )
         *             &&
         *             ( this.offSetFromBase < ((LinearNode)this.base.realNode ).count)
         */
        private class RefPoint {
            
            /**
             * The active point starting from the base node
             * with the transition of the substring of the big string t
             * which starts at startIndex and goes count characters
             */
            RefPoint(ThisTreeNodeBase base_ ) {
                this.base= base_;
                this.offsetFromBase= 0;
            }
            
            public void markTerminal() {
                if (base instanceof LinearNode) {
                    LinearNode linNode= (LinearNode) base;
                    linNode.markTerminal(offsetFromBase);
                }
                else {
                    base.markTerminal();
                }
            }

            RefPoint( LinearNode base_, int offsetFromBase_) {
                this.base= base_;
                this.offsetFromBase= offsetFromBase_;
            }
       


            public SplitResult testAndSplitFor(int nextPos) {
                return this.base.testAndSplit(this, nextPos);
            }

            public RefPoint transitionToReference(char c) {
                if (base instanceof LinearNode) {
                    LinearNode linBase= (LinearNode) base;
                    assert(linBase.charAt(offsetFromBase) == c);
                    if (offsetFromBase == linBase.count - 1 ) {
                        return new RefPoint(linBase.next);
                    }
                    else {
                        return new RefPoint(linBase, offsetFromBase +1);
                    }
                }
                else {
                    return base.transitionToReference(c);
                }
            }


            public RefPoint predecessor() {
                if (base instanceof ForkNode) {
                    assert(offsetFromBase == 0);
                    assert(((ForkNode)base).getPredecessor()!= null);
                    //base.predecessor is not a CapsuleNode only for root
                    // but we should not reach here from base root with fork
                    return new RefPoint(((ForkNode)base).getPredecessor());
                }
                else { // Linear node
                    LinearNode linNode= (LinearNode) base;
                    // -1, and +1 are for the extra char representing the transition coming from the parent node
                    return (linNode.comingFrom.getPredecessor()).transitionToReference(linNode.startPos - 1, this.offsetFromBase + 1);
                }
            }

            final ThisTreeNodeBase base;
            final private int offsetFromBase;
            
            /**
             * to help in debugging
             */
            @Override
            public String toString() {
                if (base instanceof LinearNode) {
                    LinearNode linBase= (LinearNode) base;
                    return linBase.toString() 
                           + "->"
                           + theString.substring(linBase.startPos, linBase.startPos + offsetFromBase)
                           +".";
                }
                else {
                    return base.toString();
                }
            }
          
        }





        /**
         * a base class for the nodes of this tree
         */
        abstract class ThisTreeNodeBase implements SuffixTreeNode {
            
            // the path that reaches this node in "theString"
            // expressed as startPos and endPos (last one exclusive)
            // using the the usual [star, end )convention
            final int strFromStart;
            final int strToEnd;
            
            public ThisTreeNodeBase(int strFromStart_ , int strToEnd_) {
                this.strFromStart= strFromStart_;
                this.strToEnd= strToEnd_;
            }
             
             public CharSequence substringTo() {
                return theString.subSequence(strFromStart, strToEnd);
            }

            public RefPoint transitionToReference( int startPos,
                                                   int count ) 
            {
                //TODO: optimize this: iterating one by one can get to N**2 complexity
                RefPoint result= new RefPoint(this);
                for (int i=0; i< count; i++) {
                    result=  result.transitionToReference(theString.charAt( startPos + i));
                }
                return result;
            }

            int distanceFromRoot() {return strToEnd - strFromStart; }
            
            public abstract RefPoint transitionToReference(char c) ;
            
           
            /**
             * Test if the char at next position has transition in the current node
             * If it doesn't then create one, even by splitting a current virtual position
             * @return true if a new node is created and the reference was moved
             */
            public abstract SplitResult testAndSplit(RefPoint inOutReference, int nextPosition);
            
            private boolean terminal=false;
            @Override
            public boolean isTerminal() { return this.terminal; }
            
            public void markTerminal() { this.terminal= true; }
            
            @Override
            public String toString() {
                return "\""+theString.substring(strFromStart, strToEnd)+'"';
            }
        }
        

        
        
        /**
         * the sole purpose of this artificial pseudo node
         * is to artificially terminate the loops such as
         * while( (borderP= borderP.predecessor()).findTransition(c) == null )
         * as this artificial node is at the bottom of all backchains
         * and it "has" all the transitions
         */
        private class InitialPseudoNode extends ThisTreeNodeBase  implements SuffixTreeNode {
            
            public InitialPseudoNode(ForkNode root_) {
                super(-1,-1);
                this.root= root_;
            }
            
            public SuffixTreeNode findTransition(char c) {
                return root;
            }
            
            @Override
            public RefPoint transitionToReference(char c) {
                return new RefPoint(this.root);
            }
            
            public boolean isRoot() {
                throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
            
            public boolean isTerminal() {
                throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
            
            public Collection<Character> transitionChars() { 
                throw new IllegalStateException("Check you algorithm better, no reason to call on the pseudonode"); }
            
            
            @Override
            public SplitResult testAndSplit(RefPoint inOutReference, int nextPosition) {
                return new NoSplit(); 
            }
            
            private final ForkNode root;
            
            @Override
            public String toString() { return "PseudoRoot" ;}
        }

       
        private class Terminus extends ThisTreeNodeBase {

            public Terminus(int strFromStart_) {
                super(strFromStart_, theString.length());
            }
            @Override
            public Collection<Character> transitionChars() {
                throw new RuntimeException("Not implemented yet");
            }

            @Override
            public SuffixTreeNode findTransition(char c) {
                return null;
            }

            @Override
            public me.mywiki.algos.strings.impl.suffixtrees.UkkonenConstructionOptimized0.STreeImpl.RefPoint transitionToReference(
                    int startPos, int count) {
                throw new RuntimeException("Not implemented yet");
            }

            @Override
            public me.mywiki.algos.strings.impl.suffixtrees.UkkonenConstructionOptimized0.STreeImpl.RefPoint transitionToReference(
                    char c) {
                throw new RuntimeException("Not implemented yet");
            }

            @Override
            public SplitResult testAndSplit( RefPoint inOutReference,
                                             int nextPosition) {
                throw new RuntimeException("Not implemented yet");
            }

            @Override
            public boolean isTerminal() {
                return true;
            }

            @Override
            public void markTerminal() {
                throw new RuntimeException("Not implemented yet");
            }
            
        }
         
        /**
         * Simple struct to pass intermediate results in the inner loop
         */
        private abstract static class SplitResult {
            
            final boolean wasSplit;

            public SplitResult(boolean wasSplit_) {
                this.wasSplit= wasSplit_;
            }
        }
        
        private static class NoSplit extends SplitResult { 
            public NoSplit() {  super(false); }
        }
        
        private static class ForkNewBranch extends SplitResult {
            final char cNew;
            final ForkNode splitAt;
            public ForkNewBranch( ForkNode splitAt_, char cNew_) {
                super(true);
                this.cNew= cNew_;
                this.splitAt= splitAt_;
            }
        }
        
        private static class SegmentSplitResult extends SplitResult {
            final ForkNode node;

            final char cOld, cNew;
            SegmentSplitResult(ForkNode newlySplitAt_, char cOld_, char cNew_) {
                super(true );
                this.node = newlySplitAt_;
                this.cOld= cOld_;
                this.cNew= cNew_;
            }
        }
        
    }
    
    
    public static void main(String [] args) {
        try {
            String [] tests = { /**/"",
                                "abbbbb",
                                "b",
                                "ab",
                                "aa",
                                "abc",
                                "abcd",
                                "abcdef",
                                "aaaaaaa",
                                
                                "baa",
                                /**/
                                "aaab",
                                "abbb",
                                "aba",
                                "abab",
                                "ababab",
                                "ababc",
                                "abababx",/*
                                
                                "abcaaaaabcabc",
                                "mabcabcabcabc",
                                "mississipi",/**/
                                "mississiabsiabab"//b"//ab"//ababc"/**/
                           };
            for (String testString:tests) {
                SuffixTree result= construct(testString);
                System.out.println("Verifying: "+testString);
                SuffixTreeAlgorithms.verifySuffixTree(result, testString );
                System.out.println(result);
            }
        }
        catch(Exception ex) {
            System.err.println("Main caught exception: "+ex);
            ex.printStackTrace(System.err);
        }
        
    }
    
 
}
        


