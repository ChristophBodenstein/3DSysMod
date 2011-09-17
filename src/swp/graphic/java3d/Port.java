package swp.graphic.java3d;

import com.sun.j3d.utils.geometry.*;
import java.util.Iterator;
import java.util.LinkedList;


import javax.media.j3d.*;
import javax.vecmath.*;
import swp.graphic.IGraphicConnection;
import swp.graphic.IGraphicPort;
import swp.model.IModelElement;
import swp.model.ModelPort;

public class Port extends PickableObject implements IGraphicPort
{

    public final static int INPUT_PORT = 0;
    public final static int OUTPUT_PORT = 1;
    private Cube parent;
    private Box port; // Die am Ende sichtbare Box des Ports
    private int type; // Der Typ des Ports (INPUT_PORT oder OUTPUT_PORT)
    private LinkedList<IGraphicConnection> m_connection = new LinkedList<IGraphicConnection>();
    
    private ModelPort m_modelPort = null;

    /**
     * Konstruktor zum Erzeugen eines neuen Ports. Der Parent des Ports ist dabei
     * stets die TransformGroup des übergeordneten Würfels, der Port fügt sich
     * selbstständig als Kind an diese an.
     * @param parent Die TransformGroup des dazugehörigen Würfels
     * @param position Die Position des Ports _relativ_ zum Mittelpunkt des
     * übergeordneten Würfels
     * @param type Der Typ des Ports, 0 für InputPort, 1 für OutputPort
     */
    public Port(Cube parent, Vector3d position, int type)
    {
        super();

        this.parent = parent;
        this.type = type;
        this.setName((type == INPUT_PORT) ? "inputPort" : "outputPort");

        // Schonmal die übergebene Position für den Port setzen
        this.setPosition(position);

        // Das Aussehen des Ports festlegen
        Material matPort = createMaterial(type);
        matPort.setLightingEnable(true);
        matPort.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

        Appearance appPort = new Appearance();
        appPort.setMaterial(matPort);
        appPort.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

        port = new Box(0.1f, 0.1f, 0.1f,
                Primitive.GENERATE_TEXTURE_COORDS + Primitive.GENERATE_NORMALS,
                appPort);
        port.getShape(Box.BACK).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        port.getShape(Box.FRONT).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        port.getShape(Box.TOP).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        port.getShape(Box.BOTTOM).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        port.getShape(Box.LEFT).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        port.getShape(Box.RIGHT).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);

        // Elemente zum Szenengraphen hinzufügen
        this.getTransGroup().addChild(port);
        this.addChild(this.getTransGroup());
        parent.getTransGroup().addChild(this);

       // Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Port erzeugt!");
    }

    public Port()
    {
    }

    public String getCubeName() {
      return parent.getText();
    }

    public String getTypeName() {
      return (type == 1)?"Output":"Input";
    }

    /**
     * Erzeugt und initialisiert ein Material-Objekt mit der passenden Farbe
     * @param type Der Typ des Ports, 0 für InputPort, 1 für OutputPort
     * @return Das erzeugte Material-Objekt
     */
    private Material createMaterial(int type)
    {
        Color3f white = new Color3f(1, 1, 1);
        Color3f black = new Color3f(0, 0, 0);

        Color3f color;
        if (type == INPUT_PORT)
        {
            color = new Color3f(1, 1, 0); // Gelb
        } else
        {
            color = new Color3f(0, 0, 0); // Schwarz
        }

        return new Material(
                black, // Ambient Color
                color, // Emessive Color
                white, // Diffuse Color
                white, // Specular Color
                64 // Shininess
                );
    }

    /**
     * Liefert den Typ des Ports, der mit INPUT_PORT bzw. OUTPUT_PORT überprüft werden kann.
     * @return Der Typ des Ports
     */
    @Override
    public int getType()
    {
        return this.type;
    }

    /**
     * Liefert den zugehörigen Würfel des Ports
     * @return
     */
    @Override
    public Cube getCube()
    {
        return this.parent;
    }

    /**
     * Liefert die absolute Position des Ports im Raum
     * @return
     */
    @Override
    public Vector3d getAbsolutePosition()
    {
        Vector3d vec = this.getPosition();
        vec.scale(1 + ((parent.getScale() - 1) / 10.0));
        vec.add(parent.getPosition());
        return vec;
    }

    @Override
    public void select()
    {
        // TODO: Ändern des Aussehens hat keine Wirkung
        Appearance app = port.getAppearance();
        Material mat = app.getMaterial();
        mat.setShininess(10);
        app.setMaterial(mat);
        port.setAppearance(app);
//    port.getAppearance().getMaterial().setShininess(10);
    }

    @Override
    public void unselect()
    {
        // TODO: Ändern des Aussehens hat keine Wirkung
        port.getAppearance().getMaterial().setShininess(64);
    }
   

    @Override
    public void addConnection(IGraphicConnection connection)
    {
        m_connection.add(connection);
    }

    @Override
    public void delete()
    {
        if (m_connection != null)
        {
            while(!m_connection.isEmpty())
                m_connection.getFirst().delete();

        }

        if(m_modelPort!=null)
        {
            m_modelPort.delete();
            m_modelPort = null;
        }
        this.detach();
    }

    public void onPositionChanged()
    {
        if (m_connection != null)
        {
            Iterator<IGraphicConnection> iterator = m_connection.iterator();
            while (iterator.hasNext())
            {
                IGraphicConnection connection = iterator.next();
                connection.update(this);
            }
        }
    }

    void removeConnection(Connection connection)
    {
        m_connection.remove(connection);
    }

    @Override
    public void setModelRef(IModelElement element)
    {
        if (ModelPort.class.isInstance(element))
        {
            m_modelPort = (ModelPort) element;
        }
    }

    @Override
    public IModelElement getModelRef()
    {
        return m_modelPort;
    }
}
