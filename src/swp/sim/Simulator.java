/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.sim;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Workspace;
import swp.library.Vis.IVisElement;
import swp.model.IModelElement;
import swp.model.Model;

/**
 *
 * @author svenjager
 */
public class Simulator
{

    SimulatorVisitor m_simVisitor = null;
    Manager m_manager = null;
    DEDirector m_director = null;
    Model m_model = null;

    Token m_simulationSteps = null;


    public Simulator()
    {
        m_simulationSteps = new DoubleToken(10.0);
    }

    public boolean Init(Model model)
    {
        try
        {
            m_model = model;
            
            //initalisiert Ptolemy
            Workspace w = new Workspace("w");
            m_simVisitor = new SimulatorVisitor(w);

            //baut die Simulation auf
            model.visitBySimulator(m_simVisitor);

            TypedCompositeActor toplevel = m_simVisitor.getTopLevelActor();
            
            m_director = new DEDirector(toplevel, "director");
            m_manager = new Manager(w, "manager");
            toplevel.setManager(m_manager);


            //Elemente in der Simulation anzeigen
            List<Entity> entList =  m_simVisitor.getTopLevelActor().entityList();

            for( int i = 0 ; i < entList.size();i++)
            {
               System.out.println( entList.get(i).getName());

            }
            List<Relation> relList  =    m_simVisitor.getTopLevelActor().relationList();

            for( int i = 0 ; i < relList.size();i++)
            {
               System.out.println( relList.get(i).getName());

            }

            if(m_simVisitor.getErrorCount()>0) throw new KernelException();


            JOptionPane.showMessageDialog(null, "Simulator erfolgreich initalisiert.");
            return true;

        } catch (KernelException ex)
        {
            //m_simVisitor.showErrors();
            JOptionPane.showMessageDialog(null,"Es ist ein Fehler bei der Initalisierung aufgetreten!\n" +ex.getMessage(), "Simulator", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
    }

    public void run()
    {
        try
        {
            m_director.stopTime.setExpression(m_model.getSimulationTime());
            m_manager.execute();
            //writeback results
            Iterator<IModelElement> iterator = m_model.getModelElements().iterator();
            while (iterator.hasNext())
            {
                IModelElement modelElement = iterator.next();
                if (IVisElement.class.isInstance(modelElement))
                {
                    ((IVisElement) modelElement).fetchResults(this);
                }
            }
            JOptionPane.showMessageDialog(null, "Simulation beendet.");

        } catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null,"Es ist ein Fehler bei der Simulation aufgetreten!\n" +ex.getMessage(), "Simulator", JOptionPane.ERROR_MESSAGE);
            
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void step()
    {
        try
        {
            if(m_manager.getState() ==Manager.IDLE)
                m_manager.initialize();
            m_manager.iterate();
        } catch (KernelException ex)
        {
            Logger.getLogger(Simulator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public Object getSimulationEntity(String name)
    {
       return m_simVisitor.getTopLevelActor().getEntity(name);
    }


    public void delete()
    {
    }

    public Hashtable<IModelElement, Exception> getErrors()
    {
        return m_simVisitor.getErrors();
    }
    
}


