/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Vector3d;
import swp.ModelLoader;
import swp.SaveVisitor;
import swp.graphic.IGraphic;
import swp.graphic.IGraphicConnection;
import swp.graphic.java3d.InflexPoint;
import swp.sim.SimulatorVisitor;

/**
 *
 * @author svenjager
 */
public class ModelConnection implements IModelElement
{

    private static int m_elementCounter = 0;
    private int m_id = 0;
    private ModelPort m_port1 = null;
    private ModelPort m_port2 = null;
    private IGraphicConnection m_ref = null;

    public ModelConnection(IGraphicConnection connection)
    {
        m_elementCounter++;
        m_id = m_elementCounter;
        m_ref = connection;
        m_port1 = (ModelPort) m_ref.getFirstPort().getModelRef();
        m_port2 = (ModelPort) m_ref.getSecondPort().getModelRef();

        connection.setModelRef(this);

        if(m_port1.getPortType()==ModelPort.PortType.INPUT)
        {
            ModelPort tempPort = m_port2;
            m_port2 = m_port1;
            m_port1= tempPort;

        }

        m_port1.addConnection(this);
        m_port2.addConnection(this);


        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelConnection " + m_id + " erzeugt!");

    }


    @Override
    public void delete()
    {
        if(m_port1!=null)
            m_port1.removeConnection(this);

        if(m_port2!=null)
            m_port2.removeConnection(this);

        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "ModelConnection " + m_id + " gelöscht!");

    }

    @Override
    public void visitBySimulator(SimulatorVisitor visitor)
    {
            visitor.addConnection(getUniqueName(),m_port1, m_port2);
    }

    @Override
    public void setRef(IGraphic ref)
    {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public String getUniqueName()
    {
        return new String("connection_"+m_id);
    }

    @Override
    public void visitBySaver(SaveVisitor visitor)
    {
        visitor.startConnection();

        visitor.addStringAttribute("id", String.valueOf( m_id));


        visitor.addStringAttribute("port1", m_port1.getUniqueName());
        visitor.addStringAttribute("port2", m_port2.getUniqueName());

        LinkedList<InflexPoint> pList = m_ref.getPointList();
        Iterator<InflexPoint> iterator = pList.iterator();

        int i = 0;
        while (iterator.hasNext())
        {
            InflexPoint point = iterator.next();
            visitor.addPoint(String.valueOf(i), point.getAbsolutePosition());
            i++;
        }


        visitor.endConnection();
    }

    @Override
    public void visitByLoader(ModelLoader loader)
    {

        LinkedList<Vector3d> pList = loader.getPointList();

        

        //hole den Inflexpunkt der automatisch beim erstellen der connection erzeugt wurde
        if(m_ref.getPointList().size()>1)
        {
            Logger.getLogger(ModelConnection.class.getName()).log(Level.WARNING, "Es existieren mehrere Inflexpoints");
            return;
        }

        InflexPoint inflPoint = m_ref.getPointList().get(0);

        for(int i = 1; i<pList.size();i++)
        {
            inflPoint.addInflexPoint();
        }


        LinkedList<InflexPoint> pointList = m_ref.getPointList();
        
        for(int i = 0;i<pointList.size();i++)
        {
            pointList.get(i).setPosition(pList.get(i));
        }


        
    }
}
