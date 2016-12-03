/*
 * ParseTable.java
 *
 * Created on January 23, 2008, 4:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.io.Serializable;
import java.util.*;

/*
 * LR parse table with one token of lookahead.
 */
public class ParseTable implements Serializable, LrParseTable {
    private final List<Map<Integer, Action>> actionTable;
    private final Map<Integer, Map<Integer, Integer>> gotoTable;
    
    private static final long serialVersionUID = 2428786980103563822L;
    
    public ParseTable(List<?> items) {
        actionTable = new ArrayList<Map<Integer, Action>>(items.size());
        gotoTable = new HashMap<Integer, Map<Integer, Integer>>();
        
        for(int i = 0; i < items.size(); i++) {
            actionTable.add(new HashMap<Integer, Action>());
        }
    }
    
    /* Add a new action to the parse table.  Might result in a conflict */
    public void addAction(int state, Integer lookahead, Action action) {
        Action old = actionTable.get(state).put(lookahead, action);
        
        if(old != null && old != action) {
            throw new ParseConflictException(state, lookahead, old, action);
        }
    }
    
    /* Add a new goto transition to the parse table */
    public void addGoto(int start, Integer symbol, int end) {
        Map<Integer, Integer> map = gotoTable.get(start);
        
        if(map == null) {
            map = new HashMap<Integer, Integer>();
            gotoTable.put(start, map);
        }
        
        map.put(symbol, end);
    }
    
    /* Get the action associated with this state and lookahead symbol */
    public Action getAction(int state, int sym) {
        Action a = actionTable.get(state).get(sym);
        
        if(a == null) {
            return Reject.INSTANCE;
        }
        
        return a;
    }
    
    /* Get the set of all terminals that we would accept in the given state*/
    public Set<Integer> getPossible(int state) {
        return Collections.unmodifiableSet(actionTable.get(state).keySet());
    }
    
    /* Get the goto transition associated with the state/non-terminal pair */
    public Integer getGoto(int state, int sym) {
        Map<Integer, Integer> map = gotoTable.get(state);
        
        if(map == null) {
            return null;
        }
        
        return map.get(sym);
    }
    
    public Set<Neighbour> getNeighbours(int state) {
        Set<Neighbour> set = new HashSet<Neighbour>();
        
        for(Map.Entry<Integer, Action> entry : actionTable.get(state).entrySet()) {
            if(entry.getValue() instanceof Shift) {
                set.add(new Neighbour(((Shift)entry.getValue()).dest, entry.getKey()));
            }
        }
        
        Map<Integer, Integer> map = gotoTable.get(state);
        if(map != null) {
            for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
                set.add(new Neighbour(entry.getValue(), entry.getKey()));
            }
        }
        
        return set;
    }

    public List<Map<Integer, Action>> getActionTable() {
        return Collections.unmodifiableList(actionTable);
    }

    public Map<Integer, Map<Integer, Integer>> getGotoTable() {
        return Collections.unmodifiableMap(gotoTable);
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();

        for(int i = 0; i < actionTable.size(); i++) {
            b.append(i);
            b.append(" (");
            //b.append(items.get(i));
            b.append(") action\n");
            b.append(actionTable.get(i));
            b.append("\n\n");

            if(gotoTable.containsKey(i)) {
                b.append(i);
                b.append(" goto\n");
                b.append(gotoTable.get(i));
                b.append("\n\n");
            }
        }

        return b.toString();
    }

    public String toString(Map<Integer, String> symbolNum) {
        StringBuilder b = new StringBuilder();

        for(int state = 0; state < actionTable.size(); state++) {
            b.append(state);
            b.append(" (");
            //b.append(items.get(i));
            b.append(") action\n{");
            for(Map.Entry<Integer, Action> entry : actionTable.get(state).entrySet()) {
                b.append(symbolNum.get(entry.getKey()));
                b.append('=');
                b.append(entry.getValue().toString());
                b.append(",");
            }
            b.append("}\n\n");

            if(gotoTable.containsKey(state)) {
                b.append(state);
                b.append(" goto\n");
                //b.append(gotoTable.get(i));
                for(Map.Entry<Integer, Integer> entry : gotoTable.get(state).entrySet()) {
                    b.append(symbolNum.get(entry.getKey()));
                    b.append('=');
                    b.append(entry.getValue().toString());
                    b.append(",");
                }
                b.append("\n\n");
            }
        }

        return b.toString();
    }
    
    public class Neighbour {
        public int state;
        public int symbol;

        public Neighbour(int state, int symbol) {
            this.state = state;
            this.symbol = symbol;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ParseTable.Neighbour other = (ParseTable.Neighbour) obj;
            if (this.state != other.state) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 89 * hash + this.state;
            return hash;
        }
    }
}
