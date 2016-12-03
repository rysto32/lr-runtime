/*
 * StringEncoder.java
 *
 * Created on January 23, 2008, 9:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;
import java.io.*;

/* Encode bytes into a string */
public class StringEncoder extends OutputStream {
    
    private final StringBuilder builder = new StringBuilder();
    
    /* use a simple hexadecimal encoding for now */
    static final char[] encoding = "0123456789ABCDEF".toCharArray();
    
    public StringEncoder() {
    }
    
    @Override
    public String toString() {
        return builder.toString();
    }
    
    public void clear() {
        builder.delete(0, builder.length());
    }
    
    private int writeCount = 0;

    @Override
    public void write(int b) throws IOException {
        /*
        if(writeCount < 10) {
            System.err.println(b + " (" + encoding[b & 0xf] + encoding[(b >> 4) & 0xf] + ")");
            writeCount++;
        }
         */
        
        builder.append(encoding[b & 0xf]);
        builder.append(encoding[(b >> 4) & 0xf]);
    }

}
