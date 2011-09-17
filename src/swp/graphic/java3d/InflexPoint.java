package swp.graphic.java3d;

import com.sun.j3d.utils.geometry.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import swp.graphic.IGraphicInflexPoint;
import swp.model.IModelElement;

/*
 * authors: Martin & David
 */
public class InflexPoint extends PickableObject implements IGraphicInflexPoint
{

    Sphere point;
    // Referenzen für das jeweils nächste und vorherige Objekt
    private PickableObject nextNode = null;
    private PickableObject prevNode = null;
    // Referenzen für die jeweils nächste und vorherige Linie
    private Lines nextLine = null;
    private Lines prevLine = null;
    private Vector3d position;
    private Connection m_connection = null;

    /**
     * Konstruktor. Erzeugt einen neuen Knickpunkt und hängt diesen an parent an.
     *
     * @param position - die Position des neuen InflexPoints
     * @param prevNode - der Vorgängerknoten
     * @param nextNode - der Nachfolgeknoten
     * @param parent - die Branchgroup, an die der InflexPoint angehangen wird
     */
    public InflexPoint(Vector3d position, PickableObject prevNode,
            PickableObject nextNode, Connection parent)
    {
        super();
        m_connection = parent;

        //this.setName("InflexPoint");
        this.nextNode = nextNode;
        this.prevNode = prevNode;
        this.position = position;
        this.setPosition(position);

        // Das Aussehen des Knickpunktes festlegen
        Material matPoint = new Material();
        matPoint.setLightingEnable(true);
        matPoint.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

        Appearance appPoint = new Appearance();
        appPoint.setMaterial(matPoint);
        appPoint.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

        // eine kleine Kugel erzeugen
        point = new Sphere(/*Radius*/0.1f,
                /*diverse Optionen*/ Primitive.GENERATE_TEXTURE_COORDS +
                Primitive.GENERATE_NORMALS +
                Primitive.ENABLE_GEOMETRY_PICKING,
                /*Apperance der Kugel*/ appPoint);

        // Elemente zum Szenengraph hinzufügen
        getTransGroup().addChild(point);
        this.addChild(getTransGroup());
    } // Konstruktor ----------------------------------------------------------

    /**
     * Hier wird eine neue Verbindung mit einem Inflexpoint erzeugt.Da beim
     * Erzeugen des InflexPoints PickableObject.setPosition(...) aufgerufen wird
     * und diese Methode wiederum InflexPoint.updateLines(...) aufruft und dort
     * alle Referenzen aktualisiert werden, müssen hier nur die Referenzen der
     * Linien und die des InflexPoint gesetzt werden.
     * Auch die Linien werden dort an die Branchgroup gehängt deshalb geschieht
     * dies nicht im Konstruktor von InflexPoints.
     *
     * @param ob1 - der erste Port der Verbindung
     * @param ob2 - der zweite Port der Verbindung
     * @param parent - die Elternbranchgroup
     */
    public static InflexPoint createLine(Port ob1, Port ob2, Connection parent)
    {

        Vector3d vec1 = new Vector3d();
        Vector3d vec2 = new Vector3d();


        vec1 = ob2.getAbsolutePosition();
        vec2 = ob1.getAbsolutePosition();

        /*casten der Vector3d vec1 und vec2 kann erst nach den if-Abfragen
         *initialisiert werden, da sonst beide Vector3d 0*/
        Point3f point1 = new Point3f(vec1);
        Point3f point2 = new Point3f(vec2);
        Point3f mitte = new Point3f();

        //1. Schritt: Position des Knotenpunktes berechnen
        point2.sub(point1);
        point2.scale(0.5f);
        mitte.set(point1);
        mitte.add(point2);
        point2 = new Point3f(vec2);

        //2.Schritt: Knickpunkt erzeugen
        Vector3d mittedouble = new Vector3d(mitte);

        InflexPoint temp = null;
        // die Verbindung soll vom InputPort zum OutputPort gezeichnet werden

        if (ob1.getType() == Port.INPUT_PORT)
        {
            temp = new InflexPoint(mittedouble, ob1, ob2, parent);
        } else
        {
            temp = new InflexPoint(mittedouble, ob2, ob1, parent);
        }


        return temp;

    } // createLine -----------------------------------------------------------

    /**
     * Fügt einen neuen InflexPoint in eine bereits bestehende Verbindung ein.
     *
     * @param ipoint - der gepickte InflexPoint
     * @param parent - die Elternbranchgroup
     */
    public void addInflexPoint()
    {

        Vector3d vec1 = new Vector3d();
        Vector3d vec2 = new Vector3d();

        vec1 = this.getPosition();
        vec2 = this.getNextNode().getAbsolutePosition();

        /*casten der Vector3d vec1 und vec2 kann erst nach den if-Abfragen
        initialisiert werden, da sonst beide Vector3d 0*/
        Point3f point1 = new Point3f(vec1);
        Point3f point2 = new Point3f(vec2);
        Point3f mitte = new Point3f();


        //1. Schritt: Position des Knotenpunktes berechnen
        point2.sub(point1);
        point2.scale(0.5f);
        mitte.set(point1);
        mitte.add(point2);

        //2.Schritt: Knickpunkt erzeugen
        Vector3d mittedouble = new Vector3d(mitte);

        InflexPoint temp = new InflexPoint(mittedouble, this, this.getNextNode(), m_connection);

        
        if(!(this.getNextNode() instanceof Port))
        {
            ((InflexPoint)this.getNextNode()).setPrevNode(temp);
        }
        this.setNextNode(temp);

        m_connection.addInflexPoint(temp);

        if (nextLine != null)
        {
            nextLine.detach();
        }

        //neuzeichnen der linien veranlassen
        this.getPrevNode().update();
    } // addInflexPoint -------------------------------------------------------

    /**
     * Erzeugt die Linien zwischen den Inflexpoints.
     */
    @Override
    public void update()
    {
        if (prevNode == null)
        {
            return;
        }
        if (nextNode == null)
        {
            return;
        }

        if (prevLine != null)
        {
            prevLine.detach();
        }

        //die prevLine wird immer gezeichnet
        prevLine = new Lines(new Point3f(prevNode.getAbsolutePosition()), new Point3f(this.getPosition()));
        this.addChild(prevLine);

        if (nextLine != null)
        {
            nextLine.detach();
        }

        //nextline nur beim letzten inflexpoint
        if (nextNode instanceof Port)
        {
            nextLine = new Lines(new Point3f(this.getPosition()), new Point3f(nextNode.getAbsolutePosition()));
            this.addChild(nextLine);
        }


        nextNode.update();
    }


    /* Methode delete
     * --> löscht den gewählten InflexPoint, wenn er nicht der letzte in einer
     * Verbindung sein sollte
     */
    @Override
    public void delete()
    {
        if (!(this.getPrevNode() instanceof Port))
        {
            ((InflexPoint) this.getPrevNode()).setNextNode(this.getNextNode());    
        }

        if (!(this.getNextNode() instanceof Port))
        {
            ((InflexPoint) this.getNextNode()).setPrevNode(this.getPrevNode());   
        }

        if (nextLine != null)
        {
            nextLine.detach();
        }
        if (prevLine != null)
        {
            prevLine.detach();
        }

        if (!(this.getPrevNode() instanceof Port))
        {
            this.getPrevNode().update();
        } else
        {
            this.getNextNode().update();
        }

        
        this.detach();
       
    } // delete -------------------------------------------------------------------

    /**
     * Setzt den Nachfolgeknoten.
     * @param nextNode
     */
    public void setNextNode(PickableObject nextNode)
    {
        this.nextNode = nextNode;
    }

    /**
     * Setzt den Vorgängerknoten.
     * @param prevNode
     */
    public void setPrevNode(PickableObject prevNode)
    {
        this.prevNode = prevNode;
    }

    /**
     * Gibt den Nachfolgeknoten zurück.
     * @return
     */
    public PickableObject getNextNode()
    {
        return this.nextNode;
    }

    /**
     * Gibt den Vorgängerknoten zurück.
     * @return
     */
    public PickableObject getPrevNode()
    {
        return this.prevNode;
    }

    @Override
    public void select()
    {
    }

    @Override
    public void unselect()
    {
    }

    /**
     * Gibt die aktuelle Position des InflexPoints zurück.
     * @param t3d
     * @return
     */
    @Override
    public Vector3d getPosition()
    {
        return this.position;
    }

    @Override
    public void setPosition(Vector3d newPosition)
    {
        super.setPosition(newPosition);

        position = newPosition;
        update();
    }

    @Override
    public Vector3d getAbsolutePosition()
    {
        return this.getPosition();
    }

    @Override
    public void setModelRef(IModelElement element)
    {
    }

    @Override
    public IModelElement getModelRef()
    {
        return null;
    }
} // class InflexPoint ########################################################

