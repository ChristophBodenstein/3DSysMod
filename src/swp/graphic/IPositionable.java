/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

import javax.vecmath.Vector3d;

/**
 *
 * @author svenjager
 */
public interface IPositionable {

    Vector3d getAbsolutePosition();
    void setPosition(Vector3d point);
    Vector3d getPosition();

}
