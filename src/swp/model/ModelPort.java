/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaClass;
import ptolemy.kernel.util.IllegalActionException;
import swp.ModelLoader;
import swp.SaveVisitor;
import swp.graphic.IGraphic;
import swp.graphic.IGraphicPort;
import swp.sim.SimulatorVisitor;

/**
 *
 * @author svenjager
 */
public class ModelPort extends LazyDynaClass implements IModelElement
{

    private String m_name = null;
    private boolean m_req = false;
    protected DynaBean m_bean =null;;
    protected Hashtable<String, Object> m_paramater = new Hashtable<String, Object>();

    public enum PortType
    {

        INPUT, OUTPUT
    };
    private static int m_elementCounter = 0;
    private int m_id = 0;
    private LinkedList<ModelConnection> m_connection = new LinkedList<ModelConnection>();
    private ModelElement m_modElement = null;
    private IGraphicPort m_ref = null;
    private PortType m_portType = null;
    private String m_uniqueName = null;

    public ModelElement getParent()
    {
        return m_modElement;
    }

    @Override
    public void setModelRef(Model model)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void initElement()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void visitBySaver(SaveVisitor visitor)
    {
        visitor.addStringAttribute("name", (String) m_bean.get("name"));
        visitor.addStringAttribute("id", String.valueOf(m_id));
        visitor.addStringAttribute("type", String.valueOf(m_portType));

        //Ref Connection gibt es sowohl im input als auch im output port!
        if (m_portType == PortType.INPUT)
        {
            if (m_connection != null)
            {
                Iterator<ModelConnection> iterator = m_connection.iterator();
                while (iterator.hasNext())
                {
                    iterator.next().visitBySaver(visitor);

                }
            }
        }

    }

    @Override
    public void visitByLoader(ModelLoader loader)
    {
        m_id = loader.getPortID(m_name);

        if (m_elementCounter < m_id)
        {
            m_elementCounter = m_id + 1;
        }

        m_uniqueName = new String("port_" + m_id);
        loader.addPortRef(m_uniqueName, m_ref);

    }

    public void removeConnection(ModelConnection connection)
    {
        m_connection.remove(connection);
    }

    public ModelPort(ModelElement modElement, ModelPort.PortType portType)
    {
        m_modElement = modElement;
        m_portType = portType;
        m_elementCounter++;
        m_id = m_elementCounter;
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelPort " + m_id + " erzeugt!");
        m_uniqueName = new String("port_" + m_id);
        m_name = m_uniqueName;


        try
        {
            m_paramater.put("uniqueName", m_uniqueName);
            m_paramater.put("name", m_name);
            //this.setRestricted(true);
            BeanUtils.populate(this.newInstance(), m_paramater);


        } catch (InvocationTargetException ex)
        {
            Logger.getLogger(ModelPort.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            Logger.getLogger(ModelPort.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            Logger.getLogger(ModelPort.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public PortType getPortType()
    {
        return m_portType;
    }

    @Override
    public String getUniqueName()
    {
        return m_uniqueName;
    }

    @Override
    public String getName()
    {
        return (String) m_bean.get("name");
    }

    public void addConnection(ModelConnection connection)
    {
        if (connection == null)
        {
            return;
        }
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Setze Connection " + m_id + "!");

        m_connection.add(connection);
    }

    public void setModelElement(ModelElement modElement)
    {
        m_modElement = modElement;
    }

    @Override
    public void delete()
    {
        m_modElement.removePort(this);
        if (m_connection != null)
        {
            Iterator<ModelConnection> iterator = m_connection.iterator();
            while (iterator.hasNext())
            {
                iterator.next().delete();

            }
        }
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelPort " + m_id + " gelöscht!");
    }

    @Override
    public void setRef(IGraphic ref)
    {
        if (ref instanceof IGraphicPort)
        {
            m_ref = (IGraphicPort) ref;
        }
    }

    public IGraphicPort getPort()
    {
        return m_ref;
    }

    public void setName(String name)
    {
        m_name = name;
        m_bean.set("name", name);
    }

    @Override
    public void visitBySimulator(SimulatorVisitor visitor) throws IllegalActionException
    {

        if (m_connection != null)
        {
            Iterator<ModelConnection> iterator = m_connection.iterator();
            while (iterator.hasNext())
            {
                iterator.next().visitBySimulator(visitor);

            }

            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelPort " + m_id + " bearbeitet!");
        }
    }

    public void setRequierd(boolean req)
    {
        m_req = req;
    }

    public boolean isRequiered()
    {
        return m_req;
    }

    @Override
    public DynaBean newInstance() throws IllegalAccessException, InstantiationException
    {
        if (m_bean == null)
        {
            m_bean = super.newInstance();
        }
        return m_bean;
    }
}
