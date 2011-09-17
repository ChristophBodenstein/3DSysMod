/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Vector3d;
import swp.graphic.IPositionable;

/**
 *
 * @author svenjager
 */
public class PositionDialog extends JFrame
{

    private float x, y, z;
    private int oldx, oldy, oldz;
    private JSpinner XNumberSpinner;
    private JSpinner YNumberSpinner;
    private JSpinner ZNumberSpinner;
    private JButton jOkButton = new JButton();
    private JButton jZuruecksetzenButton = new JButton();
    private PositionDialog posDialog = null;


    IPositionable m_object = null;


    public PositionDialog(IPositionable object)
    {
        super();

        m_object = object;

        posDialog = this;
        JLabel jLabel1 = new javax.swing.JLabel();
        JLabel jLabel2 = new javax.swing.JLabel();
        JLabel jLabel3 = new javax.swing.JLabel();
 
        Vector3d tempm3d = m_object.getAbsolutePosition();
                  
        oldx = (int) Math.round(tempm3d.x * 100);
        oldy = (int) Math.round(tempm3d.y * 100);
        oldz = (int) Math.round(tempm3d.z * 100);
        XNumberSpinner = new JSpinner(new SpinnerNumberModel((float) oldx / 100, -100, 100, 1));
        XNumberSpinner.addChangeListener(new PosChangeListener());
        YNumberSpinner = new JSpinner(new SpinnerNumberModel((float) oldy / 100, -100, 100, 1));
        YNumberSpinner.addChangeListener(new PosChangeListener());
        ZNumberSpinner = new JSpinner(new SpinnerNumberModel((float) oldz / 100, -100, 100, 1));
        ZNumberSpinner.addChangeListener(new PosChangeListener());
           
        this.setAlwaysOnTop(true);
        //this.setIconImage(new ImageIcon("src\\swp\\32x32_Wuerfel_gruen_transp.gif").getImage());
            
        this.setBounds(110, 97, 195, 135);
        this.setTitle("Position des Objektes");
        

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText(" X:");
        jLabel2.setText(" Y:");
        jLabel3.setText(" Z:");

        jOkButton.setText("OK");
        jOkButton.addActionListener(new OkButtonListener());
        
        jZuruecksetzenButton.setText("Zurücksetzen");
        jZuruecksetzenButton.addActionListener(new ZuruecksetzenButtonListener());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        
        getContentPane().setLayout(layout);

        GroupLayout.SequentialGroup seqhGroup = layout.createSequentialGroup();

        seqhGroup.addGroup(layout.createParallelGroup().addComponent(jLabel1).addComponent(jLabel2).addComponent(jLabel3).addComponent(jOkButton));
        seqhGroup.addGroup(layout.createParallelGroup().addComponent(XNumberSpinner).addComponent(YNumberSpinner).addComponent(ZNumberSpinner).addComponent(jZuruecksetzenButton));

        layout.setHorizontalGroup(seqhGroup);

        GroupLayout.SequentialGroup seqvGroup = layout.createSequentialGroup();

        seqvGroup.addGroup(layout.createParallelGroup().addComponent(jLabel1).addComponent(XNumberSpinner));
        seqvGroup.addGroup(layout.createParallelGroup().addComponent(jLabel2).addComponent(YNumberSpinner));
        seqvGroup.addGroup(layout.createParallelGroup().addComponent(jLabel3).addComponent(ZNumberSpinner));
        seqvGroup.addGroup(layout.createParallelGroup().addComponent(jOkButton).addComponent(jZuruecksetzenButton));
        layout.setVerticalGroup(seqvGroup);

        this.setResizable(false);
    }


    class OkButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            x = Float.valueOf(XNumberSpinner.getValue().toString()).floatValue();
            y = Float.valueOf(YNumberSpinner.getValue().toString()).floatValue();
            z = Float.valueOf(ZNumberSpinner.getValue().toString()).floatValue();

            m_object.setPosition(new Vector3d(x, y, z));
            posDialog.setVisible(false);
        }
    }

    class ZuruecksetzenButtonListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            x = (float) oldx / 100;
            y = (float) oldy / 100;
            z = (float) oldz / 100;
            XNumberSpinner.setValue(x);
            YNumberSpinner.setValue(y);
            ZNumberSpinner.setValue(z);

            m_object.setPosition(new Vector3d(x, y, z));
            
        }
    }

    class PosChangeListener implements ChangeListener
    {

        @Override
        public void stateChanged(ChangeEvent e)
        {
            x = Float.valueOf(XNumberSpinner.getValue().toString()).floatValue();
            y = Float.valueOf(YNumberSpinner.getValue().toString()).floatValue();
            z = Float.valueOf(ZNumberSpinner.getValue().toString()).floatValue();

            m_object.setPosition(new Vector3d(x, y, z));
        }
    }

}
