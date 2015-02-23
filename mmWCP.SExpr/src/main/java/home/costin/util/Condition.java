package home.costin.util;

/**
 * This interface represents an assertion on a condition
 * @author Costin Cozianu <a href=mailto://ccozianu@yahoo.com>ccozianu@yahoo.com</a>
 */
public interface Condition 
{

/**
* @returns true - if the condition has been met
* @returns false - otherwise
*/

public boolean evaluate() ;
}
