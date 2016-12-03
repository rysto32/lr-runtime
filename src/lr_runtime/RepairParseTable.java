/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.Set;

/**
 * Parse tables that can handle any repair we can make -- including inserting
 * non-terminal symbols
 */
public class RepairParseTable implements LrParseTable {
    private final ParseTable master;

    public RepairParseTable(ParseTable master) {
        this.master = master;
    }

    public Set<Integer> getPossible(int state) {
        return master.getPossible(state);
    }

    public Integer getGoto(int state, int sym) {
        return master.getGoto(state, sym);
    }

    public Action getAction(int state, int sym) {
        Action a = master.getAction(state, sym);
        
        // ok, this is pretty gross.  We're doing this so that we can handle
        // actions on non-terminals as well as terminals, because a repair
        // can insert non-terminals symbols that we need to perorm "actions"
        // on.  
        if(a instanceof Reject) {
            Integer goTo = getGoto(state, sym);
            
            if(goTo != null) {
                return new Shift(goTo);
            }
        }
        
        return a;
    }
    
    
}
