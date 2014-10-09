package sexpr.util;

/**
 * used in recursive algorithms as a form of setjmp/longjmp
 * or continuation cut if you will
 * @author Costin Cozianu
 */
public class Shortcut extends Exception {
	public static void throwIt() throws Shortcut{ throw new Shortcut();} 

}
