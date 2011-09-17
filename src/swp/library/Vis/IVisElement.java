/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library.Vis;

import swp.gui.ChartFrame;
import swp.sim.Simulator;

/**
 *
 * @author svenjager
 */
public interface IVisElement
{

    void fetchResults(Simulator simulator);

    boolean hasResults();


    void getPlotData(ChartFrame chartFrame);
}
