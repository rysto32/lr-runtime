/*
 * Action.java
 *
 * Created on January 23, 2008, 3:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.io.Serializable;
import java.util.*;

/* An interface representing the different types of actions an LR Parser
 * can do(shift, reduce, accept, error).
 */
public interface Action extends Serializable {    
    public String toString(List<?> prodList);

    public <R, T> R acceptVisitor(ActionVisitor<R, T> v, T t);
    
    // An enumeration to represent what action was undertaken
    public enum Performed { 
        // A single production was reduced
        REDUCE {
            @Override
            public boolean continueParse() {
                return true;
            }
            
            @Override
            public boolean finalState() {
                return false;
            }
        },
        
        // We hit an error but it was repaired
        REPAIRED {
            @Override
            public boolean continueParse() {
                return false;
            }
            
            @Override
            public boolean finalState() {
                return false;
            }
        },
        
        // We hit an error and no repair was found: bail
        ERROR {
            @Override
            public boolean continueParse() {
                return false;
            }
            
            @Override
            public boolean finalState() {
                return true;
            }
            
        },
        
        // String is accepted
        ACCEPT {
            @Override
            public boolean continueParse() {
                return false;
            }
            
            @Override
            public boolean finalState() {
                return true;
            }
            
        },
        
        // A single token was shifted
        SHIFT {
            @Override
            public boolean continueParse() {
                return false;
            }
            
            @Override
            public boolean finalState() {
                return false;
            }
            
        };
        
        // Returns true if the parser needs to continue to complete one more "step"
        // One "step" is either a shift action, an accept action or an unrepairable error
        public abstract boolean continueParse();
        
        // Returns true if the parser is done(either accepted or rejected with 
        //  no further repair possible)
        public abstract boolean finalState();
    };
}
