/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

import swp.graphic.java3d.Connection;

/**
 *
 * @author svenjager
 */
public interface IGraphicPort extends IGraphic{
    public IGraphicElement getCube();
    public int getType();

    public void addConnection(IGraphicConnection connection);
}
