/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library.ptolemy;

import javax.swing.tree.DefaultMutableTreeNode;
import ptolemy.actor.Actor;

/**
 *
 * @author svenjager
 */
public class PtolemyMutableTreeNode extends DefaultMutableTreeNode{

    PtolemyMutableTreeNode(Object el)
    {
        super(el);
    }


    @Override
    public String toString()
    {
        if(userObject instanceof Actor)
        {
            return ((Actor)userObject).getDisplayName();
        }

        return new String(this.getClass().toString());
    }
    
    
}
