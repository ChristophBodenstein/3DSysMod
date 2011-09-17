/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

import swp.model.ModelPort;

/**
 *
 * @author svenjager
 */
public interface IGraphicElement extends IGraphic , IColorable, IPositionable, IScalable, INamable{

    public IGraphicPort addNewPort(ModelPort port);
}
