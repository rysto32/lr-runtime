/*
 * Scanner.java
 *
 * Created on January 23, 2008, 8:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.io.*;

/**
 * Interface for the scanner -- the class that reads the input file and
 * tokenizes it.
 */
public interface Scanner {
    // Return the next Token from the file, or throws an exception on error
    // Must return an EOF token as the last token in the file
    public Token nextSymbol() throws ScannerException, IOException;
}
