package swp.graphic.java3d;

import com.sun.j3d.utils.universe.SimpleUniverse;
    
import java.awt.*;
import java.util.Enumeration;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import javax.media.j3d.*;
import javax.vecmath.*;
import swp.graphic.IGraphicConnection;
import swp.graphic.IGraphicElement;
import swp.graphic.IGraphicPort;
import swp.model.ModelElement;
import swp.*;

/**
 * Klasse PopupMenue
 * @author Silvio
 */

public class SceneUniverse extends SimpleUniverse implements DropTargetListener {

    // Bereitstellung von grundlegenden 3D-Aufgaben
    private Canvas3D c3D = null;
    // oberster Gruppenknoten
    private BranchGroup objRoot = null;
    // Gruppenknoten
    private BranchGroup sceneBranchGroup = null;
    
    private DropTarget dropTarget;


    public SceneUniverse(Canvas3D c3D) {
        super(c3D);
        this.c3D=c3D;
        getViewingPlatform().setNominalViewingTransform();
        getViewer().getView().setBackClipDistance(1000);
        getViewer().getView().setMinimumFrameCycleTime(20);

        sceneBranchGroup = createSceneBranchGroup();
        Background background = createBackground();

        if(background != null){
          sceneBranchGroup.addChild(background);
        }
        dropTarget = new DropTarget(c3D, this);
        
        //Konfiguration fuers Head-Tracking:
        c3D.setMonoscopicViewPolicy(View.LEFT_EYE_VIEW);
    }
    //---------------------------------------------------------------------------
    /** createSceneBranchGroup()
        Aufgabe:
        Erstellung der Wurzel des Szenengraphen und Hinzufügen von Kindknoten
    */
  public BranchGroup createSceneBranchGroup() {

    objRoot = new BranchGroup();

        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
        objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        //objRoot.setCapability(BranchGroup.ALLOW_PARENT_READ);

        TransformGroup objTrans = new TransformGroup();
    objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);

    BoundingSphere bounds = new BoundingSphere(
                /*Mittelpunkt*/new Point3d(0.0,0.0,0.0),
                /*Radius*/ 100000);

    sceneBranchGroup = new BranchGroup();

    sceneBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
    sceneBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    sceneBranchGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        sceneBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
        //sceneBranchGroup.setCapability(BranchGroup.ALLOW_PARENT_READ);

        sceneBranchGroup.addChild(createCoordinates());
        sceneBranchGroup.addChild(createXCoordinates());
    //  sceneBranchGroup.addChild(createYCoordinates());
        sceneBranchGroup.addChild(createZCoordinates());
        int n;
        sceneBranchGroup.addChild(createXNumbers(0));
        for(n=1;n<41;n++){
            sceneBranchGroup.addChild(createXNumbers(n));
          //sceneBranchGroup.addChild(createYNumbers(n));
            sceneBranchGroup.addChild(createZNumbers(n));
        }

    Color3f lColor1 = new Color3f(0.7f,0.7f,0.7f);
    Vector3f lDir1  = new Vector3f(-1.0f,-1.0f,-1.0f);
    Color3f alColor = new Color3f(0.2f,0.2f,0.2f);

    AmbientLight aLgt = new AmbientLight(alColor);
    aLgt.setInfluencingBounds(bounds);

    DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
    lgt1.setInfluencingBounds(bounds);

        objRoot.addChild(aLgt);
    objRoot.addChild(lgt1);

        objTrans.addChild(sceneBranchGroup);
    objRoot.addChild(objTrans);

    return objRoot;
  } // createSceneBranchGroup
//---------------------------------------------------------------------------
    /** createBackground()
        Aufgabe:
        Setzen der Hintergrundfarbe
    */
  protected Background createBackground() {
    Background back = new Background(new Color3f(1.0f, 1.0f, 1.0f));
    back.setApplicationBounds(createApplicationBounds());
    return back;
  }
    //---------------------------------------------------------------------------
    /** createCoordinates()
        Aufgabe:
        Erstellung des Koordinatensystems
    */
    protected BranchGroup createCoordinates() {
        BranchGroup bg = new BranchGroup();
      LineArray la =new LineArray(4, LineArray.COORDINATES| LineArray.COLOR_3);

    la.setCoordinate(0, new Point3f(0, 0, 0));
        la.setCoordinate(1, new Point3f(200, 0, 0));
        //la.setCoordinate(2, new Point3f(0, 0, 0));
        //la.setCoordinate(3, new Point3f(0, 100, 0));
        la.setCoordinate(2, new Point3f(0, 0, 0));
        la.setCoordinate(3, new Point3f(0, 0, 200));
        la.setColor (0, new Color3f (Color.RED));
        la.setColor (1, new Color3f (Color.RED));
       // la.setColor (2, new Color3f (Color.GREEN));
       //la.setColor (3, new Color3f (Color.GREEN));
        la.setColor (2, new Color3f (Color.BLUE));
        la.setColor (3, new Color3f (Color.BLUE));
    
    LineAttributes attr = new LineAttributes();
    attr.setLineAntialiasingEnable(true);
    
    Appearance appi = new Appearance();

    appi.setLineAttributes(attr);
    bg.addChild(new Shape3D (la, appi));

        return bg;
  }
//---------------------------------------------------------------------------
    /** createXCoordinates()
        Aufgabe:
        Erstellt die Markierungen an der X-Achse
    */
    protected BranchGroup createXCoordinates() {
        BranchGroup bg = new BranchGroup();

    //beide Variablen für den Arrayindex i für das schwarze Array
    int n=0,i=0;

    //graue Linien
    LineArray grayl = new LineArray(320, LineArray.COORDINATES| LineArray.COLOR_3);
        LineAttributes attrgray = new LineAttributes();
    Appearance appigray = new Appearance();
    attrgray.setLineAntialiasingEnable(true);
    appigray.setLineAttributes(attrgray);
    attrgray.setLineWidth(0.125f);//Linienstärke der schwarzen Linie




    //schwarze Linien
    LineArray blackl = new LineArray(80, LineArray.COORDINATES| LineArray.COLOR_3);
    LineAttributes attrblack = new LineAttributes();
    Appearance appiblack = new Appearance();
    attrblack.setLineAntialiasingEnable(true);
    appiblack.setLineAttributes(attrblack);
    attrblack.setLineWidth(0.5f);//Linienstärke der schwarzen Linie


        for (int a=1;a<201;++a) {
           
            // Jede 5te Linie schwarz zeichnen
      if (a%5 == 0){
                
        blackl.setCoordinate(i, new Point3f(a, 0, 0));
                blackl.setCoordinate(i+1, new Point3f(a, 0, 200));

        blackl.setColor(i, new Color3f(Color.BLACK));
                blackl.setColor(i+1, new Color3f(Color.BLACK));

        i+=2;
      }
            else{
                grayl.setCoordinate(n, new Point3f(a, 0, 0));
                grayl.setCoordinate(n+1, new Point3f(a, 0, 200));

        grayl.setColor(n, new Color3f(Color.GRAY));
                grayl.setColor(n+1, new Color3f(Color.GRAY));

        n+=2;
            }
            
        }
        
    
    bg.addChild(new Shape3D (blackl,appiblack));
    bg.addChild(new Shape3D (grayl,appigray));
    
        return bg;
  }
//---------------------------------------------------------------------------
    /** createYCoordinates()
        Aufgabe:
        Erstellt die Markierungen an der Y-Achse
    */
   /* protected BranchGroup createYCoordinates() {
        BranchGroup bg = new BranchGroup();
        int n,a;
        LineArray lb = new LineArray(400, LineArray.COORDINATES| LineArray.COLOR_3);
        n=0;
        for (a=-100;a<100;a++) {
            lb.setCoordinate(n, new Point3f(0, a, 0));
            lb.setCoordinate(n+1, new Point3f(-1, a, 0));
            lb.setColor(n, new Color3f(Color.GREEN));
            lb.setColor(n+1, new Color3f(Color.GREEN));
            n+=2;
        }
        bg.addChild(new Shape3D (lb));
        return bg;
  }*/
//---------------------------------------------------------------------------
    /** createZCoordinates()
        Aufgabe:
        Erstellt die Markierungen an der Z-Achse
    */
   protected BranchGroup  createZCoordinates() {
        BranchGroup bg = new BranchGroup();
        
    //beide Variablen für den Arrayindex i für das schwarze Array
    int n=0,i=0;

    //graue Linien
    LineArray grayl = new LineArray(320, LineArray.COORDINATES| LineArray.COLOR_3);
        LineAttributes attrgray = new LineAttributes();
    Appearance appigray = new Appearance();
    attrgray.setLineAntialiasingEnable(true);
    appigray.setLineAttributes(attrgray);
    attrgray.setLineWidth(0.125f);//Linienstärke der schwarzen Linie




    //schwarze Linien
    LineArray blackl = new LineArray(80, LineArray.COORDINATES| LineArray.COLOR_3);
    LineAttributes attrblack = new LineAttributes();
    Appearance appiblack = new Appearance();
    attrblack.setLineAntialiasingEnable(true);
    appiblack.setLineAttributes(attrblack);
    attrblack.setLineWidth(0.5f);//Linienstärke der schwarzen Linie


    for (int a=1;a<201;++a) {

            // Jede 5te Linie schwarz zeichnen
      if (a%5 == 0){

        blackl.setCoordinate(i, new Point3f(0, 0, a));
                blackl.setCoordinate(i+1, new Point3f(200, 0, a));

        blackl.setColor(i, new Color3f(Color.BLACK));
                blackl.setColor(i+1, new Color3f(Color.BLACK));

        i+=2;
      }
            else{
                grayl.setCoordinate(n, new Point3f(0, 0, a));
                grayl.setCoordinate(n+1, new Point3f(200, 0, a));

        grayl.setColor(n, new Color3f(Color.GRAY));
                grayl.setColor(n+1, new Color3f(Color.GRAY));

        n+=2;
            }

        }

      bg.addChild(new Shape3D (blackl,appiblack));
    bg.addChild(new Shape3D (grayl,appigray));
       return bg;
  }
//---------------------------------------------------------------------------
    /** createXNumbers(...)
        Aufgabe:
        Erstellt die Numerierung entlang der X-Achse
    */
    protected BranchGroup createXNumbers(int n) {
        BranchGroup bg = new BranchGroup();
        Appearance a = new Appearance();

    LineAttributes attr = new LineAttributes();
    attr.setLineAntialiasingEnable(true);
    
    a.setLineAttributes(attr);

    ColoringAttributes textColor = new ColoringAttributes();
        textColor.setColor(0, 0, 0);
        a.setColoringAttributes(textColor);
        a.setMaterial(new Material());
        a.setLineAttributes(attr);
    Font f = new Font("Arial Narrow",Font.PLAIN,1);
        FontExtrusion z = new FontExtrusion();
        Font3D d = new Font3D(f,z);
        String s = ""+n*5+"";
        Point3f p = new Point3f(n*5,-1,0);
        Text3D b = new Text3D(d,s,p);
        OrientedShape3D OS = new OrientedShape3D(b,a,1,p);
      
        OS.setAppearance(a);
    bg.addChild(OS);

        return bg;
    }
//---------------------------------------------------------------------------
    /** createYNumbers(...)
        Aufgabe:
        Erstellt die Numerierung entlang der Y-Achse
    */
    protected BranchGroup createYNumbers(int n) {
        BranchGroup bg = new BranchGroup();
        Appearance a = new Appearance();
    LineAttributes attr = new LineAttributes();

    attr.setLineAntialiasingEnable(true);
    a.setLineAttributes(attr);

        ColoringAttributes textColor = new ColoringAttributes();
        textColor.setColor(1.0f, 0.5f, 0.5f);
        a.setColoringAttributes(textColor);
        a.setMaterial(new Material());
        Font f = new Font("Arial Narrow",Font.PLAIN,1);
        FontExtrusion z = new FontExtrusion();
        Font3D d = new Font3D(f,z);
        String s = ""+n*5+"";
        Point3f p = new Point3f(-1,n*5-1,0);
        Text3D b = new Text3D(d,s,p);

    OrientedShape3D OS = new OrientedShape3D(b,a,1,p);

    OS.setAppearance(a);
    bg.addChild(OS);

        return bg;
    }
//---------------------------------------------------------------------------
    /** createZNumbers(...)
        Aufgabe:
        Erstellt die Numerierung entlang der Z-Achse
    */
    protected BranchGroup createZNumbers(int n) {
        BranchGroup bg = new BranchGroup();
        Appearance a = new Appearance();
    LineAttributes attr = new LineAttributes();

    attr.setLineAntialiasingEnable(true);
    a.setLineAttributes(attr);

    ColoringAttributes textColor = new ColoringAttributes();
        textColor.setColor(1.0f, 0.0f, 0.0f);
        a.setColoringAttributes(textColor);
        a.setMaterial(new Material());
        Font f = new Font("Arial Narrow",Font.PLAIN,1);
        FontExtrusion z = new FontExtrusion();
        Font3D d = new Font3D(f,z);
        String s = ""+n*5+"";
        Point3f p = new Point3f(-1,-1,n*5);
        Text3D b = new Text3D(d,s,p);
        OrientedShape3D OS = new OrientedShape3D(b,a,1,p);
        
        OS.setAppearance(a);
    bg.addChild(OS);

        return bg;
    }
//---------------------------------------------------------------------------

    /** createApplicationBounds()
        Aufgabe:
        Setzte die Grenzen des kugelförmigen Universums
    */
  private Bounds createApplicationBounds()  {
    return new BoundingSphere(
                /*Mittelpunkt*/new Point3d(0.0,0.0,0.0),
                /*Radius*/ 1000.0);
  }

    public BranchGroup getObjRoot(){
        return objRoot;
    }


    public BranchGroup getSceneBranchGroup(){
        return sceneBranchGroup;
    }

    public IGraphicElement createGraphicElement(ModelElement elem)
    {
        Cube res = new Cube(elem);
        //TODO: abstand anders ermitteln
        Point3d vec = new Point3d(0, 0, -30);
        Transform3D trans = new Transform3D();
        this.getViewingPlatform().getViewPlatformTransform().getTransform(trans);
        trans.transform(vec);
        res.setPosition(new Vector3d(vec));

        return res;
    }

    public IGraphicConnection createGraphicConnection(IGraphicPort port1, IGraphicPort port2)
    {
        return new Connection(port1, port2, sceneBranchGroup);
    }

    public void clear()
    {
        Enumeration en = sceneBranchGroup.getAllChildren();
        Object o;
        while (en.hasMoreElements())
        {
            o = en.nextElement();
            if (o instanceof Cube)
            {
                Cube cube = (Cube) o;
                cube.delete();
            }
            if (o instanceof Connection)
            {
                Connection.deleteAllConnections(sceneBranchGroup);
            }
        }
    }
    
  public void drop(DropTargetDropEvent dtde) {
    try{
      dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      String itemName = dtde.getTransferable().getTransferData(DataFlavor.stringFlavor).toString();
      System.out.println(itemName);
      ((GUI)c3D.getParent()).createNewCube();
      /*
      String fileName = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor).toString();
      fileName = fileName.substring(1, fileName.length() - 1);

      if(!fileName.endsWith(".xml")) {
        throw(new Exception("Keine gueltige Modell-Datei."));
      }
      System.out.println("Drop" + dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
      //LoadNStore.loadModel(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor).toString());
      gui.sceneUniverse.clear();
      Model newModel = new Model(gui.sceneUniverse);
      try {
        File f = new File(fileName);
        ModelLoader modLoader = new ModelLoader(f);

        modLoader.load(newModel);
        setWindowTitle(f.getName());
      } catch(Exception e) {

      }
      gui.m_model = newModel;
      gui.m_propertyEditor.setModel(gui.m_model);


        //return returnVal;


     */
    } catch(Exception e) {
      //Es koennen nur Dateien gedropped werden. Ansonsten passiert nichts.
    }

  }

  public void dragEnter(DropTargetDragEvent dtde) {
     //System.out.println("Drag Enter");
  }

  public void dragExit(DropTargetEvent dte) {
    //System.out.println("Drag Exit");
  }

  public void dragOver(DropTargetDragEvent dtde) {
    //System.out.println("Drag Over");
  }

  public void dropActionChanged(DropTargetDragEvent dtde) {
    //System.out.println("Drop Action Changed");
  }
}
