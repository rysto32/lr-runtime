/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.io.IOException;

/**
 *  Class that actually implements the LR parsing algorithm.  The actions variable
 * holds all of the parsing state and does most of the work.
 */
class Parser<T extends StackActions> {
    final T actions;
    private final Scanner lex;
    private final LrParseTable table;

    public Parser(T actions, Scanner lex, LrParseTable table) {
        this.actions = actions;
        this.lex = lex;
        this.table = table;
    }
    
    /* Do exactly one step of parsing(see Action for definition of 'step')
     * 
     */
    public Action.Performed nextToken() {
        Action.Performed p;
        Token lookahead;
        
        while(true) {
            /* Get the lookahead character(need to handle scanner errors) */
            try {
                lookahead = lex.nextSymbol();
            } catch(ScannerException e) {
                actions.scannerError();
                continue;
            } catch(IOException e) {
                throw new Error(e);
            }
            
            break;
        }
        
        do {
            /* Find the action (shift, reduce, etc) and perform it */
            Action action = table.getAction(actions.getState(), lookahead.sym);
            p = action.acceptVisitor(actions, lookahead);
        } while(p.continueParse());
        
        return p;
    }
}
