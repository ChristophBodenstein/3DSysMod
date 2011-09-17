/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library.ptolemy;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import ptolemy.actor.Actor;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLParser;
import swp.library.ILibrary;
import swp.library.NoElementFoundException;
import swp.model.ModelElement;

/**
 *
 * @author svenjager
 */
public class PtolemyLibrary implements ILibrary{


    private DefaultMutableTreeNode m_root = null;

    private NamedObj m_ptolemyClassTree = null;

    public PtolemyLibrary()
    {
        try
        {
            m_root = new DefaultMutableTreeNode(this);

            MoMLParser momParser = new MoMLParser();


            //URL filename = new URL("file:resource/Library/Ptolemy/rootEntityLibrary.xml");
            URL filename = new URL("file:resource/Library/Ptolemy/rootEntityLibrary.xml");
           
            File file = new File(filename.getFile());

            m_ptolemyClassTree = momParser.parse(file.toURI().toURL(), new FileInputStream(file));
            Iterator containedObjectsIterator = m_ptolemyClassTree.containedObjectsIterator();

            while (containedObjectsIterator.hasNext())
            {
                Object ob = containedObjectsIterator.next();
                if (ob instanceof ptolemy.moml.EntityLibrary)
                {             
                    this.recursive((ptolemy.moml.EntityLibrary) ob,m_root);
                }
            }

        } catch (Exception ex)
        {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getName()
    {
        return new String("Ptolemy");
    }


    @Override
    public DefaultMutableTreeNode getLibRootNode()
    {  
        return m_root;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public ModelElement createElementByPath(TreePath path) throws NoElementFoundException
    {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();

        if(node!=null)
        {
            if(node.getUserObject() instanceof Actor)
            {
                Class elmClass = ((Actor)node.getUserObject()).getClass();
                return new PtolemyElement(elmClass);
            }
        }

        throw new NoElementFoundException();
    }

    

     private void recursive(ptolemy.moml.EntityLibrary eLibrary, DefaultMutableTreeNode node)
    {
        DefaultMutableTreeNode newnode =new DefaultMutableTreeNode(eLibrary.getDisplayName());
        node.add(newnode);
        
        Iterator iterator = eLibrary.entityList().iterator();
        while (iterator.hasNext())
        {
            Object el = iterator.next();

            if (el instanceof ptolemy.moml.EntityLibrary)
            {
                recursive((EntityLibrary) el,newnode);

            } else
            {
                if(el instanceof Actor)
                {
                    newnode.add(new PtolemyMutableTreeNode(el));
                }
            }
        }

    }

    @Override
    public ModelElement createElementByName(String name) throws NoElementFoundException
    {
        try
        {
            return new PtolemyElement(Class.forName(name));
        } catch (ClassNotFoundException ex)
        {
            Logger.getLogger(PtolemyLibrary.class.getName()).log(Level.SEVERE, null, ex);
            throw new NoElementFoundException();
        }
    }
}
