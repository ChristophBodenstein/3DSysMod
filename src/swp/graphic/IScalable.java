/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

/**
 *
 * @author svenjager
 */
public interface IScalable {

    /**
     * liefert den Skalierungsfaktor eines Würfles
     * @return skalierungsfaktor des Würfels
     */
    int getScale();

    /**
     * setzt den Skalierungsfaktor eines Würfels
     * @param newscale neuer Skalierungsfaktor
     */
    void setScale(int newscale);

}
