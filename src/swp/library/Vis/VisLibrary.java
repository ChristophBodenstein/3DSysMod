/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library.Vis;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import swp.library.ILibrary;
import swp.library.NoElementFoundException;
import swp.model.ModelElement;

/**
 *
 * @author svenjager
 */
public class VisLibrary implements ILibrary{

    @Override
    public String getName()
    {
        return new String("Anzeige");
    }

    @Override
    public ModelElement createElementByPath(TreePath path) throws NoElementFoundException
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        String name = (String) node.getUserObject();

        if (name.equals("XYPlotter"))
        {
            return new XYPlotterElement();
        }

        throw new NoElementFoundException();
    }

    @Override
    public DefaultMutableTreeNode getLibRootNode()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
        
        root.add(new DefaultMutableTreeNode("XYPlotter"));


        return root;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public ModelElement createElementByName(String name) throws NoElementFoundException
    {
        if(XYPlotterElement.class.getName().equals(name))
        {
            return new XYPlotterElement();
        }
        return null;
    }

}
