/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

import swp.model.IModelElement;

/**
 *
 * @author svenjager
 */
public interface IGraphic {

    void setModelRef(IModelElement element);
    IModelElement getModelRef();
    void delete();
}
