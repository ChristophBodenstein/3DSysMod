package swp.gui;

import swp.*;
import swp.steuerung.*;
import swp.steuerung.Steuerung;
import swp.graphic.java3d.*;
import swp.graphic.*;
import swp.model.*;
import swp.sim.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.util.*;

public class Scene3D extends Canvas3D implements SelectionChangedListener, FocusListener {

  private SceneUniverse sceneUniverse;
  private Steuerung steuerung;
  private GUI gui;
  
  private Model model;
  
  private IPickable selectedObject;
  
  //private JMenuItem miLine, miNewColor, miNewCaption, miNewScale, miDelete,
  //miSetPosition, miWiiConnect, miWiiDisconnect, miWiiEnable, miWiiDisable;


  public Scene3D(GraphicsConfiguration config, GUI gui) {
    super(config);
    this.gui = gui;
    sceneUniverse = new SceneUniverse(this);
    steuerung = new Steuerung(gui, sceneUniverse);

    steuerung.addSelectionChangedListener(this);
    sceneUniverse.addBranchGraph(sceneUniverse.getSceneBranchGroup());
    
    Map vuMap = SceneUniverse.getProperties();
    System.out.println("J3D Version:" + vuMap.get("j3d.version"));
    
    steuerung.setTranslation(new Vector3d(15, 5, 30));
    steuerung.setRotation(-Math.PI / 16, Math.PI / 8);
    
    model = new Model(sceneUniverse);
    
    addFocusListener(this);
  }

  public SceneUniverse getSceneUniverse(){
    return sceneUniverse;
  }

  public Steuerung getSteuerung(){
    return steuerung;
  }

  public Model getModel(){
    return model;
  }

  public void setModel(Model m){
    model = m;
  }

  /**
   * Wenn sich das selektierte Objekt aendert (bzw. dieses abgewaehlt wurde),
   * so wird diese Methode als Nachricht von der Steuerungsklasse aufgerufen.
   * Somit ist eine Reaktion auf das Anwaehlen und Abwaehlen von Objekten moeglich.
   * @param e Das neue selektierte Objekt
   */
  @Override
  public void selectionChanged(SelectionEvent e) {
/*    line.setEnabled(IGraphicPort.class.isInstance(e.getSelectedObject()));
    miLine.setEnabled(IGraphicPort.class.isInstance(e.getSelectedObject()));
    miNewColor.setEnabled(IColorable.class.isInstance(e.getSelectedObject()));
    miNewCaption.setEnabled(INamable.class.isInstance(e.getSelectedObject()));
    miNewScale.setEnabled(IScalable.class.isInstance(e.getSelectedObject()));
    miDelete.setEnabled(IGraphicElement.class.isInstance(e.getSelectedObject()));
    miSetPosition.setEnabled(IPositionable.class.isInstance(e.getSelectedObject()));*/
    gui.statusbar.setElement(e);
    if(IPickable.class.isInstance(e.getSelectedObject())) {
      selectedObject = (IPickable)e.getSelectedObject();
      System.out.println(e.getSelectedObject().toString());
    } else {
      selectedObject = null;
    }
  }
  
  public void focusGained(FocusEvent e) {
    gui.getStatusbar().setElement(selectedObject);
    gui.m_propertyEditor.setModel(model);
    gui.getMainWindow().setWindowTitle(model.getTitle());
  }
  public void focusLost(FocusEvent e) {
    //nothing to do
  }
}