package swp.steuerung;

import swp.*;

import com.sun.j3d.utils.picking.*;
import javax.media.j3d.*;
import javax.swing.event.EventListenerList;
import javax.vecmath.*;
import swp.graphic.IGraphicPort;
import swp.graphic.IPickable;
import swp.graphic.java3d.SceneUniverse;
import swp.model.ModelConnection;


public class Steuerung {
  public GUI gui;
  public SceneUniverse universe;
  public Transform3D htTrans;
  private MyKeyNavigatorBehavior keyControl;
  private InputDevice headControl; //the head tracker
  private MouseControl mouseControl;
  private WiimoteControl wiimoteControl;

  // Rotation lässt sich nicht aus Transform3D extrahieren, deshalb zwischenspeichern
  private double rotX = 0, rotY = 0;

  // Das aktuell selektierte Objekt
  private IPickable selected = null;
  private boolean connectNext = false;
    
        //SelectionchangedEvent
        private EventListenerList m_changeListenerlist = new EventListenerList();



  /**
   * Konstruktor der oberen Steuerungsklasse
   * @param gui Das Gui-Objekt
     * @param universe Das SceneUniverse-Objekt
   */
  public Steuerung(GUI gui, SceneUniverse universe) {
    this.gui = gui;
    this.universe = universe;
    BranchGroup objRoot = universe.getObjRoot();
    BoundingSphere bounds = new BoundingSphere(new Point3d(), 1000);

    keyControl = new MyKeyNavigatorBehavior(
      universe.getViewingPlatform().getViewPlatformTransform()
    );
    keyControl.setSchedulingBounds(bounds);
    objRoot.addChild(keyControl);

    headControl = new HeadTracker(
      universe.getViewingPlatform().getViewPlatformTransform(),
      universe.getViewer().getPhysicalBody(), gui
    );
    headControl.initialize();
    htTrans = new Transform3D();
    PhysicalEnvironment physicalEnvironment = universe.getViewer().getPhysicalEnvironment();
    physicalEnvironment.addInputDevice(headControl);

 
    physicalEnvironment.setSensor(0, headControl.getSensor(0));
    physicalEnvironment.setHeadIndex(0);

    View universeView = universe.getViewer().getView();
    universeView.setViewPolicy(View.SCREEN_VIEW);
    universeView.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
    universeView.setUserHeadToVworldEnable(true);
    universeView.setCoexistenceCenteringEnable(false); //verzerrt die Szene

     Transform3D transform3D = new Transform3D();
    transform3D.setTranslation(new Vector3d(0.001, 0.1, 0.2));
    universe.getCanvas().getScreen3D().setTrackerBaseToImagePlate(transform3D);
    universeView.setTrackingEnable(true);

 
    mouseControl = new MouseControl(this);
    mouseControl.setSchedulingBounds(bounds);
    objRoot.addChild(mouseControl);

    wiimoteControl = new WiimoteControl(this);
    wiimoteControl.setSchedulingBounds(bounds);
    objRoot.addChild(wiimoteControl);
  }

  /**
   * Bei Beenden des Prgramms aufrufen. Wird dazu benötigt, um den WiiMote-Manager
   * ordnungsgemäß zu beenden.
   */
  public void shutDown() {
    wiimoteControl.shutDown();
  }

  /**
   * Sucht nach einer bereits per Bluetooth verbundenen WiiMote und stellt
   * diese dem Programm zur Verfügung
   * @return Das Ergebnis der Suche
   */
  public boolean connectWiimote() {
    wiimoteControl.connect();
    return wiimoteControl.isConnected();
    
  }
  /**
   * Trennt die Verbindung zur WiiMote (diese bleibt aber weiterhin mit dem
   * Rechner verbunden).
   */
  public void disconnectWiimote() {
    wiimoteControl.disconnect();
  }
  /**
   * Aktiviert die WiiMote.
   */
  public void enableWiimote() {
    wiimoteControl.enable();
  }
  /**
   * Deaktiviert vorrübergehend die WiiMote.
   */
  public void disableWiimote() {
    wiimoteControl.disable();
  }

  /**
   * Aktiviert die Tastatur-Steuerung.
   */
  public void enableKeyboard() {
    keyControl.setEnable(true);
  }
  /**
   * Deaktiviert vorrübergehend die Tastatursteuerung.
   */
  public void disableKeyboard() {
    keyControl.setEnable(false);
  }


  /**
   * Stellt den Zugriff auf die Gui für die untergeordneten Klassen zur Verfügung
   * @return Das Gui Objekt
   */
  public GUI getGui() {
    return this.gui;
  }
  /** 
   * Stellt den zugriff auf das SimpleUniverse für die untergeordneten Klassen zur Verfügung
   * @return Das bei der Erstellung übergebene Universum
   */
  public SceneUniverse getUniverse() {
    return this.universe;
  }



//---------- SELEKTION -------------------------------------------------------//

  /**
   * Setzt das angegebene Objekt als selektiertes Objekt.
   * Anschließend wird die Gui mittels notifyChangedSelected() vom Ändern
   * des selektierten Objektes benachrichtigt.
   * @param pick Das Objekt, das als selektiert gesetzt werden soll
   */
  public void setSelected(IPickable pick) {
    // Wenn sich das Objekt nicht geändert hat, sind wir bereits fertig
    if (pick == selected)
      return;

    // Das alte Objekt abgewählen, also dessen unselect() aufrufen
    if (selected != null) {
      selected.unselect();
    }

    // Das neue Objekt setzen, und dessen select() aufrufen
    IPickable oldPick = selected;
    selected = pick;
    if (selected != null) {
      selected.select();
    }

    // Wurde zuvor gefordert, dass eine Verbindungslinie gezogen werden soll,
    // so wird dies nun erledigt
    if (connectNext && (oldPick instanceof IGraphicPort) && (pick instanceof IGraphicPort)) {
                    
                        new ModelConnection(universe.createGraphicConnection((IGraphicPort)oldPick, (IGraphicPort)pick) );
    }
    connectNext = false;

    // Gui benachrichtigen
    
                this.fireSelectionChangedEvent(selected);

  }
  /**
   * Liefert das aktuell ausgewählte Objekt bzw. null, falls kein Objekt
   * ausgewählt wurde.
   * @return Das aktuell ausgewählte Objekt
   */
  public IPickable getSelected() {
    return this.selected;
  }

  /**
   * Fordert die Steuerung auf, zum nächsten ausgewählten Port eine Verbindungs-
   * linie zu ziehen.
   */
  public void connectToNextPort() {
    // Nur Ports dürfen verbunden werden
    connectNext = (selected instanceof IGraphicPort);
  }


//---------- PICKING ---------------------------------------------------------//
  
  /**
   * Liefert das naheste Objekt unterhalb der angegeben Bildschirm-Koordinaten
   *
   * @param x Die X Koordinate für das Picking
   * @param y Die Y Koordinate für das Picking
   * @param setAsSelected Falls true, wird das ermittelte Objekt direkt als
   * neues selektiertes Objekt gesetzt, ansonsten wird nur das Pick:ergebnis zurück geliefert.
   * @return Das gepickte Objekt
   */
  public IPickable pickObject(int x, int y, boolean setAsSelected) {
    PickCanvas canvas = new PickCanvas(
        getUniverse().getCanvas(),
        getUniverse().getSceneBranchGroup()
    );
    canvas.setMode(PickCanvas.GEOMETRY);
    canvas.setShapeLocation(x, y);




    PickResult pick = canvas.pickClosest();
    IPickable newPick = null;

    if (pick != null) {
      BranchGroup result = (BranchGroup) pick.getNode(PickResult.BRANCH_GROUP);
      if (result instanceof IPickable) {
        newPick = (IPickable) result;
      }
    }

    // Falls gefordert, das gefundene Objekt als selektiert setzen,
    // ansonsten nur zurückliefern.
    if (setAsSelected) {
      this.setSelected(newPick);
    }
    return newPick;
  }
  /**
   * Liefert das naheste Objekt unterhalb der angegebenen Bildschirm-Koordinaten
   * und setzt das gefundene Objekt direkt als neues selektiertes Objekt.
   *
   * @param x Die X Koordinate für das Picking
   * @param y Die Y Koordinate für das Picking
   * @return Das gepickte Objekt
   */
  public IPickable pickObject(int x, int y) {
    return this.pickObject(x, y, true);
  }


//---------- ROTATION --------------------------------------------------------//

  /**
   * Setzt die Rotation auf die angegebenen Winkel. Die Wertebereiche werden
   * dabei überprüft und die Werte ggf. angepasst.
   *
   * @param newRotX X-Rotation im Bgenmaß, positiv dreht nach oben.
   * Bereich ist auf +/- Pi/2 (entspricht +/- 90°) begrenzt, um ein Kopf-Stehen
   * der Kamera zu verhindern.
   * @param newRotY Y-Rotation im Bogenmaß, positiv dreht nach rechts.
   * Wert wird um 2*Pi verändert, wenn er +2*Pi übersteigt oder -2*Pi unterschreitet.
   */
  public void setRotation(double newRotX, double newRotY) {
    // Wenn sich die Werte nicht verändert haben, müssen wir auch nix rechnen
    if ((newRotX == rotX) && (newRotY == rotY))
      return;

    // Wertebereich für RotX: [-Pi/2, +Pi/2]
    rotX = Math.max(Math.min(newRotX, Math.PI/2), -Math.PI/2);

    // Wertebereich für RotY: [-2*Pi, +2*Pi]
    // Effekt durch doppelten Wertebereich: Bei Korrektur landen wir nahe der
    // 0, sodass ein sofortiges Zurückspringen nicht stattfinden kann.
    rotY = newRotY;
    while (Math.abs(rotY) > Math.PI*2)
      rotY -= Math.signum(rotY) * Math.PI*2;

    Transform3D trans = this.getViewTransform(); // Aktuelle Transformationsmatrix
    Vector3d vec = this.getTranslation();    // Aktuelle Translation

    // Die neue Transformationsmatrix errechnen, die neben der Rotation
    // auch bereits die Traslation enthält.
    double sinx = Math.sin(rotX), cosx = Math.cos(rotX);
    double siny = Math.sin(rotY), cosy = Math.cos(rotY);
    Matrix4d matrix = new Matrix4d(
       cosy,  sinx*siny,  cosx*siny,  vec.x,
       0,     cosx,      -sinx,       vec.y,
      -siny,  sinx*cosy,  cosx*cosy,  vec.z,
       0,     0,          0,          1
    );

    trans.set(matrix);
    this.setViewTransform(trans);
  }
  /**
   * Liefert die aktuelle Rotation um die X-Achse.
   * @return Die X-Rotation im Bogenmaß
   */
  public double getRotationX() {
    return rotX;
  }
  /**
   * Liefert die aktuelle Rotation um die Y-Achse.
   * @return Die Y-Rotation im Bogenmaß
   */
  public double getRotationY() {
    return rotY;
  }



//---------- TRANSLATION -----------------------------------------------------//

  /**
   * Setzt die Kamera an die angegebene Position, ohne die Rotation zu verändern.
   * @param position Die neue Position
   */
  public void setTranslation(Vector3d position) {
    Transform3D trans = this.getViewTransform();
    trans.setTranslation(position);
    this.setViewTransform(trans);
  }
  /**
   * Liefert die aktuelle Kameraposition im 3D-Raum.
   * @return Die Position der Kamera
   */
  public Vector3d getTranslation() {
    Transform3D trans = this.getViewTransform();
    Vector3d vec = new Vector3d();
    trans.get(vec);
    return vec;
  }

//---------- TRANSFORMATION --------------------------------------------------//

  /**
   * Liefert die aktuelle Transformationsmatrix der ViewPlatform. Diese kann
   * dann für Bewegungen und Drehungen der Kamera genutzt werden.
   * @return Das aktuelle Transform3D-Objekt der ViewingPlatform
   */
  private Transform3D getViewTransform() {
    Transform3D trans = new Transform3D();
    universe.getViewingPlatform().getViewPlatformTransform().getTransform(trans);
    return trans;
  }
  /**
   * Setzt die aktuelle Transformationsmatrix der ViewPlatform.
   * @param trans Die neue Transformationsmatrix
   */
  private void setViewTransform(Transform3D trans) {
    universe.getViewingPlatform().getViewPlatformTransform().setTransform(trans);
  }

    /**
     * Transformiert den angegebenen Vektor mit der aktuellen Transformationsmatrix
     * der Kamera.
     * @param vec Der zu transformierende Vektor
     */
    protected void transformVector(Vector3d vec) {
        getViewTransform().transform(vec);
    }

    public void addSelectionChangedListener(SelectionChangedListener l)
    {
        m_changeListenerlist.add(SelectionChangedListener.class, l);
    }

    public void removeSelectionChangedListener(SelectionChangedListener l)
    {
        m_changeListenerlist.remove(SelectionChangedListener.class, l);
    }

    protected void fireSelectionChangedEvent(Object selection) {
      // Guaranteed to return a non-null array
      Object[] listeners = m_changeListenerlist.getListenerList();
      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length-2; i>=0; i-=2) {
          if (listeners[i]==SelectionChangedListener.class) {
              // Lazily create the event:
              SelectionEvent selCh = new SelectionEvent(this, selection);
              ((SelectionChangedListener)listeners[i+1]).selectionChanged(selCh);
          }
      }
  }
}
