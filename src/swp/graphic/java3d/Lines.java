package swp.graphic.java3d;

import java.awt.Color;

import javax.media.j3d.*;
import javax.vecmath.*;

/** Lines
 *
 * @author Martin & David
 */
public class Lines extends BranchGroup
{

    /**
     * Erstellt eine neue Linie mit node1 und node2 als Endpunkte
     *
     * @param node1 - erster Punkt der Linie
     * @param node2 - zweiter Punkt der Linie
     */
    public Lines(Point3f node1, Point3f node2)
    {

        this.setCapability(ALLOW_DETACH);
//		this.setCapability(ALLOW_PARENT_READ);
        

        Appearance app = new Appearance();
        LineAttributes attr = new LineAttributes();

        attr.setLineWidth(2f);
        attr.setLineAntialiasingEnable(true);
        app.setLineAttributes(attr);

        // das LineArray für die eigentliche Linie
        LineArray la = new LineArray(2, LineArray.COORDINATES | LineArray.COLOR_3);
        la.setColor(0, new Color3f(Color.BLACK));
        la.setColor(1, new Color3f(Color.BLACK));
        la.setCoordinate(0, node1);
        la.setCoordinate(1, node2);

        // das LineArray wird als Shape3D an dieses Objekt angehangen
        this.addChild(new Shape3D(la, app));
    } // Lines(...) -------------------------------------------------------

} // class Lines ##############################################################
