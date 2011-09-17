/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp;

import swp.gui.PositionDialog;
import swp.graphic.java3d.PickableObject;

import java.awt.*;
import java.awt.event.*;

import javax.media.j3d.*;
import javax.swing.*;
import swp.graphic.IPickable;
import swp.graphic.IPositionable;
import swp.graphic.java3d.Connection;

/**
 *
 * @author Administrator
 */
public class Inflexpopup extends JPopupMenu
{

    private JMenuItem jMenu1 = new JMenuItem();
    private JMenuItem jMenu2 = new JMenuItem();
    private JMenuItem jMenu3 = new JMenuItem();
    private JMenuItem jMenu4 = new JMenuItem();
    public swp.steuerung.Steuerung steuerung;
    private swp.graphic.java3d.InflexPoint ipoint;

    /**
     * Konstruktor, erstellt das PopupMenü für Knickpunkte
     */
    public Inflexpopup()
    {
        jMenu1.setText("Verbindung Löschen");
        jMenu1.addActionListener(new deleteLineListener());
        jMenu2.setText("Knickpunkt Löschen");
        jMenu2.addActionListener(new deletePointListener());
        jMenu3.setText("Knickpunkt Hinzufügen");
        jMenu3.addActionListener(new addPointListener());
        jMenu4.setText("Position festlegen");
        jMenu4.addActionListener(new newPositionListener());
        this.add(jMenu1);
        this.add(jMenu2);
        this.add(jMenu3);
        this.add(jMenu4);
    }

    /**
     * setzt den Knickpunkt als ausgewählt
     * @param selected aktuell selectiertes Object
     */
    public void setSelected(PickableObject selected)
    {
        ipoint = (swp.graphic.java3d.InflexPoint) selected;
    }

    /**
     * zeigt das Popupmenü
     * @param c3d Vanvas des Programms
     * @param selected aktuell selectiertes Objekt
     * @param X X koordiante wo das KontextMenü erscheint
     * @param Y Y koordinate wo das Kontexmenü erscheint
     */
    public void show(Component c, IPickable selected, int X, int Y)
    {
        ipoint = (swp.graphic.java3d.InflexPoint) selected;
        if ((ipoint.getNextNode() instanceof swp.graphic.java3d.Port) && (ipoint.getPrevNode() instanceof swp.graphic.java3d.Port))
        {
            jMenu2.setEnabled(false);
        } else
        {
            jMenu2.setEnabled(true);
        }
        super.show(c, X, Y);
    }

    /**
     * löscht eine Connection
     */
    class deleteLineListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            //((BranchGroup)ipoint.getParent()).detach();
            ((Connection) ipoint.getParent()).delete();

        }
    }

    /**
     * löschte einen Knickpunkt
     */
    class deletePointListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            ipoint.delete();
        }
    }

    /**
     * fügt einen Knickpunkt hinzu
     */
    class addPointListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            ipoint.addInflexPoint();
        }
    }

    /**
     * erstellt den dialog zur festlegung der Position eines Knickpunktes
     */
    class newPositionListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (IPositionable.class.isInstance(steuerung.getSelected()))
            {
                PositionDialog posDialog = new PositionDialog((IPositionable) steuerung.getSelected());
                posDialog.setVisible(true);
            }
        }
    }
}
