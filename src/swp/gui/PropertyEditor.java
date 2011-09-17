/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

import java.awt.Color;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.vecmath.Color3f;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaClass;
import org.apache.commons.beanutils.PropertyUtils;
import swp.ColorCell;
import swp.NumberCell;
import swp.SelectionChangedListener;
import swp.SelectionEvent;
import swp.graphic.IGraphic;
import swp.model.Model;

/**
 *
 * @author svenjager
 */
public class PropertyEditor extends JTable implements SelectionChangedListener
{
    private Model m_model = null;

    /**
     * This constructor method specifies what data the table will display (the
     * table model) and uses the TableColumnModel to customize the way that the
     * table displays it. The hard work is done by the TableModel implementation
     * below.
     */
    public PropertyEditor()
    {
        super();
      
        // Set the data model for this table
        this.setGridColor(Color.LIGHT_GRAY);
        this.setRowSelectionAllowed(true);
        this.setCellSelectionEnabled(false);
        this.setCellSelectionEnabled(true);


    }

    public void setActiveModelElement(Object elm)
    {

        try
        {
            setModel(new JavaBeanPropertyTableModel(elm));
        } catch (IntrospectionException e)
        {
            System.err.println("WARNING: can't introspect: " + elm);
        }

        // Tweak the appearance of the table by manipulating its column model
        TableColumnModel colmodel = getColumnModel();

        // Set column widths
        colmodel.getColumn(0).setPreferredWidth(10);
        colmodel.getColumn(1).setPreferredWidth(10);

        this.setRowHeight(20);
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column)
    {
        JavaBeanPropertyTableModel model = (JavaBeanPropertyTableModel) getModel();


        Class cellClass = model.getCellClass(row, column);

        if (cellClass != null)
        {
            if (cellClass == Color3f.class)
            {
                return new ColorCell();
            }

            if (cellClass == int.class)
            {
                return new NumberCell();
            }
        }

        return super.getCellRenderer(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column)
    {
        JavaBeanPropertyTableModel model = (JavaBeanPropertyTableModel) getModel();

        Class cellClass = model.getCellClass(row, column);
        if (cellClass != null)
        {
            if (cellClass == Color3f.class)
            {
                return new ColorCell();
            }

            if (cellClass == int.class)
            {
                return new NumberCell();
            }

            //System.out.println(cellClass.toString());
        }


        return super.getCellEditor(row, column);
    }

    @Override
    public void selectionChanged(SelectionEvent e)
    {
        if(IGraphic.class.isInstance(e.getSelectedObject()))
        {
            IGraphic graph = (IGraphic) e.getSelectedObject();
            this.setActiveModelElement(graph.getModelRef());
        }else
        {
            this.setActiveModelElement(m_model);
        }
    }

    public void setModel(Model model)
    {
        m_model = model;
    }

    /**
     * This class implements TableModel and represents JavaBeans property data
     * in a way that the JTable component can display. If you've got some type
     * of tabular data to display, implement a TableModel class to describe that
     * data, and the JTable component will be able to display it.
     */
    static class JavaBeanPropertyTableModel extends AbstractTableModel
    {

        Object m_object = null;
        //LinkedList<PropertyDescriptor> m_props = null;
        Map properties = null;
        Object m_bean = null;

        /**
         * The constructor: use the JavaBeans introspector mechanism to get
         * information about all the properties of a bean. Once we've got this
         * information, the other methods will interpret it for JTable.
         */
        public JavaBeanPropertyTableModel(Object object)
                throws java.beans.IntrospectionException
        {
            try
            {
                m_object = object;
                properties = new HashMap();
                
                if (object == null)
                {
                    return;
                }
                m_bean = m_object;
                
                if (LazyDynaClass.class.isInstance(m_object))
                { 
                        DynaBean bean = ((LazyDynaClass) m_object).newInstance();
                        m_bean = bean;
                }

                
                
                properties = BeanUtils.describe(m_bean);

                
            } catch (InstantiationException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            }



        }
        // These are the names of the columns represented by this TableModel
        static final String[] columnNames = new String[]
        {
            "Name", "Value",
        };
        // These are the types of the columns represented by this TableModel
        static final Class[] columnTypes = new Class[]
        {
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
            return properties.size();
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
            if (columnIndex == 1)
            {
                String key = (String) properties.keySet().toArray()[rowIndex];
                if (PropertyUtils.isWriteable(m_bean, key))
                {
                    return true;
                }
            }
            return false;

        }

        /**
         * This method returns the value that appears at the specified row and
         * column of the table
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            try
            {
                String key = (String) properties.keySet().toArray()[row];

                switch (column)
                {
                    case 0:
                        return key;
                    case 1:
                    {
                    try
                    {

                        return PropertyUtils.getSimpleProperty(m_bean, key);
                    } catch (IllegalAccessException ex)
                    {
                        Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvocationTargetException ex)
                    {
                        Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (NoSuchMethodException ex)
                    {
                        Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                        //return properties.get(key);
                    } 
                    

                }
            } catch (IllegalArgumentException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;

        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            if (columnIndex == 0)
            {
                return;
            }

            String key = (String) properties.keySet().toArray()[rowIndex];
            
            try
            {
                
                PropertyUtils.setProperty(m_bean, key, aValue);

                //objekt sagen das Daten verändert wurden
                
                
            } catch (NoSuchMethodException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } 

        }

        private Class getCellClass(int row, int column)
        {
            try
            {
                if (column == 0)
                {
                    return null;
                }

                String key = (String) properties.keySet().toArray()[row];
                
                return PropertyUtils.getPropertyType(m_bean, key);

            } catch (IllegalAccessException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex)
            {
                Logger.getLogger(PropertyEditor.class.getName()).log(Level.SEVERE, null, ex);
            }

            return String.class;
        }
    }
}
