/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.model;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaClass;
import ptolemy.kernel.util.IllegalActionException;
import swp.ModelLoader;
import swp.SaveVisitor;
import swp.graphic.IGraphic;
import swp.graphic.IGraphicElement;
import swp.sim.SimulatorVisitor;

/**
 *
 * @author svenjager
 */
public abstract class ModelElement extends LazyDynaClass implements IModelElement  {

    //referenz auf das Anzeigeobjekt
    private IGraphicElement m_ref = null;

    private static int m_elementCounter = 0;
    
    protected int m_id = 0;
    private Model m_model = null;
    
    //protected Hashtable<String, String> m_paramater = new Hashtable<String, String>();
    //protected DynaBean m_bean = null;

    private Color3f m_color = new Color3f(1.0f,0.0f,0.0f);
    
    protected String m_uniqueName = null;
    
    protected LinkedList<ModelPort> m_modelPortList = new LinkedList<ModelPort>();

    protected DynaBean m_bean;
    protected Hashtable<String, Object> m_paramater = new Hashtable<String, Object>();


    public ModelElement()
    {
        m_elementCounter++;
        m_id = m_elementCounter;
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelElement " + m_id +" erzeugt!");
        
    }

    @Override
    public void setModelRef(Model model)
    {
        m_model = model;
    }

    @Override
    public void delete()
    {
        m_model.delete(this);
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelElement " + m_id + " gelöscht!");

    }

    @Override
    public String getUniqueName()
    {
        return m_uniqueName;
    }

    @Override
    public void setRef(IGraphic ref)
    {
        if(ref instanceof IGraphicElement)
            m_ref = (IGraphicElement) ref;
    }

    public void setDisplayText(String text)
    {
        m_ref.setText(text);
    }

    public String getDisplayText()
    {
        return m_ref.getText();
    }


    public abstract String getScenePath();
    public abstract String getImagePath();

    public Color3f getColor()
    {
        return m_color;
    }

    public void setColor(Color3f color)
    {
        m_color = color;
        if(m_ref!=null)
            m_ref.setColor(m_color);
    }
   
    @Override
    public void initElement()
    {
        this.add("uniqueName");
        m_paramater.put("uniqueName", m_uniqueName);
        
    }

    @Override
    public void visitBySimulator(SimulatorVisitor visitor) throws IllegalActionException
    {
        Iterator<ModelPort> iterator = m_modelPortList.iterator();
        while (iterator.hasNext())
        {
            ModelPort modelPort = iterator.next();
            modelPort.visitBySimulator(visitor);
        }
    }

    public void addPort(ModelPort newPort)
    {
        Iterator<ModelPort> iterator = m_modelPortList.iterator();

        while (iterator.hasNext())
        {
            ModelPort modelPort = iterator.next();
            if (modelPort.getName().equals(newPort.getName()))
            {
                //sobald ein port mit dem gleichen namen existiert wird das hinzufügen abgebrochen
                System.out.println("Port mit diesem Namen existiert bereits.");
                return;
            }
        }


        newPort.setModelElement(this);
        //erzeuge neuen Port im Anzeigemodell
        newPort.setRef(m_ref.addNewPort(newPort));
        m_modelPortList.add(newPort);
        
    }


    public void removePort(ModelPort port)
    {
        if(m_modelPortList.remove(port))
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelPort entfernt!");
    }
    
    @Override
    public void visitBySaver(SaveVisitor visitor)
    {
        visitor.addStringAttribute("id",String.valueOf(m_id));
        visitor.addStringAttribute("name",m_uniqueName);
        visitor.addStringAttribute("displaytext", this.getDisplayText());

        visitor.addPoint("pos",m_ref.getAbsolutePosition());
        visitor.addColor("color",m_ref.getColor());
        visitor.addParameter("scale", String.valueOf(m_ref.getScale()));

        Iterator keys = propertiesMap.keySet().iterator();

        while (keys.hasNext())
        {
            String parName = (String) keys.next();
            //lese die information des Beans aus
            if (m_bean != null)
            {
                if(parName.equals("uniqueName")) continue;
                visitor.addParameter(parName, (String) m_bean.get(parName));
            }

        }
        

        Iterator<ModelPort> iterator = m_modelPortList.iterator();
        while (iterator.hasNext())
        {
           visitor.startPort();
           ModelPort modPort = iterator.next();
           modPort.visitBySaver(visitor);
           visitor.endPort();
        }

    }

    @Override
    public void visitByLoader(ModelLoader loader)
    {
        m_id = loader.getElementID();
        if(m_elementCounter<m_id) m_elementCounter=m_id+1;
        m_uniqueName = loader.getElementName();

        m_ref.setPosition(loader.getPoint("pos"));
        m_ref.setColor(loader.getColor("color"));

        String scale = loader.getParameterValue("scale");
        if(!scale.isEmpty())
            m_ref.setScale(Integer.parseInt(scale));

        this.setDisplayText(loader.getStringAttribute("displaytext"));


        Iterator keys = propertiesMap.keySet().iterator();

        while (keys.hasNext())
        {
            String parName = (String) keys.next();
            //lese die information des Beans aus
            if (m_bean != null)
            {
                String expr = loader.getParameterValue(parName);
                if(!expr.isEmpty())
                    m_bean.set(parName,expr );
            }

        }
        
        //ports durchlaufen
        Iterator<ModelPort> it = m_modelPortList.iterator();
        while (it.hasNext())
        {
            ModelPort mport = it.next();
            mport.visitByLoader(loader);
        }
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
