/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.List;

/**
 * A repair that replaces a single token with another token
 */
public class SubstitutionRepair extends Repair {
    public final Token token;

    public SubstitutionRepair(int index, Token token) {
        super(index);
        this.token = token;
    }

    @Override
    public int applyRepair(List<Token> list, int indexAdjustment) {
        list.set(index + indexAdjustment, token);
        
        return 0;
    }

    @Override
    public String toString() {
        return "SUB " + token + " @ " + index;
    }
}
