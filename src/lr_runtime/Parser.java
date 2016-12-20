/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

/**
 *  Class that actually implements the LR parsing algorithm.  The actions variable
 * holds all of the parsing state and does most of the work.
 */
class Parser<T extends StackActions<T>> {
    final T actions;
    private final LrParseTable table;

    public Parser(T actions, LrParseTable table) {
        this.actions = actions;
        this.table = table;
    }
    
    private Parser(Parser<T> p) {
        this.actions = p.actions.branch();
        this.table = p.table;
    }
    
    Parser<T> branch() {
        return new Parser(this);
    }
    
    T getActions() {
        return actions;
    }
    
    /* Do exactly one step of parsing(see Action for definition of 'step')
     * 
     */
    public Action.Performed nextToken(Token lookahead) {
        Action.Performed p;
        
        do {
            /* Find the action (shift, reduce, etc) and perform it */
            Action action = table.getAction(actions.getState(), lookahead.sym);
            p = action.acceptVisitor(actions, lookahead);
        } while(p.continueParse());
        
        return p;
    }
}
