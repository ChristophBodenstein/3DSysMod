/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.model;

import swp.SaveVisitor;
import ptolemy.kernel.util.IllegalActionException;
import swp.ModelLoader;
import swp.graphic.IGraphic;
import swp.sim.SimulatorVisitor;

/**
 *
 * @author svenjager
 */
public interface IModelElement
{

    void setRef(IGraphic ref);
    void setModelRef(Model model);
    void delete();
    void initElement();
    void visitBySimulator(SimulatorVisitor visitor) throws IllegalActionException;
    void visitBySaver(SaveVisitor visitor);
    void visitByLoader(ModelLoader loader);

    String getUniqueName();
   
}
