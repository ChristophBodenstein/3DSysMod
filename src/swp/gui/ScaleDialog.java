/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

import java.util.Hashtable;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import swp.graphic.IScalable;

/**
 *
 * @author svenjager
 */
public class ScaleDialog extends JFrame
{

    private JSlider jSlider1;
    private JSpinner jSpinner1;
    private ScaleDialog m_instance = null;
    private IScalable m_object = null;

   
    public ScaleDialog(IScalable object)
    {
        super();
        m_object = object;
        m_instance = this;


        this.setLocation(110, 97);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);
        this.setTitle("Größe des Würfels");
        this.setResizable(false);
        //this.setIconImage(new ImageIcon("src\\swp\\32x32_Wuerfel_gruen_transp.gif").getImage());


        jSlider1 = new javax.swing.JSlider(1, 100, 1);
        jSlider1.addChangeListener(new sliderChangeListener());
        jSpinner1 = new javax.swing.JSpinner();
        jSpinner1.addChangeListener(new spinnerChangeListener());
        jSlider1.setPaintTicks(true);
        jSlider1.setMajorTickSpacing(20);
        JLabel label = new JLabel();
        Hashtable<Integer, JLabel> dict = new Hashtable<Integer, JLabel>();
        label.setText("1");
        dict.put(1, label);
        for (int i = 10; i <= 100; i += 10)
        {
            dict.put(i, new JLabel(Integer.toString(i)));
        }
        jSlider1.setLabelTable(dict);
        jSlider1.setMinorTickSpacing(1);
        jSlider1.setPaintTrack(true);
        jSlider1.setPaintLabels(true);
        jSlider1.setSnapToTicks(true);

        jSlider1.setValue(m_object.getScale());
        jSpinner1.setValue(m_object.getScale());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jSpinner1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE).addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        pack();
    }

    class sliderChangeListener implements ChangeListener
    {

        public void stateChanged(ChangeEvent e)
        {
            jSpinner1.setValue(jSlider1.getValue());
            m_object.setScale(jSlider1.getValue());
        }
    }

    class spinnerChangeListener implements ChangeListener
    {

        public void stateChanged(ChangeEvent e)
        {
            if (Float.valueOf(jSpinner1.getValue().toString()).intValue() > 100)
            {
                jSpinner1.setValue(100);
                jSlider1.setValue(100);
            }
            if (Float.valueOf(jSpinner1.getValue().toString()).intValue() < 1)
            {
                jSpinner1.setValue(1);
                jSlider1.setValue(1);
            }
            jSlider1.setValue(Integer.valueOf(jSpinner1.getValue().toString()).intValue());
            m_object.setScale(jSlider1.getValue());
        }
    }
}
