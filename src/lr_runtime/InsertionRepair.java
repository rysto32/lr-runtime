/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.List;

/**
 * Repair parse by inserting a single token
 */
public class InsertionRepair extends Repair {
    //the token that we are going to insert.
    public final Token token;

    public InsertionRepair(int index, Token token) {
        super(index);
        this.token = token;
    }

    @Override
    public int applyRepair(List<Token> list, int indexAdjustment) {
        list.add(index + indexAdjustment, token);
        
        return 1;
    }

    @Override
    public String toString() {
        return "INS " + token + " @ " + index;
    }

}
