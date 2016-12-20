/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;
import lr_runtime.Action.Performed;

/**
 * The actions performed by the parse stack
 */ 
class ParseStackActions implements StackActions<ParseStackActions> {
    private final Stack<Token> parseStack = new Stack<Token>();
    
    private final LrParser parser;
    private final TokenFactory factory;
    
    private int parserState;
    private boolean haveError = false;

    ParseStackActions(LrParser parser, TokenFactory f) {
        this.parser = parser;
        parserState = parser.getStartState();
        factory = f;
    }
    
    private ParseStackActions(ParseStackActions template) {
        parser = template.parser;
        factory = template.factory;
        parserState = template.parserState;
        haveError = template.haveError;

        for (Token t : template.parseStack) {
            Token copy = factory.makeToken(t.sym, t.value, t.line, t.column);
            copy.state = t.state;
            parseStack.push(copy);
        }
    }
    
    public ParseStackActions branch() {
        return new ParseStackActions(this);
    }
    
    public Object getStackTop() {
        return parseStack.peek().value;
    }

    public Action.Performed visitShift(Shift s, Token t) {
        t.state = parserState;

        parseStack.push(t);
        parserState = s.dest;

        return Action.Performed.SHIFT;
    }

    @SuppressWarnings("unchecked")
    public Action.Performed visitReduce(Reduce r, Token token) {
        int productionId = r.productionId;
        int numPops = parser.getProductionLength(productionId);

        Object v = null;
        if(!haveError) {
            //we only perform actions while the parse is still valid
            v = parser.performAction(productionId, 
                    parseStack.subList(parseStack.size() - numPops, parseStack.size()));
        }

        int line = -1;
        int column = -1;
        // pop RHS of production off of the stack
        for(int i = 0; i < numPops; i++) {
            Token old = parseStack.pop();
            
            if(line < 0) {
                line = old.line;
                column = old.column;
            }

            parserState = old.state;
        }

        // push the LHS of the production
        int lhsSymbol = parser.getLhsSymbol(productionId);

        Token t = factory.makeToken(lhsSymbol, v, line, column);
        t.state = parserState;
        parseStack.push(t);

        //will throw NullPointerException if goto does not contain (state, lhsSymbol)
        //this should never happen, though: only the action table can signal error
        parserState = parser.table.getGoto(parserState, lhsSymbol);

        return Action.Performed.REDUCE;
    }

    public int getState() {
        return parserState;
    }

    // The parse stack should *never* see an error -- the state stack should either
    // repair any errors or bail out before the error reaches the parse stack
    public Performed visitReject(Reject r, Token lookahead) {
        throw new Error("Parse stack hit an error at token " + lookahead + "(" + lookahead.line + ")");
    }

    // We've hit acceptance: signal error if we saw an error earlier but repaired it
    public Action.Performed visitAccept(Accept a, Token lookahead) {
        if(haveError) {
            return Action.Performed.ERROR;
        } else {
            return Action.Performed.ACCEPT;
        }
    }

    /* Called when somebody else detects a scanner error */
    public void scannerError() {
        setError();
    }
    
    /* Called when somebody else detects a parse error */
    public void setError() {
        haveError = true;
    }
    
    public boolean hasError() {
        return haveError;
    }

    Stack<Token> getParseStack() {
        return parseStack;
    }
    
    
}
