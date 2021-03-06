/*
 * LrParser.java
 *
 * Created on January 23, 2008, 7:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;
import java.io.*;
import java.util.zip.*;

/*
 * Superclass of all generated LR parsers
 */
public abstract class LrParser {
    final ParseTable table;

    static final int DEFAULT_PARSER = 0;

    private final Map<Integer, ParserInstance> instances = new HashMap<Integer, ParserInstance>();
    private int nextInstance = DEFAULT_PARSER + 1;

    static final int DEFERED_TOKENS_LENGTH = 20;
    static final int MAX_REPAIR_CHANGES = 1;
    static final int REPAIR_FORWARD_TOKENS = 4;

    public LrParser(String ... parseTable) {
        table = decode(parseTable);
    }

    /* Parse a file.  Returns the value of the start symbol, or null on error  */
    public Object parse(Scanner lex, TokenFactory factory) {
        instances.put(DEFAULT_PARSER, new ParserInstance(lex, factory, this));
        Action.Performed p;
        
        do {
            ParserInstance parser;
            Token lookahead;

            try {
                lookahead = lex.nextSymbol();
            } catch(ScannerException e) {
                throw new Error(e);
            } catch(IOException e) {
                throw new Error(e);
            }

            parser = instances.get(lookahead.parser);
            p = parser.nextToken(lookahead);
        } while(!p.finalState());
        
        if(p == Action.Performed.ERROR) {
            return null;
        } else {
            return instances.get(DEFAULT_PARSER).getStackTop();
        }
    }
    
    /**
     * Branch the state of the given parser.
     * @param instance The instance number of the parser to branch
     * @return The instance number of the new parser.
     */
    public int branchParser(int instance) {
        int newInstance = nextInstance;
        nextInstance++;
        
        ParserInstance newParser = instances.get(instance).branch();
        instances.put(newInstance, newParser);
        return newInstance;
    }
    
    /* Return the number of the start state */
    protected abstract int getStartState();
    
    /* Return the total number of symbols in the vocabulary */
    protected abstract int getNumSymbols();
    
    /* Return the number of synbols on the RHS of the given production */
    protected abstract int getProductionLength(int productionId);
    
    /* Return the Sym value of the symbol on the LHS of this production */
    protected abstract int getLhsSymbol(int productionId);
    
    /* Perform the action associated with the given production, if any action exists */
    protected abstract Object performAction(int productionId, List<Token> rhs);
    
    /* Convert Sym value to human-readable form.  Should be overriden */
    protected String convertSym(int sym) {
        return Integer.toString(sym);
    }
    
    /* Convert a list of Sym values to a list of strings */
    List<String> convertPossibleNext(Set<Integer> possible) {
        List<String> list = new ArrayList<String>(possible.size());
        
        for(int i : possible) {
            list.add(convertSym(i));
        }
        
        return list;
    }
    
    /*
     * Decode a parse table from the string array.
     * 
     * At this point, you're probably thinking "from a string array"?  Yeah, it's
     * a hack, but it works.  You're probably better off not examining this
     * method closely for the sake of your own sanity,
     */
    private ParseTable decode(String [] table) {
        try {
            StringDecoder decode = new StringDecoder(table);

            ObjectInputStream stream = new ObjectInputStream(new GZIPInputStream(decode));

            return (ParseTable)stream.readObject();
        } catch(IOException e) {
            throw new Error(e);
        } catch(ClassNotFoundException e) {
            throw new Error(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected static lr_runtime.Token findFirst(java.util.List<lr_runtime.Token> list) {
        for(lr_runtime.Token t : list) {
            if(t != null && t.getLine() >= 0) {
                return t;
            }
        }
        return null;
    }
    
    // Method called to report a parse error 
    public abstract void reportError(Token lookahead, int state, List<String> possible);    
    
    // Method called to inform the user of a suggested fix to the last error
    public abstract void suggestRepair(List<Token> list, NavigableSet<Integer> changes);
    
    // Method called to report that no repair is possible
    public abstract void reportGiveUp(Token errorToken);
}
