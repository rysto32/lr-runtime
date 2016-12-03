/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.io.IOException;
import java.util.*;

/**
 * A scanner that can remember a single "checkpoint" and return to that point.
 * Used in error repair: when we hit an error, we set a checkpoint and keep trying
 * different possible repairs.  We have to reset this scanner back to the checkpoint
 * before trying each repair.
 */
public class CheckpointScanner implements Scanner {
    // The scanner that is generating our tokens
    private final Scanner lex;
    // The list of tokens that we've saved for this checkpoint
    private final List<Token> savedTokens = new ArrayList<Token>();
    
    private enum CheckpointState {
        PASS_THROUGH,   // normal operation -- pass tokens to caller and do nothing else
        SAVE,           // checkpoint is active so save tokens before passing them to caller
        FROM_SAVED,     // return saved tokens, and when saved tokens are exhausted save new tokens
        SAVED_TO_PASS_THROUGH   // return saved tokens, and when saved tokens are 
                                //      exhausted go to PASS_THROUGH state
    }
    
    // the next token to return from savedTokens
    private int nextToken = 0;
    private CheckpointState state = CheckpointState.PASS_THROUGH;

    public CheckpointScanner(Scanner lex) {
        this.lex = lex;
    }

    public Token nextSymbol() throws ScannerException {
        try {
            switch(state) {
                case PASS_THROUGH:
                    return lex.nextSymbol();

                case SAVE:
                {
                    Token t = lex.nextSymbol();
                    savedTokens.add(t);
                    return t;
                }

                case FROM_SAVED:
                {
                    Token t = savedTokens.get(nextToken);
                    nextToken++;

                    if(nextToken == savedTokens.size()) {
                        state = CheckpointState.SAVE;
                    }

                    return t;
                }

                case SAVED_TO_PASS_THROUGH:
                {
                    Token t = savedTokens.get(nextToken);
                    nextToken++;

                    if(nextToken == savedTokens.size()) {
                        state = CheckpointState.PASS_THROUGH;
                    }

                    return t;
                }

                default:
                    throw new Error();
            }
        } catch(IOException e) {
            throw new Error(e);
        }
    }
    
    public void checkpoint() {
        if(state == CheckpointState.PASS_THROUGH) {
            state = CheckpointState.SAVE;
            savedTokens.clear();
        } else if(state == CheckpointState.SAVED_TO_PASS_THROUGH) {
            state = CheckpointState.SAVE;
            savedTokens.subList(0, nextToken).clear();
        } else {
            throw new Error();
        }
    }
    
    public void reset() {
        if(state == CheckpointState.PASS_THROUGH || 
                state == CheckpointState.SAVED_TO_PASS_THROUGH) 
        {
            throw new Error();
        }
        
        if(savedTokens.isEmpty()) {
            state = CheckpointState.SAVE;
        } else {
            state = CheckpointState.FROM_SAVED;
            nextToken = 0;
        }
    }
    
    public void clearCheckpoint() {
        if(state == CheckpointState.PASS_THROUGH || 
                state == CheckpointState.SAVED_TO_PASS_THROUGH) 
        {
            throw new Error();
        }
        
        if(savedTokens.isEmpty()) {
            state = CheckpointState.PASS_THROUGH;
        } else {
            state = CheckpointState.SAVED_TO_PASS_THROUGH;
            nextToken = 0;
        }
        
    }
    
}
