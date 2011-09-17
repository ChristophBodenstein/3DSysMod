package swp.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.io.File;

import swp.*;
import swp.model.*;

public class MainWindow extends JFrame implements DropTargetListener {

  private DropTarget dropTarget;
  private GUI gui;
  
  private String windowTitle = "3DSysMod";

  private JSplitPane leftSplitpane;
  private JSplitPane upperSplitpane;
  private JSplitPane entireSplitpane;
  
  public MainWindow() {
    super();
    gui = new GUI();
    gui.setMainWindow(this);
    setSize(300,300);
    setTitle(windowTitle);
    setLocation(0, 0);
    Rectangle maxBounds = GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getMaximumWindowBounds(); //Ermittlung Deskotp-Bereich exklusive Taskleiste
    //setSize((int) maxBounds.getWidth(), (int) maxBounds.getHeight());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setLayout(new BorderLayout(10, 10));

    java.net.URL tmp = GUI.class.getResource("resource/32x32_Wuerfel_gruen_transp.gif");
    if (tmp != null) {
      setIconImage(Toolkit.getDefaultToolkit().createImage(tmp));
    }
    dropTarget = new DropTarget(this, this);
    setJMenuBar(gui.getMenuBar());

    getContentPane().add(gui.getToolBar(), BorderLayout.NORTH);
    getContentPane().add(gui.getStatusBar(), BorderLayout.SOUTH);

    leftSplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(gui.getTree()), new JScrollPane(gui.createPropertyView()));
    leftSplitpane.setContinuousLayout(true);

    upperSplitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSplitpane, gui);
    upperSplitpane.setContinuousLayout(true);

    entireSplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplitpane, new JScrollPane(gui.createErrorView()));

    getContentPane().add(entireSplitpane, BorderLayout.CENTER);

    setVisible(true);
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        setDividerLocations();
      }
    });
  }
  
  private void setDividerLocations() {
    leftSplitpane.setDividerLocation(0.5f);
    entireSplitpane.setDividerLocation(0.9f);
    upperSplitpane.setDividerLocation(0.16f);
  }
  
  public void setWindowTitle(String title) {
    setTitle(windowTitle + " - " + title);
  }
  
  public void drop(DropTargetDropEvent dtde) {
    try{
      dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
      String fileName = dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor).toString();
      fileName = fileName.substring(1, fileName.length() - 1);

      if(!fileName.endsWith(".xml")) {
        gui.statusbar.setMessage("Fehler: Die Modell-Datei muss die Endung '.xml' haben.");
        throw(new Exception("Keine gueltige Modell-Datei."));
      }
      //System.out.println("Drop" + dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor));
      //LoadNStore.loadModel(dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor).toString());
      try {
        File f = new File(fileName);
        //gui.addScene(f.getName());
        gui.setProject(f.getName());
        Model newModel = new Model(gui.getCurrentScene().getSceneUniverse());
        newModel.setTitle(f.getName());
        ModelLoader modLoader = new ModelLoader(f);

        modLoader.load(newModel);
        setWindowTitle(f.getName());
        gui.getCurrentScene().setModel(newModel);
        gui.statusbar.setMessage("Modell <" + f.getName() + "> erfolgreich geladen.");

      } catch(Exception e) {

      }

      gui.m_propertyEditor.setModel(gui.getCurrentScene().getModel());

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