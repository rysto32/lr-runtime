/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.List;

/**
 * Class representing the LR reject action.
 */
public class Reject implements Action {
    /* Use a singleton because there are no instance variables */
    public static final Reject INSTANCE = new Reject();
    
    private Reject() {
        
    }

    public String toString(List<?> prodList) {
        return "Reject";
    }

    public <R, T> R acceptVisitor(ActionVisitor<R, T> v, T t) {
        return v.visitReject(this, t);
    }
    
    
}
