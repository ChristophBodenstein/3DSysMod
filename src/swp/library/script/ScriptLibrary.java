/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.library.script;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import swp.library.ILibrary;
import swp.library.NoElementFoundException;
import swp.model.ModelElement;

/**
 *
 * @author svenjager
 */
public class ScriptLibrary implements ILibrary
{

    Hashtable<String, String> m_elementlist = new Hashtable<String, String>();

    public ScriptLibrary()
    {
        //durchsuche Script pfad
        File f = new File("resource/Library/Script");

        File[] fileArray = f.listFiles();

        if(fileArray==null) return;
        
        for (int i = 0; i < fileArray.length; i++)
        {
            String filename = fileArray[i].getName();
            String ext = filename.substring(filename.lastIndexOf('.') + 1, filename.length());

            //nur python-scripts suchen
            if (ext.equals("py"))
            {
                m_elementlist.put(filename, fileArray[i].getAbsolutePath());
            }
        }

    }

    @Override
    public String getName()
    {
        return new String("Script");
    }

    @Override
    public ModelElement createElementByPath(TreePath path) throws NoElementFoundException
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        String name = (String) node.getUserObject();

        
        String filename = m_elementlist.get(name);
        if(!filename.isEmpty())
        {
            return new ScriptElement(filename);
        }



        throw new NoElementFoundException();
    }

    @Override
    public DefaultMutableTreeNode getLibRootNode()
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
        
        Enumeration<String> keys = m_elementlist.keys();

        while (keys.hasMoreElements())
        {
            String name = keys.nextElement();
            root.add(new DefaultMutableTreeNode(name));
        }

        


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
        return new ScriptElement("");
    }
}
