/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp;

import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author svenjager
 */
public class NumberCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
{

    JSpinner m_spin = null;

    @Override
    public Object getCellEditorValue()
    {
        return m_spin.getValue();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        int number = (Integer) value;
        JLabel lab = new JLabel(String.valueOf(number),SwingConstants.RIGHT);
        return lab;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        int number = (Integer) value;

        m_spin = new JSpinner();
        m_spin.setValue(number);
        return m_spin;
    }
}
