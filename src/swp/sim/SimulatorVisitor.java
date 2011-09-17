/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.sim;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.domains.de.kernel.DEActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.Workspace;
import swp.model.IModelElement;
import swp.model.ModelPort;

/**
 *
 * @author svenjager
 */
public class SimulatorVisitor
{

    TypedCompositeActor m_toplevel = null;
    Hashtable<String,ModelPort[]> m_connList = new Hashtable<String,ModelPort[]>();
    
    Hashtable<IModelElement, Exception> m_errorList = new Hashtable<IModelElement, Exception>();


    SimulatorVisitor(Workspace w)
    {
        m_toplevel = new TypedCompositeActor(w);

        StreamListener debugoutput = new StreamListener();
        m_toplevel.addDebugListener(debugoutput);

    }

    public TypedCompositeActor getTopLevelActor()
    {
        if (m_toplevel != null)
        {
            return m_toplevel;
        }

        return null;
    }

    public void addActor(DEActor actor)
    {
    }

    public void addConnection(String name,ModelPort port1, ModelPort port2)
    {
        
        if (m_connList.containsKey(name))
        {
            return;
        }
        
        m_connList.put(name, new ModelPort[]
                {
                    port1, port2
                });
    }

    public void connectActors()
    {

        Enumeration<String> keys = m_connList.keys();

        while (keys.hasMoreElements())
        {
            String name = keys.nextElement();
            ModelPort[] modelPorts = m_connList.get(name);

            ModelPort m_port1 = modelPorts[0];
            ModelPort m_port2 = modelPorts[1];

            List<Relation> linkedRelationList = this.getTopLevelActor().linkedRelationList();


            ComponentEntity ent1 = this.getTopLevelActor().getEntity(m_port1.getParent().getUniqueName());
            ComponentEntity ent2 = this.getTopLevelActor().getEntity(m_port2.getParent().getUniqueName());
            ComponentPort port1 = (ComponentPort) ent1.getPort(m_port1.getName());
            ComponentPort port2 = (ComponentPort) ent2.getPort(m_port2.getName());

            if (port1.linkedRelationList().size() > 0)
            {
                try
                {
                    Relation rel1 = (Relation) port1.linkedRelationList().get(0);
                    Relation rel = this.getTopLevelActor().newRelation(m_port1.getUniqueName() + "_" + m_port2.getUniqueName());
                    port2.link(rel);
                    rel.link(rel1);
                } catch (Exception ex)
                {
                    this.addError(m_port1, ex);
                    Logger.getLogger(SimulatorVisitor.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else
            {
                try
                {
                    Relation rel = this.getTopLevelActor().newRelation(m_port1.getUniqueName() + "_" + m_port2.getUniqueName());
                    port1.link(rel);
                    port2.link(rel);
                } catch (Exception ex)
                {
                    this.addError(m_port2, ex);
                    Logger.getLogger(SimulatorVisitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }


    }

    public int getErrorCount()
    {
        return m_errorList.size();
    }

    public void addError(IModelElement object,Exception ex)
    {
        m_errorList.put(object, ex);
    }

    public void showErrors()
    {
        Enumeration<IModelElement> keys = m_errorList.keys();

        while (keys.hasMoreElements())
        {
            IModelElement object = keys.nextElement();

            System.out.println(object.getUniqueName()+ " "+m_errorList.get(object).getClass().getSimpleName().toString() + " " +m_errorList.get(object).getMessage());


        }

    }

    Hashtable<IModelElement, Exception> getErrors()
    {
        return m_errorList;
    }

}
