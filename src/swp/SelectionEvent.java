/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp;

import java.util.EventObject;

/**
 *
 * @author svenjager
 */
public class SelectionEvent extends EventObject
{
    private Object m_selectedObject;

    public SelectionEvent(Object source, Object selectedObject)
    {
        super(source);
        m_selectedObject = selectedObject;
    }

    public Object getSelectedObject()
    {
        return m_selectedObject;
    }
}
