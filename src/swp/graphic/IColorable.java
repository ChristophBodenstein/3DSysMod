/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

import javax.vecmath.Color3f;

/**
 *
 * @author svenjager
 */
public interface IColorable
{

    public void setColor(Color3f color);
    public Color3f getColor();
    public void setCubeTransperancy(float trans);
}
