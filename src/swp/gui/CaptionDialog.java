/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import swp.graphic.INamable;

/**
 *
 * @author svenjager
 */
public class CaptionDialog extends JFrame
{

    String name;
    JOptionPane pane = new JOptionPane();
    JTextField text = new JTextField();
    JButton okButton = new JButton();
    CaptionDialog m_instance = null;
    INamable m_object = null;

    public CaptionDialog(INamable object)
    {
        super();
        m_object = object;
        m_instance = this;

        okButton.setText("OK");
        okButton.addActionListener(new okButtonListener());
        this.setTitle("Beschriftung des Würfels");
        this.setBounds(110, 97, 250, 50);
        this.setAlwaysOnTop(true);
        this.setResizable(false);

        Border border1 = BorderFactory.createEtchedBorder(1);
        text.setBorder(border1);
        text.setText(m_object.getText());
        text.getDocument().addDocumentListener(new IListener());
        this.add(text, BorderLayout.CENTER);
        this.add(okButton, BorderLayout.EAST);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    class okButtonListener implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            m_instance.setVisible(false);
        }
    }

    class IListener implements DocumentListener
    {

        public void insertUpdate(DocumentEvent e)
        {
            name = String.valueOf(text.getText());
            m_object.setText(name);
        }

        public void removeUpdate(DocumentEvent e)
        {
            name = String.valueOf(text.getText());
            m_object.setText(name);
        }

        public void changedUpdate(DocumentEvent e)
        {
        }
    }
}
