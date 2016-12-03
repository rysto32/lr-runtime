/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;
import lr_runtime.Action.Performed;

/**
 * The actions that we perform on the state stack.  This class implements
 * error recovery.
 */
class StateStackActions implements StackActions {
    /* Our stack of states.  The top of the stack is our current state */
    private final Stack<Integer> stateStack = new Stack<Integer>();
    
    /* The LrParser we belong to */
    private final LrParser parent;
    
    /* A factory for constructing Tokens */
    private final TokenFactory factory;
    
    /* The tokens waiting for delivery to the parse stack */
    private final LinkedList<Token> defered;
    
    /* The parse stack parser */
    private final Parser<ParseStackActions> parser;
    
    /* The scanner that gives us tokens */
    private final CheckpointScanner lex;

    StateStackActions(LrParser parent, LinkedList<Token> defered, 
            Parser<ParseStackActions> parser, CheckpointScanner lex, TokenFactory f) 
    {
        this.parent = parent;
        this.defered = defered;
        this.parser = parser;
        this.lex = lex;
        factory = f;
        
        stateStack.push(parent.getStartState());
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

    public Performed visitShift(Shift s, Token lookahead) {
        defered.add(lookahead);

        while(defered.size() > LrParser.DEFERED_TOKENS_LENGTH) {
            parser.nextToken(defered.poll());
        }

        stateStack.push(s.dest);

        return Performed.SHIFT;
    }

    public int getState() {
        return stateStack.peek();
    }

    /* Called to try to repair an error caused by lookahead */
    public Performed visitReject(Reject r, Token lookahead) {
        // report the error to the user
        parent.reportError(lookahead, getState(), 
                parent.convertPossibleNext(parent.table.getPossible(getState())));
        parser.actions.setError();
        
        // checkpoint the lexer so we can keep returning to this point in the
        //   parse while we look for a repair
        lex.checkpoint();
        
        // find the best repair
        SortedSet<Repair> best = findRepair(lookahead);
        
        if(best == null) {
            // No repair found -- bail out
            parent.reportGiveUp(lookahead);
            return Performed.ERROR;
        } else {
            //apply repair to our queue of defered tokens
            defered.add(lookahead);
            TreeSet<Integer> changes = new TreeSet<Integer>();
            Repair.applyRepairs(defered, best, changes);
            
            // Report the repair to the user
            parent.suggestRepair(defered, changes);
            
            // Make the parse stack parser completely catch up.
            while(!defered.isEmpty()) {
                Performed result = parser.nextToken(defered.poll());
                
                if(result.finalState() && !defered.isEmpty()) {
                    throw new Error();
                }
            }
            
            // copy the state stack from the parse stack
            copyParserStack();
            
            // we're done, so clear the checkpoint
            lex.clearCheckpoint();
            return Performed.REPAIRED;
        }
    }
    
    /* Make our state stack identical to the parse stack */
    private void copyParserStack() {
        stateStack.clear();
        
        for(Token t : parser.actions.getParseStack()) {
            stateStack.push(t.state);
        }
        
        stateStack.push(parser.actions.getState());
    }
    
    /* Find the set of repairs that allows us to continue furthest */
    private SortedSet<Repair> findRepair(Token lookahead) {
        // The best repair we've found so far
        SortedSet<Repair> best = null;
        // The number of tokens we were able to parse with best
        int bestDistance = -1;
        
        //try incrementally more simulaneous repairs
        for(int maxChanges = 1; maxChanges <= LrParser.MAX_REPAIR_CHANGES; maxChanges++) {
            // Find all possible repairs(this really, really sucks)
            Iterable<SortedSet<Repair>> possibleRepairs = findRepairs(maxChanges, defered.size() + 1);
            
            //System.err.println("Found " + possibleRepairs.size() + " repairs with " + maxChanges + " changes");
            
            // Try every repair and see if any are good
            for(SortedSet<Repair> repair : possibleRepairs) {
                
                // build the list of tokens that we'll act on
                LinkedList<Token> tokens = new LinkedList<Token>(defered);
                tokens.add(lookahead);
                
                // we must parse at least this far for this to be a good parse
                //  this far == past the error token
                int minDistance = tokens.size();
                
                // reset to our checkpoint
                lex.reset();
                
                //add a few extra tokens so we know how far we can get
                addForwardTokens(tokens);
                
                QueueScanner scanner = new QueueScanner(tokens);

                // apply the repairs to the list
                Repair.applyRepairs(tokens, repair, null);
                int queueLen = tokens.size();
                
                // create the temporary parser
                RepairStackActions actions = new RepairStackActions(parser.actions, parent, scanner);
                Parser<RepairStackActions> p = new Parser<RepairStackActions>(actions,
                        new RepairParseTable(parent.table));
                
                //System.err.println("Try repair " + repair);
                
                // distance is the number of tokens we parse
                int distance;
                for(distance = 0; distance < queueLen; distance++) {
                    Performed result = p.nextToken(scanner.nextSymbol());
                    
                    if(result.finalState()) {
                        break;
                    }
                }
                
                //if we hit the minimum distance and this is the best distance
                // we've achieved so far, record this repair as best
                if(distance > minDistance && distance > bestDistance) {
                    bestDistance = distance;
                    best = repair;
                    
                    //if we made it all the way, we can't beat this so return right away
                    if(distance == queueLen) {
                        return best;
                    }
                }
                    
            }
        }
        
        return best;
    }
    
    /* Add some extra tokens to the list from the lexer */
    private void addForwardTokens(Queue<Token> tokens) {
        for(int i = 0; i < LrParser.REPAIR_FORWARD_TOKENS; i++) {
            while(true) {
                try {
                    tokens.add(lex.nextSymbol());
                } catch(ScannerException e) {
                    continue;
                }
                
                break;
            }
            
        }
    }

    public Action.Performed visitAccept(Accept a, Token lookahead) {
        // first make sure that the parse stack finishes its parse
        defered.add(lookahead);
        while(!defered.isEmpty()) {
            parser.nextToken(defered.poll());
        }
        
        // if we ever saw any errors, return ERROR
        if(parser.actions.hasError()) {
            return Action.Performed.ERROR;
        } else {
            return Action.Performed.ACCEPT;
        }
    }

    public void scannerError() {
        parser.actions.scannerError();
    }
    
    private Iterable<SortedSet<Repair>> findRepairs(int maxChanges, int queueLen) {
        return findRepairs(maxChanges, 0, queueLen);
    }
    
    /* Ugly, ugly method that enumates all possible repairs.  Only works for
     * maxChanges = 1, but the whole algorithm is horribly sub-optimal
     */
    private Iterable<SortedSet<Repair>> findRepairs(int maxChanges, int start, int queueLen) {
        if(maxChanges == 1) {
            return new SingletonRepairSet(start, queueLen);
        } else {
            return new UnionSet(maxChanges - 1, start, queueLen);
        }
    }
    
    private class UnionSet implements Iterable<SortedSet<Repair>> {
        private final int maxChanges;
        private final int start;
        private final int queueLen;

        public UnionSet(int maxChanges, int start, int queueLen) {
            this.maxChanges = maxChanges;
            this.start = start;
            this.queueLen = queueLen;
        }

        public Iterator<SortedSet<Repair>> iterator() {
            return new UnionIterator();
        }
        
        private class UnionIterator implements Iterator<SortedSet<Repair>> {
            private int index = start;
            private Iterator<SortedSet<Repair>> it;

            public UnionIterator() {
                it = new MultipleRepairSet(index, findRepairs(maxChanges, start + 1, queueLen)).iterator();
            }

            public boolean hasNext() {
                return index < (queueLen - maxChanges);
            }

            public SortedSet<Repair> next() {
                SortedSet<Repair> set = it.next();
                
                if(!it.hasNext()) {
                    index++;
                    it = new MultipleRepairSet(index, findRepairs(maxChanges, index + 1, queueLen)).iterator();
                }
                
                return set;
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            
        }
    }
    
    private enum NextReturn { DELETE, SUBSTITUTION, INSERTION }
    
    private class MultipleRepairSet implements Iterable<SortedSet<Repair>> {
        private final Iterable<SortedSet<Repair>> set;
        
        private final int index;

        public MultipleRepairSet(int index, Iterable<SortedSet<Repair>> set) {
            this.set = set;
            this.index = index;
        }
        
        public Iterator<SortedSet<Repair>> iterator() {
            Iterator<SortedSet<Repair>> it = set.iterator();
            if(it.hasNext()) {
                return new MultipleIterator(it);
            } else {
                Set<SortedSet<Repair>> temp = Collections.emptySet();
                return temp.iterator();
            }
        }
        
        private class MultipleIterator implements Iterator<SortedSet<Repair>> {
            private int symbol = 0;
            private NextReturn next = NextReturn.DELETE;
            private Iterator<SortedSet<Repair>> it;
            private SortedSet<Repair> current;

            public MultipleIterator(Iterator<SortedSet<Repair>> it) {
                this.it = it;
                current = it.next();
            }

            public boolean hasNext() {
                return symbol < parent.getNumSymbols();
            }

            @SuppressWarnings("unchecked")
            public SortedSet<Repair> next() {
                SortedSet<Repair> repairSet = new TreeSet<Repair>(current);
                
                switch(next) {
                    case DELETE:
                    {
                        next = StateStackActions.NextReturn.SUBSTITUTION;
                        Repair r = new DeletionRepair(index);
                        repairSet.add(r);
                        return repairSet;
                    }
                        
                    case SUBSTITUTION:
                    {
                        next = StateStackActions.NextReturn.INSERTION;
                        Repair r = new SubstitutionRepair(index, factory.makeToken(symbol, null, -1, -1));
                        repairSet.add(r);
                        return repairSet;
                    }
                    
                    case INSERTION:
                    {
                        next = StateStackActions.NextReturn.DELETE;
                        if(!it.hasNext()) {
                            symbol++;
                            it = set.iterator();
                        }
                        
                        current = it.next();
                        
                        Repair r = new InsertionRepair(index, factory.makeToken(symbol, null, -1, -1));
                        repairSet.add(r);
                        return repairSet;                        
                    }
                }
                
                throw new Error();
            }

            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
            
        }
    }
    
    private class SingletonRepairSet implements Iterable<SortedSet<Repair>> {
        
        private final int start;
        private final int queueLen;

        public SingletonRepairSet(int start, int queueLen) {
            this.start = start;
            this.queueLen = queueLen;
        }
        
        public Iterator<SortedSet<Repair>> iterator() {
            return new SingletonIterator();
        }
        
        private class SingletonIterator implements Iterator<SortedSet<Repair>> {
            private int symbol = 0;
            private int index = start;
            private NextReturn next = NextReturn.DELETE;
            
            

            public boolean hasNext() {
                return symbol < parent.getNumSymbols();
            }

            @SuppressWarnings("unchecked")
            public SortedSet<Repair> next() {
                SortedSet<Repair> repairSet = new TreeSet<Repair>();
                switch(next) {
                    
                    case DELETE:
                    {
                        next = StateStackActions.NextReturn.SUBSTITUTION;
                        Repair r = new DeletionRepair(index);
                        repairSet.add(r);
                        return repairSet;
                    }
                        
                    case SUBSTITUTION:
                    {
                        next = StateStackActions.NextReturn.INSERTION;
                        Repair r = new SubstitutionRepair(index, factory.makeToken(symbol, null, -1, -1));
                        repairSet.add(r);
                        return repairSet;
                    }
                    
                    case INSERTION:
                    {
                        next = StateStackActions.NextReturn.DELETE;
                        index++;
                        
                        if(index == queueLen) {
                            index = 0;
                            symbol++;
                        }
                        
                        Repair r = new InsertionRepair(index, factory.makeToken(symbol, null, -1, -1));
                        repairSet.add(r);
                        return repairSet;                        
                    }
                }
                
                throw new Error();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
            
            
        }
    }
}
