/*
 * Shift.java
 *
 * Created on January 23, 2008, 3:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/* An LR Shift action */
public class Shift implements Action {
    
    public final Integer dest;
    
    private static final long serialVersionUID = 7505395756405373268L;

    public Shift(Integer state) {
        if(state == null) {
            throw new Error();
        }
        dest = state;
    }
    
    @Override
    public String toString() {
        return "Shift " + dest;
    }

    public String toString(List<?> prodList) {
        return toString();
    }

    public <R, T> R acceptVisitor(ActionVisitor<R, T> v, T t) {
        return v.visitShift(this, t);
    }
}
