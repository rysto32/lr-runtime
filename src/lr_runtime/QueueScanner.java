/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;

/**
 * A scanner that reads tokens from a queue.  Don't let the queue empty.
 */
public class QueueScanner implements Scanner {
    private Queue<Token> queue;

    public QueueScanner(Queue<Token> queue) {
        this.queue = queue;
    }

    public Token nextSymbol() throws ScannerException {
        return queue.remove();
    }
}
