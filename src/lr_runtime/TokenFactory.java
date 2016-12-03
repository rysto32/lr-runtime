/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

/**
 * Factory interface that creates tokens.
 */
public interface TokenFactory<E> {
    public Token<E> makeToken(int s, E v, int l, int c);
}
