/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.graphic;

/**
 *
 * @author svenjager
 */
public interface IPickable
{

   /**
     * Wird aufgerufen, sobald das Objekt angewählt (selektiert) wurde.
     */
    public void select();

    /**
     * Wird aufgerufen, sobald das Objekt abgewählt wurde.
     */
    public void unselect();
}
