/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp;

import swp.graphic.java3d.PickableObject;

import java.awt.*;
import java.awt.event.*;

import javax.media.j3d.*;
import javax.swing.*;
import swp.graphic.IGraphicPort;
import swp.graphic.IPickable;
import swp.model.ModelPort;

/**
 *
 * @author Administrator
 */
public class PortPopup extends JPopupMenu
{

    private JMenuItem jMenu1 = new JMenuItem();
   
    public swp.steuerung.Steuerung steuerung;
    private IGraphicPort m_port;

    /**
     * Konstruktor, erstellt das PopupMenü für Knickpunkte
     */
    public PortPopup()
    {
        jMenu1.setText("Port Löschen");
        jMenu1.addActionListener(new deletePortListener());

        this.add(jMenu1);

    }

    /**
     * setzt den Knickpunkt als ausgewählt
     * @param selected aktuell selectiertes Object
     */
    public void setSelected(PickableObject selected)
    {
        m_port = (swp.graphic.java3d.Port) selected;
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
        m_port = (IGraphicPort) selected;
        super.show(c, X, Y);
    }

    /**
     * löscht eine Connection
     */
    class deletePortListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (!((ModelPort)( m_port).getModelRef()).isRequiered())
            {
                ( m_port).delete();
            }
        }
    }
}
