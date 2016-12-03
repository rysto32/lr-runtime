/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.List;

/**
 * Repair parse by deleting a single token.
 */
public class DeletionRepair extends Repair {

    public DeletionRepair(int index) {
        super(index);
    }

    @Override
    public int applyRepair(List<Token> list, int indexAdjustment) {
        list.remove(index + indexAdjustment);
        
        return -1;
    }

    @Override
    public String toString() {
        return "DEL @ " + index;
    }
}
