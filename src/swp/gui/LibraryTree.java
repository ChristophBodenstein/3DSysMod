package swp.gui;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;

import swp.*;

public class LibraryTree extends JTree implements MouseListener {

  private GUI gui;
  
  public LibraryTree(DefaultMutableTreeNode node, GUI g) {
    super(node);
    gui = g;
    addMouseListener(this);
    setDragEnabled(true);
    DefaultTreeSelectionModel model = new DefaultTreeSelectionModel();
    model.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    setSelectionModel(model);
  }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (e.getClickCount() == 2) {
        gui.createNewCube(this);
      }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}