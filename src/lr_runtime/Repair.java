/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/**
 * Abstract class representing a way of repairing a syntax error 
 */
public abstract class Repair implements Comparable<Repair> {
    public final int index;

    public Repair(int index) {
        this.index = index;
    }    
    
    public int compareTo(Repair o) {
        return index - o.index;
    }
    
    /* apply this repair to the list of queued tokens 
     * Returns how the length of the list has changed(eg -1 if the list is 1 element shorter)
     */
    public abstract int applyRepair(List<Token> list, int indexAdjustment);
    
    /* Apply all of the repairs in the set to the list.  If indexes is non-null,
     * this function will put the indexes of all repair tokens in indexes
     * 
     */
    public static void applyRepairs(List<Token> list, SortedSet<Repair> repairs, Set<Integer> indexes) {
        //Repair.index measures from the start of the list before any repairs are made
        // adjust tracks how the length of the list has changed, so that each repair
        // gets applied to the correct position in the list
        int adjust = 0;
        int count = 0;
        try {
            for(Repair r : repairs) {
                if(indexes != null) {
                    indexes.add(r.index + adjust);
                }

                adjust += r.applyRepair(list, adjust);
                count++;
            }
        } catch(RuntimeException t) {
            System.err.println("Count: " + count + " adjust: " + adjust);
            throw t;
        }
    }
}
