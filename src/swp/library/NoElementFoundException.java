/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library;

/**
 *
 * @author svenjager
 */
public class NoElementFoundException extends Exception {

    /**
     * Creates a new instance of <code>NoElementFoundException</code> without detail message.
     */
    public NoElementFoundException() {
    }


    /**
     * Constructs an instance of <code>NoElementFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoElementFoundException(String msg) {
        super(msg);
    }
}
