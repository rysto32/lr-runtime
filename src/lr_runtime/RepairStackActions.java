/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;
import lr_runtime.Action.Performed;

/**
 * To check how good a repair is, we need to try the repair and see how far
 * it can go.  The RepairStackActions are used to implement a parser checks how
 * good a repair is.
 */
class RepairStackActions implements StackActions {
    private final Stack<Integer> stateStack = new Stack<Integer>();
    private final LrParser parent;
    private final Scanner lex;

    RepairStackActions(ParseStackActions initStack, LrParser parent, Scanner lex) {
        this.parent = parent;
        this.lex = lex;
        
        for(Token t : initStack.getParseStack()) {
            stateStack.push(t.state);
        }
        
        stateStack.push(initStack.getState());
    }

    public Performed visitAccept(Accept a, Token lookahead) {
        return Performed.ACCEPT;
    }

    public int getState() {
        return stateStack.peek();
    }

    public Performed visitReduce(Reduce r, Token t) {
        int numPops = parent.getProductionLength(r.productionId);
        for(int i = 0; i < numPops; i++) {
            stateStack.pop();
        }

        int lhsSymbol = parent.getLhsSymbol(r.productionId);
        stateStack.push(parent.table.getGoto(getState(), lhsSymbol));

        return Performed.REDUCE;
    }

    public Performed visitReject(Reject r, Token lookahead) {
        return Performed.ERROR;
    }

    public void scannerError() {
        
    }

    public Performed visitShift(Shift s, Token lookahead) {
        stateStack.push(s.dest);

        return Performed.SHIFT;
    }

    
}
