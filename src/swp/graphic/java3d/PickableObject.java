package swp.graphic.java3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;
import swp.graphic.IPickable;

public abstract class PickableObject extends BranchGroup implements IPickable
{

    private TransformGroup transGroup;
    private String m_name;

    /**
     * Der Konstruktor des PickableObject erzeugt eine TransformGroup, die
     * nachflgend mit getTransGroup() abgefragt werden kann. Des weiteren setzt
     * er notwendige Capabilities dieser TrasformGroup und von sich selbst.
     * Das Anhängen der BranchGroup und der TransformGroup muss in den
     * abgeleiteten Klassen geschehen!
     */
    public PickableObject()
    {
        this.setCapability(ALLOW_CHILDREN_EXTEND);
        this.setCapability(ALLOW_CHILDREN_READ);
        this.setCapability(ALLOW_CHILDREN_WRITE);
        //this.setCapability(ALLOW_PARENT_READ);
        this.setCapability(ALLOW_DETACH);
        this.setCapability(ENABLE_PICK_REPORTING);


        transGroup = new TransformGroup();
        transGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        transGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
        transGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
//        transGroup.setCapability(TransformGroup.ALLOW_PARENT_READ);
        transGroup.setCapability(ALLOW_DETACH);


        /* In den abgeleiteten Klassen am Ende des Konstruktors ausführen:
        this.addChild(getTransGroup());
        parent.addChild(this);
         * */

    }

    /**
     * Liefert die TransformGroup, die automatisch mit diesem Objekt angelegt wird
     * @return die TransformGroup
     */
    public TransformGroup getTransGroup()
    {
        return transGroup;
    }

    /**
     * Löscht dieses Objekt aus dem Szenengraphen
     */
    public void delete()
    {
        this.detach();
    }

    /**
     * Liefert die aktuelle Position des Objektes als Vector.
     * @return Die aktuelle Position
     */
    public Vector3d getPosition()
    {
        Transform3D trans = new Transform3D();
        Vector3d vec = new Vector3d();
        transGroup.getTransform(trans);
        trans.get(vec);
        return vec;
    }

    /**
     * Setzt das Objekt auf die angegebene Position.
     * @param newPosition Die neue Position
     */
    public void setPosition(Vector3d newPosition)
    {
        Transform3D trans = new Transform3D();
        transGroup.getTransform(trans);
        trans.setTranslation(newPosition);
        transGroup.setTransform(trans);
        //InflexPoint.updateLines((PickableObject) this);
    }

     public abstract Vector3d getAbsolutePosition();

    /**
     * Wird aufgerufen, sobald das Objekt angewählt (selektiert) wurde.
     */
    public abstract void select();


    /**
     * Wird aufgerufen, sobald das Objekt abgewählt wurde.
     */
    public abstract void unselect();

   
    public void update()
    {
    }
}
