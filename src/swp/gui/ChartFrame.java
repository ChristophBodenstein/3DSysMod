/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.gui;

//import com.sun.media.jai.rmi.RenderingHintsState;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.PaintSample;
import org.jfree.ui.StrokeChooserPanel;
import org.jfree.ui.StrokeSample;
import org.jfree.util.PaintUtilities;
import sun.security.acl.OwnerImpl;
import swp.library.Vis.IVisElement;
import swp.model.ModelElement;

/**
 *
 * @author svenjager
 */
public class ChartFrame extends JFrame
{

    private Hashtable<Integer, XYSeries> m_series = new Hashtable<Integer, XYSeries>();
    int counter = 0;

    public ChartFrame(ModelElement m_modelElement)
    {

        if(!IVisElement.class.isInstance(m_modelElement))
            return;

        IVisElement visElemet = (IVisElement)m_modelElement;
        
        this.setTitle("Ergebnisse von "+m_modelElement.getUniqueName());
        this.setPreferredSize(new Dimension(400, 300));
        this.setSize(this.getPreferredSize());
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        if (visElemet.hasResults())
        {
            
            visElemet.getPlotData(this);


            XYSeriesCollection xyDS = new XYSeriesCollection();
            Enumeration<XYSeries> elements = m_series.elements();
            while (elements.hasMoreElements())
            {
                xyDS.addSeries( elements.nextElement());

            }

            JFreeChart chart = ChartFactory.createXYLineChart(m_modelElement.getDisplayText(), "Time", null, xyDS, PlotOrientation.VERTICAL, true, true, true);
            ChartPanel pan = new ChartPanel(chart);

            chart.setAntiAlias(false);
            this.add(pan);
        }

    }

    public int createSerie(String name)
    {
        m_series.put(++counter, new XYSeries(name));
        return counter;
    }

    public void addXYitem(int serie ,double x, double y)
    {
        m_series.get(serie).add(x,y);
    }

}
