/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.gui;

import java.awt.Color;
import java.util.Hashtable;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import swp.model.IModelElement;
import swp.model.ModelPort;

/**
 *
 * @author svenjager
 */
public class ErrorView extends JTable {

    Hashtable<IModelElement, Exception> m_errorList = null;
    /**
     * This constructor method specifies what data the table will display (the
     * table model) and uses the TableColumnModel to customize the way that the
     * table displays it. The hard work is done by the TableModel implementation
     * below.
     */
    public ErrorView()
    {
        super();

        // Set the data model for this table
        this.setGridColor(Color.LIGHT_GRAY);
        this.setRowSelectionAllowed(true);
        this.setCellSelectionEnabled(false);
        this.setRowSelectionAllowed(true);

    }

    public void setActiveModelElement( Hashtable<IModelElement, Exception> elm)
    {

        setModel(new ErrorTableModel(elm));
        m_errorList = elm;
        // Tweak the appearance of the table by manipulating its column model
        TableColumnModel colmodel = getColumnModel();

        // Set column widths
        colmodel.getColumn(0).setPreferredWidth(10);
        colmodel.getColumn(1).setPreferredWidth(10);
        colmodel.getColumn(2).setPreferredWidth(1000);

        this.setRowHeight(20);
    }
    





    /**
     * This class implements TableModel and represents JavaBeans property data
     * in a way that the JTable component can display. If you've got some type
     * of tabular data to display, implement a TableModel class to describe that
     * data, and the JTable component will be able to display it.
     */
    static class ErrorTableModel extends AbstractTableModel
    {

        Hashtable<IModelElement, Exception> m_errorList = null;
        
        /**
         * The constructor: use the JavaBeans introspector mechanism to get
         * information about all the properties of a bean. Once we've got this
         * information, the other methods will interpret it for JTable.
         */
        public ErrorTableModel(Hashtable<IModelElement, Exception> errorList)
        {
            
                m_errorList = errorList;

        }
        // These are the names of the columns represented by this TableModel
        static final String[] columnNames = new String[]
        {
            "Element", "Fehler","Nachricht"
        };
        // These are the types of the columns represented by this TableModel
        static final Class[] columnTypes = new Class[]
        {
            String.class,
            String.class,
            String.class
        };

        // These simple methods return basic information about the table
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }

        @Override
        public int getRowCount()
        {
            if(m_errorList==null) return 0;
            return m_errorList.size();
        }

        @Override
        public String getColumnName(int column)
        {
            return columnNames[column];
        }

        @Override
        public Class getColumnClass(int column)
        {
            return columnTypes[column];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex)
        {
            return false;

        }

        /**
         * This method returns the value that appears at the specified row and
         * column of the table
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            if(m_errorList==null) return null;

                IModelElement element = (IModelElement) m_errorList.keySet().toArray()[row];

                if(element==null) return null;

                Exception ex = m_errorList.get(element);

                switch (column)
                {
                    case 0:
                        if(ModelPort.class.isInstance(element))
                        {

                            return ((ModelPort)element).getParent().getUniqueName();
                        }
                        else
                        {
                             return element.getUniqueName();
                        }
                    case 1:
                        return ex.getClass().getSimpleName();
                    case 2:
                        return ex.getMessage();

                }
            
                return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
          
        }
    }
}
