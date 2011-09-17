package swp.graphic.java3d;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;


import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.geometry.*;

import com.sun.j3d.utils.image.TextureLoader;
import java.awt.Font;

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.*;
import javax.swing.ImageIcon;
import javax.vecmath.*;
import swp.graphic.IGraphicElement;
import swp.model.IModelElement;
import swp.model.ModelElement;
import swp.model.ModelPort;

/**
 *
 * @author Timmä
 */
public class Cube extends PickableObject implements IGraphicElement
{

    private ModelElement m_elem = null;
    private Box cube;
    protected Appearance appCube = new Appearance();
    private Material matCube = new Material();
    private Color3f dColor = new Color3f(1.0f, 1.0f, 1.0f);
    private Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
    private Color3f aColor = new Color3f(0.0f, 0.0f, 0.0f);
    private Font f = new Font("Arial Narrow", Font.PLAIN, 1);
    private FontExtrusion z = new FontExtrusion();
    private Font3D d = new Font3D(f, z);
    private Point3f q = new Point3f(0, 1, 0);
    private Text3D cubeText = new Text3D(d, "", q, 0, 1);
    //private Point3f p = new Point3f(0, 0, 0);
    protected OrientedShape3D OS2;
    protected Shape3D shape2 = new Shape3D();
    protected Appearance app2 = new Appearance();
    private int scalefak;

    private LinkedList<Port> inputPort = new LinkedList<Port>();
    private LinkedList<Port> outputPort = new LinkedList<Port>();

    /**
     * Konstruktor erstellt einen neuen Würfel
     * @param label Beschriftung des Würfels
     * @param color farbe des Würfels
     * @param sign Zeichen des Würfels
     */
    public Cube(ModelElement elem)
    {
        super();


        m_elem = elem;

        //Referenz auf das korrespondierende Modell Element
        elem.setRef(this);

        scalefak = 1;
//        inputPort=0;
//        outputPort=0;

        app2.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        appCube.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        appCube.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
        matCube.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

        cube = new Box(1.0f, 1.0f, 1.0f, Primitive.GENERATE_TEXTURE_COORDS + Primitive.GENERATE_NORMALS + Primitive.ENABLE_GEOMETRY_PICKING, appCube);
        cube.getShape(Box.BACK).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        cube.getShape(Box.FRONT).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        cube.getShape(Box.TOP).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        cube.getShape(Box.BOTTOM).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        cube.getShape(Box.LEFT).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        cube.getShape(Box.RIGHT).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);


        //würfel bekommt eine eindeutige id
        this.setName("cube");

        this.setCubeTransperancy(0.3f);



        OS2 = new OrientedShape3D(shape2.getGeometry(), app2, OrientedShape3D.ROTATE_ABOUT_AXIS, new Vector3f(1, 0, 0));
        //OS2.setGeometry(shape2.getGeometry());
        OS2.setCapability(OrientedShape3D.ALLOW_GEOMETRY_WRITE);
        getTransGroup().addChild(OS2);
        //this.setCubeSign(sign);

        Appearance a = new Appearance();
        ColoringAttributes textColor = new ColoringAttributes();
        textColor.setColor(0, 0, 0);
        a.setColoringAttributes(textColor);
        a.setMaterial(new Material());
        OrientedShape3D OS = new OrientedShape3D(cubeText, a, OrientedShape3D.ROTATE_ABOUT_AXIS, new Vector3f(0, 1, 0));
        cubeText.setCapability(Text3D.ALLOW_STRING_WRITE);
        getTransGroup().addChild(OS);



        getTransGroup().addChild(cube);
        this.addChild(getTransGroup());


        //setze die übergebenen Eigenschaften
        this.loadCubeSign();
        this.setColor(m_elem.getColor());

         //Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Cube erzeugt!");
    }

    /**
     * liefert die farbe des Würfels
     * @return
     */
    @Override
    public Color3f getColor()
    {
        return aColor;
    }

    /**
     * setzt die Farbe des Würfels
     * @param newColor
     */
    @Override
    public void setColor(Color3f newColor)
    {
        aColor = newColor;
        matCube.setAmbientColor(eColor);
        matCube.setDiffuseColor(dColor);
        matCube.setEmissiveColor(aColor);
        matCube.setSpecularColor(dColor);
        matCube.setShininess(99.f);
        appCube.setMaterial(matCube);
        cube.setAppearance(appCube);
    }

    /**
     * setzt die Transparenz eines Würfels
     * @param trans Transparenz des Würfels
     */
    public void setCubeTransperancy(float trans)
    {
        TransparencyAttributes t_attr = new TransparencyAttributes(TransparencyAttributes.BLEND_ZERO, trans);
        appCube.setTransparencyAttributes(t_attr);
        cube.setAppearance(appCube);
    }

    /**
     * liefert die Beschriftung des Würfels
     * @return
     */
    @Override
    public String getText()
    {
        return cubeText.getString();
    }

    /**
     * setzt die Beschriftung eines Würfels
     * @param label Beschriftung des Würfels
     */
    @Override
    public void setText(String label)
    {
        cubeText.setString(label);
    }

    /**
     * Laed das Zeichen des Würfels
     */
    protected void loadCubeSign()
    {

        if (m_elem.getImagePath() != null)
        {
            ImageIcon image = new ImageIcon(m_elem.getImagePath());
            TextureLoader loader = new TextureLoader(image.getImage(), ImageComponent2D.FORMAT_RGBA, null);
            Texture tex = loader.getTexture();
            TextureAttributes ta = new TextureAttributes();
            ta.setTextureMode(TextureAttributes.COMBINE_ONE_MINUS_SRC_ALPHA);
            appCube.setTextureAttributes(ta);
            appCube.setTexture(tex);


        }

        if (m_elem.getScenePath() != null)
        {


            ObjectFile objFileloader = new ObjectFile();

            objFileloader.setFlags(-3);


            try
            {
                Scene scene = objFileloader.load(m_elem.getScenePath());
                scene.getSceneGroup().getChild(0).setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
                shape2 = (Shape3D) scene.getSceneGroup().getChild(0);

                Material mat = new Material(eColor, aColor, dColor, dColor, 64.f);
                app2.setMaterial(mat);
                shape2.setAppearance(app2);
                OS2.setGeometry(shape2.getGeometry());
            } catch (FileNotFoundException ex)
            {
                Logger.getLogger(Cube.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IncorrectFormatException ex)
            {
                Logger.getLogger(Cube.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParsingErrorException ex)
            {
                Logger.getLogger(Cube.class.getName()).log(Level.SEVERE, null, ex);
            }




        }

    }


    /**
     * liefert die Position des Würfels
     * @param t3d TransformGroup des Würfels
     */
    public void getPosition(Transform3D t3d)
    {
        getTransGroup().getTransform(t3d);
    }

    /**
     * setzt die Position eins Würfels
     * @param newPosition neue Position des Würfels
     */
    @Override
    public void setPosition(Vector3d newPosition)
    {
        super.setPosition(newPosition);
        // TODO: Adaptieren der Linien ermöglichen!
        //ports aufrufen linienupdate

        for(int i = 0; i<inputPort.size(); i++)
        {
            inputPort.get(i).onPositionChanged();
        }


        for(int i = 0; i<outputPort.size(); i++)
        {
            outputPort.get(i).onPositionChanged();
        }
    }

    /**
     * setzt die Position eines Würfels
     * @param x X koordinate des Würfels
     * @param y Y koordinate des Würfels
     * @param z Z koordinate des Würfels
     */
    public void setPosition(float x, float y, float z)
    {
        super.setPosition(new Vector3d(x, y, z));

    }

    /**
     * liefert den Skalierungsfaktor eines Würfles
     * @return skalierungsfaktor des Würfels
     */
    @Override
    public int getScale()
    {
        return scalefak;
    }

    /**
     * setzt den Skalierungsfaktor eines Würfels
     * @param newscale neuer Skalierungsfaktor
     */
    @Override
    public void setScale(int newscale)
    {
        Transform3D t3d = new Transform3D();
        getTransGroup().getTransform(t3d);
        t3d.setScale(1 + ((newscale - 1) / 10.0));
        getTransGroup().setTransform(t3d);
        scalefak = newscale;
    }

    /**
     * setz die Shininess eines würfels um zu signalisieren das er gewählt ist
     */
    @Override
    public void select()
    {
        matCube.setShininess(10);
        appCube.setMaterial(matCube);
        cube.setAppearance(appCube);
    }

    /**
     * löscht den Würfel und die daran hängenden Linien
     */
    @Override
    public void delete()
    {
        
        for (int i = 0; i < inputPort.size(); i++)
        {
            inputPort.get(i).delete();
        }

        for (int i = 0; i < outputPort.size(); i++)
        {
            outputPort.get(i).delete();
        }
        this.detach();

        //verknüpftes ModelElement löschen
        m_elem.delete();
    }

    /**
     * setz die Shininess eines würfels um zu signalisieren das er nicht gewählt ist
     */
    @Override
    public void unselect()
    {
        matCube.setShininess(64);
        appCube.setMaterial(matCube);
        cube.setAppearance(appCube);
    }

  


    @Override
    public Vector3d getAbsolutePosition()
    {
        return this.getPosition();
    }


    /**
     * Fügt einen neuen Port an den Cube an
     * @param port
     * @return
     */
    
    @Override
    public Port addNewPort(ModelPort port)
    {
        if(port.getPortType() == ModelPort.PortType.INPUT)
        {
            Port newPort = new Port(this, new Vector3d(0,0,0), Port.INPUT_PORT);
            newPort.setModelRef(port);
            inputPort.add(newPort);


            rearrangePorts(inputPort,Port.INPUT_PORT);
            return newPort;
        }

        if(port.getPortType() == ModelPort.PortType.OUTPUT)
        {
            Port newPort = new Port(this, new Vector3d(0,0,0), Port.OUTPUT_PORT);
            newPort.setModelRef(port);
            outputPort.add(newPort);


            rearrangePorts(outputPort,Port.OUTPUT_PORT);
            return newPort;
        }
        return null;
    }



    private void rearrangePorts(LinkedList<Port> portarray, int portType)
    {

        float currentx = 0;
        float currenty = 0;
        float currentz = 0;

        float x = 0f;
        float y = 0f;
        float z = 0f;
        float zeilenabstand = 0.35f;
        float zeile = 0f;
        float abst = 0.2f;
        float portsize = 0.2f;
        float platz = 0f;
        boolean fehler = true;

        int i, j = 0;
        int zz = 1;
        int a = 1, b = 1;

        // X ist stets unabhängig von allem, außer dem Porttyp
        if (portType == Port.INPUT_PORT)
        {
            x = currentx - 1;
        } else
        {
            x = currentx + 1;
        }

        for (i = 1; i <= portarray.size(); i++)
        {
            //Überprüfung ob noch Ports auf den Würfel passen
            if (((portsize * j) + (abst * (j + 1))) * ((portsize * zz) +
                    (zeilenabstand * (zz + 1))) < 4)
            {
                // ((dimy/skalfak*2)*(dimz/skalfak*2)) ergibt stets 4
                j++;
                //Überprüfung des mittig sitzenden Ports
                if (j > 1)
                {
//                    if (dimz/skalfak%2>0){
//                        platz=dimz/skalfak*2+0.21f;
//                    }else{
//                        platz=dimz/skalfak*2+0.2f;
//                    }
                    platz = 2.21f; // Nur der Then Fall wird erreicht, dimz==skalfak

                    if ((abst * j + portsize * j + abst) >= platz)
                    {
                        //Zeile voll?
                        j = 1;
                        if (b > 0)
                        {
                            //nach oben
                            zeile = (float) Math.abs(zeile) + zeilenabstand;
                            zz++;
                            b = b * (-1);
                            z = (float) currentz;
                            y = (float) currenty + zeile;
//                            x = (float) currentx + 1; // dimx/skalfak == 1
                            a = 1;
                        } else
                        {
                            //nach unten
                            zeile = (float) zeile * (-1);
                            zz++;
                            b = b * (-1);
                            z = (float) currentz;
                            y = (float) currenty + zeile;
//                            x = (float) currentx + 1; // dimx/skalfak == 1
                            a = 1;
                        } //else
                    } else
                    {
                        if (a > 0)
                        {
                            //nach rechts
                            z = (float) currentz + (abst * j);
                            y = (float) currenty + zeile;
//                            x = (float) currentx + 1; // dimx/skalfak == 1
                            a = a * (-1);
                        } else
                        {
                            // nach links
                            z = (float) currentz + ((abst * (j - 1)) * (-1));
                            y = (float) currenty + zeile;
//                            x = (float) currentx + 1; // dimx/skalfak == 1
                            a = a * (-1);
                        } //else
                    }
                } else
                {
                    //mittig sitzender Port
                    z = (float) currentz;    //z = (float) currentz/z1;
                    y = (float) currenty + zeile;    //y = (float) (currenty + zeile)/y1;
//                    x = (float) currentx + 1; //dimx/skalfak; //x = (float) (currentx + dimx/skalfak)/x1;
                }
                //Ports erstellen

                portarray.get(i-1).setPosition(new Vector3d(x, y, z));

            } else
            {
                if (fehler)
                {
                    // TODO: Fehlermeldung ausgeben
//                    JOptionPane.showMessageDialog(null,"Zuviele Output-" +
//                            "Ports für die Größe des Würfels!",
//                            "Fehler",JOptionPane.ERROR_MESSAGE);
                    fehler = false;
                } //if
            } //else
        }
    }

    public int getInputPortNumber()
    {
        return inputPort.size();
    }

    public int getOutputPortNumber()
    {
        return outputPort.size();
    }

    @Override
    public ModelElement getModelRef()
    {
        return m_elem;
    }

    @Override
    public void setModelRef(IModelElement element)
    {
        if(ModelElement.class.isInstance(element))
        {
            m_elem = (ModelElement) element;
        }
    }
}
