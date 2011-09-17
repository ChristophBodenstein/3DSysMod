/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.model;

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.Node;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaClass;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import swp.SaveVisitor;
import swp.graphic.IGraphicElement;
import swp.graphic.IGraphicPort;
import swp.library.ptolemy.PtolemyElement;
import swp.graphic.java3d.SceneUniverse;
import swp.sim.SimulatorVisitor;

/**
 *
 * @author svenjager
 */
public class Model extends LazyDynaClass
{

    private LinkedList<IModelElement> m_modElemList = new LinkedList<IModelElement>();
    private SceneUniverse scene = null;
    private Hashtable<String, Object> m_paramater = new Hashtable<String, Object>();
    private DynaBean m_bean = null;


    public Model(SceneUniverse sceneBranchGroup)
    {
        scene = sceneBranchGroup;
        try
        {
            //Parameter für die Anzeige im PropertyEditor
            this.add("SimulationTime");
            this.add("Name");
            m_paramater.put("SimulationTime", "10.0");
            m_paramater.put("Name", "Neues Projekt");
            newInstance();
        } catch (IllegalAccessException ex)
        {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex)
        {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setTitle(String name) {
      m_paramater.put("Name", name);
    }

    public String getTitle() {
      return m_paramater.get("Name").toString();
    }

    synchronized public void addModelElement(IModelElement modElem)
    {
        if (!ModelElement.class.isInstance(modElem))
        {
            return;
        }

        ModelElement elem = (ModelElement) modElem;
        
        IGraphicElement newCube = scene.createGraphicElement(elem);
        elem.initElement();
        scene.getSceneBranchGroup().addChild((Node) newCube);
        m_modElemList.add(modElem);


        modElem.setModelRef(this);

        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelElement in das Modell eingefügt!");


    }

    public LinkedList<IModelElement> getModelElements()
    {
        return m_modElemList;
    }

    void delete(ModelElement modElem)
    {
        if (m_modElemList.remove(modElem))
        {
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelElement aus dem Modell entfernt!");
        }
    }

    public void visitBySimulator(SimulatorVisitor m_simVisitor) throws IllegalActionException, NameDuplicationException
    {

        //fügt Actoren in die simulation ein
        for (int i = 0; i < m_modElemList.size(); i++)
        {
            m_modElemList.get(i).visitBySimulator(m_simVisitor);
        }
  
        m_simVisitor.connectActors();

    }

    public void saveModel(SaveVisitor saveVisitor)
    {

        saveVisitor.startDocument();

        saveVisitor.addStringAttribute("name", (String) m_bean.get("Name"));
        saveVisitor.addStringAttribute("simulationtime", (String) m_bean.get("SimulationTime"));

        for (int i = 0; i < m_modElemList.size(); i++)
        {
            saveVisitor.startModelElement();
            m_modElemList.get(i).visitBySaver(saveVisitor);
            saveVisitor.endModelElement();
        }

        saveVisitor.endDocument();

        saveVisitor.write();
    }

    public ModelConnection addModelConnection(IGraphicPort port1, IGraphicPort port2)
    {
        return new ModelConnection(scene.createGraphicConnection(port1, port2));
    }

    @Override
    public String getName()
    {
        return this.getClass().getName();
    }

    @Override
    public DynaBean newInstance() throws IllegalAccessException, InstantiationException
    {
        try
        {
            if (m_bean == null)
            {
                m_bean = new BasicDynaBean(this);
                BeanUtils.populate(m_bean, m_paramater);
            }
            return m_bean;
        } catch (InvocationTargetException ex)
        {
            Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getSimulationTime()
    {
        return (String) m_bean.get("SimulationTime");
    }
}
