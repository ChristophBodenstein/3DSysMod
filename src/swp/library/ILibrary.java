/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.library;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import swp.model.ModelElement;

/**
 *
 * @author Sven Jäger
 */
public interface ILibrary {

    public String getName();

    public ModelElement createElementByPath(TreePath path) throws NoElementFoundException;
    public ModelElement createElementByName(String name) throws NoElementFoundException;

    public DefaultMutableTreeNode getLibRootNode();

    @Override
    public String toString();

}
