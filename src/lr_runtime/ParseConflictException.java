/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

/**
 * Exception thrown on a Shift/Reduce or Reduce/Reduce conflict 
 */
public class ParseConflictException extends RuntimeException {
    public final int state;
    public final int lookahead;
    public final Action old;
    public final Action action;
    
    public ParseConflictException(int state, int lookahead, Action old, Action action) {
        super("Conflict in state " + state + " on lookahead " + lookahead +
                    "\nOld Action: " + old +"\tNew Action " + action);
        
        this.state = state;
        this.lookahead = lookahead;
        this.old = old;
        this.action = action;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ParseConflictException)) {
            return false;
        }
        
        ParseConflictException other = (ParseConflictException)obj;
        
        return state == other.state && 
                (old.equals(other.old) && action.equals(other.action) ||
                old.equals(other.action) && action.equals(other.old));
    }

    @Override
    public int hashCode() {
        return state + old.hashCode() + action.hashCode();
    }
    
    
}
