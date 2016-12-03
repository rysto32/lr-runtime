/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package lr_runtime;

/**
 *  Thrown by a Scanner to indicate a lexical error
 *
 * @author rstone
 */
public class ScannerException extends Exception {

    public ScannerException() {
    }

    public ScannerException(String msg) {
        super(msg);
    }

    public ScannerException(Throwable cause) {
        super(cause);
    }

    public ScannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
