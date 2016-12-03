/*
 * Accept.java
 *
 * Created on January 23, 2008, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

public class Accept implements Action {
    
    public static final Accept INSTANCE = new Accept();
    
    public static final long serialVersionUID = 3646721542913668581L;
    
    private Accept() {
    }
    
    @Override
    public String toString() {
        return "Accept";
    }

    @Override
    public <R, T> R acceptVisitor(ActionVisitor<R, T> parser, T t){
        return parser.visitAccept(this, t);
    }

    public String toString(List<?> prodList) {
        return toString();
    }
}
