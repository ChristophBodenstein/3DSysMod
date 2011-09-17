package swp;

import swp.gui.SourceEditorFrame;
import swp.gui.ChartFrame;
import swp.gui.PositionDialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Math.*;

import javax.media.j3d.*;
import javax.swing.*;
import swp.graphic.IColorable;
import swp.graphic.IGraphic;
import swp.graphic.IGraphicElement;
import swp.graphic.INamable;
import swp.graphic.IPickable;
import swp.graphic.IPositionable;
import swp.graphic.IScalable;
import swp.gui.CaptionDialog;
import swp.gui.ColorDialog;
import swp.gui.ScaleDialog;
import swp.library.Vis.IVisElement;
import swp.library.script.IScriptElement;
import swp.model.ModelElement;
import swp.model.ModelPort;

/**
 * Klasse PopupMenue
 * @author Timmä
 */
public class PopupMenue extends JPopupMenu
{

    private JMenuItem jMenu1 = new JMenuItem();
    private JMenuItem jMenu2 = new JMenuItem();
    private JMenuItem jMenu3 = new JMenuItem();
    private JMenuItem jMenu6 = new JMenuItem();
    private JMenuItem jMenu7 = new JMenuItem();
    private JMenuItem jMenu8 = new JMenuItem();
    private JMenuItem jMenu9 = new JMenuItem();
    private JMenuItem jMenu10 = new JMenuItem();
    private JMenuItem jMenu11 = new JMenuItem();
    public IGraphic cube;
    private ModelElement m_modelElement = null;
    public swp.steuerung.Steuerung steuerung;

    /**
     * Konstruktor erstellt das Menü
     */
    public PopupMenue(IGraphic graph)
    {

        if (IColorable.class.isInstance(graph))
        {
            jMenu1.setText("Farbe ändern");
            jMenu1.addActionListener(new newColorListener());
            this.add(jMenu1);
        }

        if (INamable.class.isInstance(graph))
        {
            jMenu2.setText("Beschriftung ändern");
            jMenu2.addActionListener(new newCaptionListener());
            this.add(jMenu2);
        }

        if (IScalable.class.isInstance(graph))
        {
            jMenu3.setText("Größe ändern");
            jMenu3.addActionListener(new scaleListener());
            this.add(jMenu3);
        }
        if (IPositionable.class.isInstance(graph))
        {
            jMenu6.setText("Position festlegen");
            jMenu6.addActionListener(new newPositionListener());
            this.add(jMenu6);
        }




        if (IGraphicElement.class.isInstance(graph))
        {


            jMenu9.setText("Inputport hinzufügen");
            jMenu9.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {

                    if (m_modelElement != null)
                    {
                        m_modelElement.addPort(new ModelPort(m_modelElement, ModelPort.PortType.INPUT));
                    }
                }
            });
            this.add(jMenu9);

            jMenu10.setText("Outputport hinzufügen");
            jMenu10.addActionListener(new ActionListener()
            {

                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (m_modelElement != null)
                    {
                        m_modelElement.addPort(new ModelPort(m_modelElement, ModelPort.PortType.OUTPUT));
                    }
                }
            });

            this.add(jMenu10);

            m_modelElement = (ModelElement) graph.getModelRef();

            if (IVisElement.class.isInstance(m_modelElement))
            {
                jMenu8.setText("Zeige Ergebnisse");
                jMenu8.setEnabled(false);
                jMenu8.addActionListener(new ActionListener()
                {

                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        ChartFrame pan = new ChartFrame(m_modelElement);
                        pan.setVisible(true);
                    }
                });
                this.add(jMenu8);
            }

            if (IScriptElement.class.isInstance(m_modelElement))
            {
                jMenu11.setText("Quelltext anzeigen");
                jMenu11.setEnabled(false);
                jMenu11.addActionListener(new ActionListener()
                {

                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        SourceEditorFrame frm = new SourceEditorFrame(m_modelElement);
                        frm.setVisible(true);
                    }
                });

                this.add(jMenu11);
            }

        }



        jMenu7.setText("Löschen");
        jMenu7.addActionListener(new deleteListener());
        this.add(jMenu7);


        //jPopupMenu.addAncestorListener(listener);
    }

    /**
     * Zeigt das PopupMenü bei rechtsklick auf einen Würfel
     * @param c3d Canvas des Programms
     * @param selected Selectierter Würfel
     * @param X X koordinate wo das PopupMenü erscheint
     * @param Y Y koordinate wo das Popupmenü erscheint
     * @param str
     */
    public void show(Component c, IPickable selected, int X, int Y, swp.steuerung.Steuerung str)
    {
        if (!IGraphicElement.class.isInstance(selected))
        {
            return;
        }


        super.show(c, X, Y);


        cube = (IGraphicElement) selected;


        m_modelElement = (ModelElement) cube.getModelRef();
        if (IVisElement.class.isInstance(m_modelElement))
        {
            if (((IVisElement) m_modelElement).hasResults())
            {
                jMenu8.setEnabled(true);
            }
        }

        if (IScriptElement.class.isInstance(m_modelElement))
        {
            jMenu11.setEnabled(true);
        }

        steuerung = str;
    }

    /**
     * ruft den Farbauswahldialog aus dem PopupMenü auf
     */
    class newColorListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (IColorable.class.isInstance(steuerung.getSelected()))
            {
                ColorDialog colDialog = new ColorDialog((IColorable) steuerung.getSelected());
                colDialog.setVisible(true);
            }
        }
    }

    /**
     * ruft den Beschriftungsdialog aus dem Pupupmenü auf
     */
    class newCaptionListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (INamable.class.isInstance(steuerung.getSelected()))
            {
                CaptionDialog capDialog = new CaptionDialog((INamable) steuerung.getSelected());
                capDialog.setVisible(true);
            }
        }
    }

    /**
     * ruft den Positionierungsdialog aus dem Popupmenü auf
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

    /**
     * Löscht ein Object aus dem PopupMenü
     */
    class deleteListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            cube.delete();
            steuerung.setSelected(null);
        }
    }

    /**
     * ruft den Skalierdialog aus dem Popupmenü auf
     */
    class scaleListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if (IScalable.class.isInstance(steuerung.getSelected()))
            {
                ScaleDialog scalDialog = new ScaleDialog((IScalable) steuerung.getSelected());
                scalDialog.setVisible(true);
            }
        }
    }
}



