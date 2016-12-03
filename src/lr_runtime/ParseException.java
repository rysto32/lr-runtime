/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/**
 * Exception that can be thrown when a parse error is found
 */
public class ParseException extends Exception {
    public final Token lookahead;
    public final List<String> possibleLookaheads;

    public ParseException(Token t, List<String> possible) {
        super("Error @ line " + t.line + " column " + t.column + ": " +
                "caused by token " + t + "(" + t.sym + ") possible=" + possible);
        
        lookahead = t;
        possibleLookaheads = possible;
    }
}
