/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library;

/**
 *
 * @author svenjager
 */
public class NoCategorieFoundException extends Exception {

    /**
     * Creates a new instance of <code>NoCategorieFoundException</code> without detail message.
     */
    public NoCategorieFoundException() {
    }


    /**
     * Constructs an instance of <code>NoCategorieFoundException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NoCategorieFoundException(String msg) {
        super(msg);
    }
}
