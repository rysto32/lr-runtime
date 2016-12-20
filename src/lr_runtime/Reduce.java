/*
 * Reduce.java
 *
 * Created on January 23, 2008, 3:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/* Class representing the LR reduce action */
public class Reduce implements Action {
    //the production that we reduce
    public final int productionId;
    
    private static final long serialVersionUID = -8087129964202762651L;
    
    public Reduce(int prodId) {
        productionId = prodId;
    }

    public <R, T> R acceptVisitor(ActionVisitor<R, T> v, T t) {
        return v.visitReduce(this, t);
    }
    
    @Override
    public String toString() {
        return "Reduce " + productionId;
    }

    public String toString(List<?> prodList) {
        return toString() + " (" + prodList.get(productionId) + ")";
    }
}
