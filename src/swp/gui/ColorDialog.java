/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Color3f;
import swp.graphic.IColorable;

/**
 *
 * @author svenjager
 */
public class ColorDialog extends JFrame
{

    Color3f colorold = new Color3f(0.0f, 0.0f, 0.0f);
    Color3f colornew = new Color3f(0.0f, 0.0f, 0.0f);
    JSlider jslider = new JSlider(0, 100, 50);
    JButton okButton = new JButton();
    JButton zurueckButton = new JButton();
    private JColorChooser jColor = new JColorChooser();
    ColorDialog m_instance = null;
    IColorable m_object = null;


    public ColorDialog(IColorable object)
    {
        super();
        m_instance = this;
        m_object = object;

        this.setBounds(110, 97, 530, 322);
        this.add(jColor, BorderLayout.CENTER);
        this.add(jslider, BorderLayout.EAST);
        this.add(okButton, BorderLayout.SOUTH);


        okButton.setText("OK");
        okButton.addActionListener(new okButtonListener());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        
        this.setTitle("Farbe des Würfels");
        //frame.setIconImage(new ImageIcon("src\\swp\\32x32_Wuerfel_gruen_transp.gif").getImage());
        Border border1 = BorderFactory.createTitledBorder("Farbe");
        jColor.setBorder(border1);
        jColor.setPreviewPanel(new JLabel());
        jColor.getSelectionModel().addChangeListener(new colorClickedListener());
        Border border2 = BorderFactory.createTitledBorder("Sichtbarkeit");
        jslider.setBorder(border2);
        jslider.addChangeListener(new transperancyClickedListener());
        jslider.setOrientation(JSlider.VERTICAL);
        jslider.setMinorTickSpacing(100);
        jslider.setMajorTickSpacing(10);
        jslider.setPaintTicks(true);
        jslider.setPaintLabels(true);
        Hashtable<Integer, JLabel> dict = new Hashtable<Integer, JLabel>();
        dict.put(0, new JLabel("    0%"));
        dict.put(100, new JLabel("    100%"));
        jslider.setLabelTable(dict);
        
    }

    class okButtonListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            m_instance.setVisible(false);
        }
    }

    class colorClickedListener implements ChangeListener
    {

        public void stateChanged(ChangeEvent e)
        {
            colornew.set(jColor.getColor());
            m_object.setColor(colornew);
        }
    }

    class transperancyClickedListener implements ChangeListener
    {

        public void stateChanged(ChangeEvent e)
        {
            m_object.setCubeTransperancy(1 - (jslider.getValue() / 100f));
        }
    }
}


