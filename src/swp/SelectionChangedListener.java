/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp;

import java.util.EventListener;

/**
 *
 * @author svenjager
 */
public interface SelectionChangedListener extends EventListener {
    
    public void selectionChanged(SelectionEvent e);

}
