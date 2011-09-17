/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

import java.util.LinkedList;
import swp.graphic.java3d.InflexPoint;

/**
 *
 * @author svenjager
 */
public interface IGraphicConnection extends IGraphic{

    public IGraphicPort getSecondPort();

    public IGraphicPort getFirstPort();

    public LinkedList<InflexPoint> getPointList();

    public void update(IGraphicPort port);

}
