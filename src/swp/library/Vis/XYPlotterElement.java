/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library.Vis;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import swp.gui.ChartFrame;
import swp.ModelLoader;
import swp.SaveVisitor;
import swp.model.ModelElement;
import swp.model.ModelPort;
import swp.sim.SimulatorVisitor;
import swp.sim.Simulator;

/**
 *
 * @author svenjager
 */
public class XYPlotterElement extends ModelElement implements IVisElement {

    
    private boolean m_hasResult = false;

    private Hashtable<Integer, Hashtable<Double, Token>> m_history = null;

    public XYPlotterElement()
    {
        super();
        m_uniqueName = new String("xyplotter_" + m_id);
        this.setColor(new Color3f(0.0f,0.0f,1.0f));
    }

    @Override
    public String getScenePath()
    {
        return null;
    }

    @Override
    public String getImagePath()
    {
        return null;
    }

    @Override
    public void fetchResults(Simulator simulator)
    {
        XYPlotterActor plotter = (XYPlotterActor) simulator.getSimulationEntity(m_uniqueName);

        m_history = plotter.getHistory();

        m_hasResult = true;
    }

    @Override
    public void getPlotData(ChartFrame chartFrame)
    {
        if(!m_history.isEmpty())
        {
            Enumeration<Integer> series = m_history.keys();
            while (series.hasMoreElements())
            {
                int serie = series.nextElement();

                int serienumber = chartFrame.createSerie("Serie " + serie);

                Hashtable<Double, Token> serieHT = m_history.get(serie);

                Enumeration<Double> ser = serieHT.keys();
                while (ser.hasMoreElements())
                {
                    double time = ser.nextElement();
                    
                    Token tok = serieHT.get(time);

                    if (tok.getType() == BaseType.DOUBLE)
                    {
                        double value = ((DoubleToken) tok).doubleValue();
                        chartFrame.addXYitem(serienumber,time,value );
                        continue;
                    }

                    if (tok.getType() == BaseType.INT)
                    {
                        double value = ((IntToken) tok).doubleValue();
                        chartFrame.addXYitem(serienumber,time, value);
                        continue;
                    }

                    System.out.println(tok.getType().toString());
                }
            }
        }
    }

    @Override
    public boolean hasResults()
    {
        return m_hasResult;
    }

    @Override
    public void initElement()
    {
        ModelPort inputPort = new ModelPort(this, ModelPort.PortType.INPUT);
        inputPort.setName("input");
        inputPort.setRequierd(true);

        this.addPort(inputPort);
    }

    @Override
    public void visitBySimulator(SimulatorVisitor visitor) throws IllegalActionException
    {
        try
        {
            m_hasResult = false;
            XYPlotterActor plotter = new XYPlotterActor(visitor.getTopLevelActor(), m_uniqueName);


        } catch (NameDuplicationException ex)
        {
            Logger.getLogger(XYPlotterElement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalActionException ex)
        {
            Logger.getLogger(XYPlotterElement.class.getName()).log(Level.SEVERE, null, ex);
        }
        super.visitBySimulator(visitor);
    }

    @Override
    public void visitBySaver(SaveVisitor visitor)
    {
        super.visitBySaver(visitor);
        
        visitor.addStringAttribute("creator", VisLibrary.class.getName());
        visitor.addStringAttribute("classname", this.getClass().getName());

    }

    @Override
    public void visitByLoader(ModelLoader loader)
    {
       super.visitByLoader(loader);
    }

    @Override
    public String getName()
    {
        return this.getClass().getName();
    }

}
