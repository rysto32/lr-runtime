/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

/**
 * Interface describing the actions we perform on a parser's stack.
 */
public interface StackActions<T extends StackActions<T>> extends ActionVisitor<Action.Performed, Token> {
    public void scannerError();
    public int getState();
    
    T branch();
}
