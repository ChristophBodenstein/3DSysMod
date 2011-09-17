/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.vecmath.Color3f;

/**
 *
 * @author svenjager
 */
public class ColorCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{

    final JColorChooser chooser = new JColorChooser();
    Color3f m_color = null;

    @Override
    public Object getCellEditorValue()
    {
        return m_color;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        JPanel pan = new JPanel();
        Color3f col = (Color3f) value;
        pan.setBackground(col.get());
        return pan;

    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        m_color = (Color3f) value;

        chooser.setColor(m_color.get());

        JPanel pan = new JPanel();
        pan.setBackground(m_color.get());

        pan.addMouseListener(new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent e)
            {
                JDialog chdlg = JColorChooser.createDialog(null, "Farbauswahl", true, chooser, new ActionListener()
                {

                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        m_color = new Color3f(chooser.getColor());
                        fireEditingStopped();
                    }
                }, null);
                chdlg.setVisible(true);


            }

            @Override
            public void mousePressed(MouseEvent e)
            {
                //throw new UnsupportedOperationException("Not supported yet.");
                }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                //throw new UnsupportedOperationException("Not supported yet.");
                }

            @Override
            public void mouseEntered(MouseEvent e)
            {
                //throw new UnsupportedOperationException("Not supported yet.");
                }

            @Override
            public void mouseExited(MouseEvent e)
            {
                //throw new UnsupportedOperationException("Not supported yet.");
                }
        });

        return pan;
    }
}
