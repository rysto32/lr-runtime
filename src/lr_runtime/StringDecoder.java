/*
 * StringDecoder.java
 *
 * Created on January 23, 2008, 9:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lr_runtime;

import java.util.*;
import java.io.*;

/* Decode a byte array encoded in hexadecimal in a string */
public class StringDecoder extends InputStream {
    
    private final String [] encoded;
    private int index = 0;
    private int arrayIndex = 0;
    
    public StringDecoder(String [] s) {
        encoded = s;
    }

    @Override
    public void close() {
    }
    
    private int writeCount = 0;
    
    int decodeChar(char c) {
        if(c <= '9') {
            return c - '0';
        } else {
            return c - 'A' + 10;
        }
    }
    
    @Override
    public int read() {
            
        if(arrayIndex >= encoded.length) {
            return -1;
        }
            
        if((index + 1) >= encoded[arrayIndex].length()) {
            index = 0;
            
            arrayIndex++;
            
            if(arrayIndex >= encoded.length) {
                return -1;
            }
        }
        
        int c = decodeChar(encoded[arrayIndex].charAt(index));
        c += decodeChar(encoded[arrayIndex].charAt(index + 1)) << 4;
/*        
        if(writeCount < 10) {
            System.err.println(c + " (" + encoded.charAt(index) + encoded.charAt(index + 1) + ")");
            writeCount++;
        }
  */      
        index += 2;
        
        return c;
    }
    
}
