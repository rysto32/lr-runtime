/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/**
 * Generic interface of an LR parse table
 */
public interface LrParseTable {
    public Action getAction(int state, int sym);    
    public Set<Integer> getPossible(int state);    
    public Integer getGoto(int state, int sym);
}
