/*
 * Token.java
 *
 * Created on January 23, 2008, 7:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/* Abstract class representing a token.  This is abstract so I don't try to
 * instantiate one of these; if I try to print a Token to the user they won't
 * have a clue what the token is.
 * 
 */
public abstract class Token<E> {
    
    protected int sym;
    protected int line;
    protected int column;
    protected int parser;
    public E value;
    
    
    int state;
    
    public Token(int s, E v, int l, int c) {
        sym = s;
        value = v;
        line = l;
        column = c;
        parser = LrParser.DEFAULT_PARSER;
    }

    public int getColumn() {
        return column;
    }

    public int getLine() {
        return line;
    }

    public int getSym() {
        return sym;
    }
    
    public String toString() {
        return "Token(" + sym + ")";
    }

    public void setState(int state) {
        this.state = state;
    }
}
