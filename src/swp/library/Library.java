/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import swp.library.Vis.VisLibrary;
import swp.library.ptolemy.PtolemyLibrary;
import swp.library.script.ScriptLibrary;
import swp.model.ModelElement;

/**
 *
 * @author svenjager
 */
public class Library {

    /**
     *
     */
    private static Library instance = null;

   
    /**
     * Liefert die Instanz der Library
     * @return
     */
    public static Library getInstance()
    {
        if(instance ==null){
            instance = new Library();
        }

        return instance;    
    }


    private DefaultMutableTreeNode m_root = null;
    
    private ILibrary[] libraryList = new ILibrary[3];

    public Library()
    {
        libraryList[0] = new PtolemyLibrary();
        libraryList[1] = new VisLibrary();
        libraryList[2] = new ScriptLibrary();

        //create root treenode
        m_root = new DefaultMutableTreeNode("Library");

        for(int i = 0; i<libraryList.length;i++)
        {
            m_root.add(libraryList[i].getLibRootNode());
        }

    }

    public ModelElement createElementByPath(TreePath path) throws NoElementFoundException
    {
        DefaultMutableTreeNode libNode = (DefaultMutableTreeNode)path.getPathComponent(1);
        
        ILibrary lib = (ILibrary) libNode.getUserObject();
        
        if(lib!=null)
        {   
            return lib.createElementByPath(path);
        }
        

        throw new NoElementFoundException();
    }

    public DefaultMutableTreeNode getRootNode()
    {
        
       return m_root;
    }

    public ModelElement createElement(String creatorName, String className) throws NoElementFoundException
    {

        for(int i = 0; i< libraryList.length;i++)
        {
            try
            {
                if (Class.forName(creatorName).isInstance(libraryList[i]))
                {
                   return libraryList[i].createElementByName(className);
                }
            } catch (ClassNotFoundException ex)
            {
                Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
                throw new NoElementFoundException();
            }
        }

        return null;
    }

}
