/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import jsyntaxpane.DefaultSyntaxKit;
import swp.library.script.IScriptElement;
import swp.model.ModelElement;

/**
 *
 * @author svenjager
 */
public class SourceEditorFrame extends JFrame
{

    JEditorPane m_editorpane = null;
    ModelElement m_element = null;

    public SourceEditorFrame(ModelElement modelElement)
    {

        m_element = modelElement;
        this.setLayout(new BorderLayout());


        DefaultSyntaxKit.initKit();

        m_editorpane = new JEditorPane();
        JScrollPane sp = new JScrollPane(m_editorpane);

        this.add(sp);


        String scriptContent = "";

        if(IScriptElement.class.isInstance(m_element))
        {
            scriptContent = ((IScriptElement)m_element).getSource();
        }


        m_editorpane.setContentType("text/python");
        m_editorpane.setText(scriptContent);

        this.setSize(400, 500);
        
        this.setTitle("Quelltext von " + modelElement.getUniqueName());

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        this.addWindowListener(new WindowAdapter()
        {

            @Override
            public void windowClosing(WindowEvent e)
            {
                switch (JOptionPane.showConfirmDialog(null, "Wollen sie den Code übernehmen?"))
                {

                    case JOptionPane.YES_OPTION:
                    {
                        if(IScriptElement.class.isInstance(m_element))
                        {
                            ((IScriptElement)m_element).setSource(m_editorpane.getText());
                        }

                        e.getWindow().setVisible(false);
                        e.getWindow().dispose();

                    }
                    case JOptionPane.NO_OPTION:
                        e.getWindow().setVisible(false);
                        e.getWindow().dispose();
                    case JOptionPane.CANCEL_OPTION:
                }

            }
        });
    }
}
