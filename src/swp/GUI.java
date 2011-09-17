package swp;

import swp.gui.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import swp.library.NoElementFoundException;
import swp.steuerung.Steuerung;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.media.j3d.*;
import javax.swing.tree.TreePath;
import javax.vecmath.*;
import swp.graphic.IColorable;
import swp.graphic.IGraphic;
import swp.graphic.IGraphicElement;
import swp.graphic.IGraphicInflexPoint;
import swp.graphic.IGraphicPort;
import swp.graphic.INamable;
import swp.graphic.IPickable;
import swp.graphic.IPositionable;
import swp.graphic.IScalable;
import swp.graphic.java3d.Connection;
import swp.graphic.java3d.SceneUniverse;
import swp.gui.CaptionDialog;
import swp.gui.ColorDialog;
import swp.gui.ErrorView;
import swp.library.Library;
import swp.model.Model;
import swp.model.ModelElement;
import swp.sim.Simulator;
import swp.gui.PropertyEditor;
import swp.gui.ScaleDialog;

public class GUI extends JTabbedPane {//implements SelectionChangedListener
// Attribute
//###########################################################################
    // das Hauptfenster

    private JFrame newProjectDialog = new JFrame();
    
    // die Buttons für Ports und Verbindungen in der Toolbar
    private JButton screenShot = null, line = null;
    // das Hauptuniversum für 3D-Darstellung
    //public SceneUniverse sceneUniverse = null;
    // Die Steuerung
    protected Steuerung steuerung = null;
    private MainWindow mainWindow;
    // Bereitstellung von grundlegenden 3D-Aufgaben
    //private Scene3D c3D = null;
    // Baumstruktur für die Objekt-Library
    private JTree tree = null;
    // Untermenü für die Ansichten im 3D-Fenster
    private JComboBox views = null;
    // ein Schieberegler für die Größenanpassung der Objekte
    private JSlider scale;
    private boolean setPortButtonValue = false, setLineButtonValue = false;
    private JMenuItem miLine, miNewColor, miNewCaption, miNewScale, miDelete,
            miSetPosition, miWiiConnect, miWiiDisconnect, miWiiEnable, miWiiDisable;

    
    private boolean fullscreenStatus = false;
    private JMenuBar menubar;
    private JToolBar toolbar;
    public StatusBar statusbar;
    //public Model m_model = null;
    private Simulator m_sim = null;
    public swp.gui.PropertyEditor m_propertyEditor = null;
    private ErrorView m_errorEditor = null;

    /** Methoden
    ###########################################################################
    GUI()
    Konstruktor
    Aufgabe:
    Initialisierung der Benutzeroberfläche
     */
    public GUI()
    {
        // das JPanel (die GUI) wird in 5 Regionen aufgeteilt
        //setLayout(new CardLayout());

        createMenuBar();
        createToolBar();
        createStatusBar();
        createTree();

        // Initialisierung des Szenengraphen
        GraphicsConfiguration config = SceneUniverse.getPreferredConfiguration();
        Scene3D c3D = new Scene3D(config, this);
        addTab("Neues Projekt", c3D);
        //sceneUniverse = c3D.getSceneUniverse();
        steuerung = c3D.getSteuerung();
        
        //Modell initalisieren
        //m_model = c3D.getModel();

    }
//---------------------------------------------------------------------------

    public Scene3D getCurrentScene() {
      return (Scene3D)getSelectedComponent();
    }

    public void addScene() {
      addScene("Neues Projekt");
    }

    public StatusBar getStatusbar() {
      return statusbar;
    }

    public void setMainWindow(MainWindow mw) {
      mainWindow = mw;
    }

    public MainWindow getMainWindow() {
      return mainWindow;
    }

    public Steuerung getSteuerung() {
      return getCurrentScene().getSteuerung();
    }

    public void addScene(String title) {
      GraphicsConfiguration config = SceneUniverse.getPreferredConfiguration();
      Scene3D s3D = new Scene3D(config, this);
      addTab(title, s3D);
      setSelectedComponent(s3D);
    }
    public void setProject(String title) {
      setTitleAt(getSelectedIndex(), title);
    }
    /**---------------------------------------------------------------------------
    createNewProjectButton(...)
    Aufgabe:
    Erzeugung des Buttons "Neues Projekt"
     */
    private JButton createNewProjectButton()
    {
        JButton newProject = new JButton(new ImageIcon("resource"
                + "/icons/png/16x16/new1.png"));
        newProject.setToolTipText("neues Projekt");
        newProject.addActionListener(new newProjectDialogListener());

        return newProject;
    }
//---------------------------------------------------------------------------

    public PropertyEditor createPropertyView()
    {
        m_propertyEditor = new PropertyEditor();
        m_propertyEditor.setModel(getCurrentScene().getModel());
        steuerung.addSelectionChangedListener(m_propertyEditor);
        return m_propertyEditor;
    }

    public ErrorView createErrorView()
    {
        m_errorEditor = new ErrorView();
        return m_errorEditor;
    }
    /** createObjCreateButton()
    Aufgabe:
    Erzeugung des Buttons "neues Objekt"
     */
    public JButton createObjCreateButton()
    {
//        JButton objCreate = new JButton(new ImageIcon("resource"
//                + "/icons/png/16x16/add1.png"));
        JButton objCreate = new JButton(new ImageIcon("resource/icons/png/16x16/add1.png"));
        objCreate.setToolTipText("Objekt erstellen");
        objCreate.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                changeCubeProperties();
            }
        });
        return objCreate;
    }
//---------------------------------------------------------------------------

    /** changeCubeProperties()
    Aufgabe:
    Anpassung der Eigenschaften des Würfel-Objektes, das eingefügt werden soll
     */
    private void changeCubeProperties()
    {

        createNewCube(tree);

    }

    public void createNewCube() {
      createNewCube(tree);
    }

    public void createNewCube(JTree tree) {
        if (tree.getLeadSelectionPath() == null) {
            return;
        }

        TreePath tp = tree.getLeadSelectionPath();

        if (!((DefaultMutableTreeNode) tp.getLastPathComponent()).isLeaf()) {
            return;
        }


        try {
            ModelElement modElem = Library.getInstance().createElementByPath(tp);

            String name = JOptionPane.showInputDialog("Bitte Beschriftung eingeben", tp.getLastPathComponent().toString());
            if (name != null && !name.isEmpty())
            {
                getCurrentScene().getModel().addModelElement(modElem);

                modElem.setDisplayText(name);
            }
        } catch (NoElementFoundException ex)
        {
            JOptionPane.showMessageDialog(null, "Konnte Element nicht erzeugen!");
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

//---------------------------------------------------------------------------
    /** createObjDeleteButton
    Aufgabe:
    Erzeugen des Buttuns "Objekt löschen"
     */
    private JButton createObjDeleteButton()
    {
        JButton objDelete = new JButton(new ImageIcon("resource"
                + "/icons/png/16x16/remove.png"));
        objDelete.setToolTipText("Objekt löschen");
        objDelete.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteObj();
            }
        });
        return objDelete;
    }
//---------------------------------------------------------------------------

    /** deleteObj()
    Aufgabe:
    löscht ein Objekt aus dem Szenengraph
     */
    public void deleteObj()
    {
        IPickable pick = steuerung.getSelected();

        if (IGraphic.class.isInstance(pick))
        {
            IGraphic selected = (IGraphic) pick;
            if (selected != null)
            {
                selected.delete();
                steuerung.setSelected(null);
            } else
            {
                JOptionPane.showMessageDialog(null, "Kein Objekt gewählt");
            }
        }
    }
//---------------------------------------------------------------------------

  public void setFocusToCanvas3D() {
    getSelectedComponent().requestFocusInWindow();
  }

    /** createPortNumberButton
    Aufgabe:
    Erzeugung des Buttons "Ports festlegen"
     */
    private JButton createScreenshotButton()
    {
        screenShot = new JButton(new ImageIcon("resource/icons/png"
                + "/16x16/camera.png"));
        screenShot.setToolTipText("Screenshot");
        screenShot.addActionListener(new ScreenshotListener());
        setPortButtonValue = true;
        setPortButton();

        return screenShot;
    }
//---------------------------------------------------------------------------

    /** setPortButton()
    Aufgabe:
    ???
     */
    private void setPortButton()
    {
        screenShot.setEnabled(setPortButtonValue);
    }
//---------------------------------------------------------------------------

    /** createViewComboBox()
    Aufgabe:
    Erstellt DropDownMenü
     */
    private JComboBox createViewComboBox()
    {

        String[] item =
        {
            "3D-Ansicht", "X-Y Ebene", "Y-Z Ebene", "X-Z Ebene"
        };
        views = new JComboBox(item);
        views.setMaximumSize(new Dimension(110, 25));
        views.setMinimumSize(new Dimension(72, 25));
        views.setPreferredSize(new Dimension(77, 25));
        views.setToolTipText("Ansicht festlegen");
        views.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (views.getSelectedIndex() > 0)
                {
                    setViewPosition(views.getSelectedIndex());
                }
            }
        });
        views.setSelectedIndex(0);

        return views;
    }

//---------------------------------------------------------------------------
    private JButton createExitButton()
    {
        JButton Exit = new JButton(new ImageIcon("resource"
                + "/icons/png/16x16/exit1.png"));
        Exit.setToolTipText("Programm beenden");
        Exit.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                statusbar.getHeadTracker().stopHeadTracker();
                int returnVal = JOptionPane.showConfirmDialog(null, "Möchten Sie vorher Speichern?\nJa: Speichern und Beenden.\nNein: Beenden ohne Speichern");
                if (returnVal == 0)
                {
                    System.out.println("returnVal: " + returnVal);
                    if (LoadNStore.storeModel(getCurrentScene().getModel()) == 0)
                    {
                        System.exit(0);
                    }
                }
                if (returnVal == 1)
                {
                    System.exit(0);
                }

            }
        });
        return Exit;
    }

//---------------------------------------------------------------------------
    /** createLineButton
    Aufgabe:
    Erzeugung des Buttons "Verbindung hinzufügen"
     */
    private JButton createLineButton()
    {
        line = new JButton(new ImageIcon("resource/icons/png"
                + "/16x16/right.png"));
        line.setToolTipText("Verbindung hinzufügen");
        setLineButtonValue = true;
        setLineButton();
        line.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                /*            lines.saveFirstCube(pickTGroup.getParent().getParent().getName(),lineCount);
                sceneUniverse.saveLineCoordinates(sceneUniverse.getLinePointOne());
                lineFirstPoint = true;
                differentPortTest = pickTGroup.getName();
                sceneUniverse.setLinePointTwo(sceneUniverse.getLinePointOne());
                 */
                steuerung.connectToNextPort();
            }
        });
        return line;
    }
//---------------------------------------------------------------------------

    /** setLineButton()
    Aufgabe:
    ???
     */
    private void setLineButton()
    {
        line.setEnabled(setLineButtonValue);
        miLine.setEnabled(setLineButtonValue);
    }
//---------------------------------------------------------------------------

    /** createDeleteLinesButton()
    Aufgabe:
    Erzeugung des Buttons "Verbindungen löschen"
     */
    private JButton createDeleteLinesButton()
    {

        JButton deleteLine = new JButton(new ImageIcon("resource/icons"
                + "/png/16x16/no.png"));
        deleteLine.setToolTipText("alle Verbindungen entfernen");
        deleteLine.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {

                Connection.deleteAllConnections(steuerung.getUniverse().getSceneBranchGroup());
            }
        });
        return deleteLine;
    }
//---------------------------------------------------------------------------

    /** createFileMenu()
    Aufgabe:
    ???
     */
    private JMenu createFileMenu()
    {
        JMenu ret = new JMenu("Datei");
        ret.setMnemonic('D');
        JMenuItem mi;

        mi = new JMenuItem("Neu", 'N');
        setAltAccelerator(mi, 'N');
        mi.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "/_active__new1.png"));
        mi.addActionListener(new newProjectDialogListener());
        ret.add(mi);

        ret.addSeparator();

        mi = new JMenuItem("Laden", 'L');
        setAltAccelerator(mi, 'L');
        mi.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "/folderblue.png"));
        mi.addActionListener(new loadProjectDialogListener());
        ret.add(mi);

        mi = new JMenuItem("Speichern", 'P');
        setAltAccelerator(mi, 'P');
        mi.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "/_active__save1.png"));
        mi.addActionListener(new saveProjectDialogListener());
        ret.add(mi);

        mi = new JMenuItem("Screenshot", 'C');
        setAltAccelerator(mi, 'C');
        mi.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "/_active__camera.png"));
        mi.addActionListener(new ScreenshotListener());
        ret.add(mi);

        ret.addSeparator();

        miWiiConnect = new JMenuItem("WiiMote verbinden", 'V');
        setAltAccelerator(miWiiConnect, 'V');
        miWiiConnect.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (steuerung.connectWiimote())
                {
                } else
                {
                    JOptionPane.showMessageDialog(null, "Es konnte keine Verbindung zur WiiMote hergestellt werden", "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        ret.add(miWiiConnect);

        miWiiDisconnect = new JMenuItem("WiiMote trennen", 'T');
        setAltAccelerator(miWiiDisconnect, 'T');
        miWiiDisconnect.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                steuerung.disconnectWiimote();
            }
        });
        miWiiDisconnect.setEnabled(false);
        ret.add(miWiiDisconnect);

        miWiiEnable = new JMenuItem("WiiMote aktivieren", 'K');
        setAltAccelerator(miWiiEnable, 'K');
        miWiiEnable.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                steuerung.enableWiimote();
            }
        });
        miWiiEnable.setEnabled(false);
        ret.add(miWiiEnable);

        miWiiDisable = new JMenuItem("WiiMote deaktivieren", 'E');
        setAltAccelerator(miWiiDisable, 'E');
        miWiiDisable.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                steuerung.disableWiimote();
            }
        });
        miWiiDisable.setEnabled(false);
        ret.add(miWiiDisable);
        ret.addSeparator();

        mi = new JMenuItem("Beenden", 'B');
        setAltAccelerator(mi, 115);
        mi.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "/_active__exit1.png"));
        mi.addActionListener(new ActionListener()
        {

            public void actionPerformed(ActionEvent e)
            {
                final JOptionPane optionPane = new JOptionPane();
                int returnVal = optionPane.showConfirmDialog(null, "Möchten Sie vorher Speichern?\nJa: Speichern und Beenden.\nNein: Beenden ohne Speichern");
                statusbar.getHeadTracker().stopHeadTracker();
                if (returnVal == 0)
                {
                    if (LoadNStore.storeModel(getCurrentScene().getModel()) == 0)
                    {
                        System.exit(0);
                    }
                }
                if (returnVal == 1)
                {
                    System.exit(0);
                }
            }
        });
        ret.add(mi);

        return ret;
    }
//---------------------------------------------------------------------------

    /** createEditMenu()
    Aufgabe:
    Erzeugung des Menüpunktes "Bearbeiten" mitsamt aller Untermenüpunkte
     */
    private JMenu createEditMenu()
    {

        JMenu ret = new JMenu("Bearbeiten");
        ret.setMnemonic('B');


        JMenuItem mi;

		mi = new JMenuItem("Objekt erstellen");
        mi.setIcon(new ImageIcon("resource/icons/png/16x16/add1.png"));
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                changeCubeProperties();
            }
        });
        ret.add(mi);

        mi = new JMenuItem("Objekt löschen");
        mi.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "/remove.png"));
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteObj();
            }
        });
        ret.add(mi);


       
        miLine = new JMenuItem("Verbindung hinzufügen");
        miLine.setIcon(new ImageIcon("resource/icons/png/16x16"
                + "                   /yes.png"));
        miLine.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                steuerung.connectToNextPort();
            }
        });
        miLine.setEnabled(false);
        ret.add(miLine);

        miNewColor = new JMenuItem("Farbe Ãndern");
        miNewColor.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (IColorable.class.isInstance(steuerung.getSelected()))
                {
                    ColorDialog colDialog = new ColorDialog((IColorable) steuerung.getSelected());
                    colDialog.setVisible(true);
                }
            }
        });
        miNewColor.setEnabled(false);
        ret.add(miNewColor);

        miNewCaption = new JMenuItem("Beschriftung Ãndern");
        miNewCaption.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (INamable.class.isInstance(steuerung.getSelected()))
                {
                    CaptionDialog capDialog = new CaptionDialog((INamable) steuerung.getSelected());
                    capDialog.setVisible(true);
                }
            }
        });
        miNewCaption.setEnabled(false);
        ret.add(miNewCaption);

        miNewScale = new JMenuItem("Größe Ändern");
        miNewScale.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (IScalable.class.isInstance(steuerung.getSelected()))
                {
                    ScaleDialog scalDialog = new ScaleDialog((IScalable) steuerung.getSelected());
                    scalDialog.setVisible(true);
                }
            }
        });
        miNewScale.setEnabled(false);
        ret.add(miNewScale);

        miDelete = new JMenuItem("löschen");
        miDelete.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                deleteObj();
            }
        });
        miDelete.setEnabled(false);
        ret.add(miDelete);

        miSetPosition = new JMenuItem("Position Festlegen");
        miSetPosition.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {

                if (IPositionable.class.isInstance(steuerung.getSelected()))
                {
                    PositionDialog posDialog = new PositionDialog((IPositionable) steuerung.getSelected());
                    posDialog.setVisible(true);
                }

            }
        });
        miSetPosition.setEnabled(false);
        ret.add(miSetPosition);

        mi = new JMenuItem("alle Verbindungen entfernen");
        mi.setIcon(new ImageIcon("resource/icons/png/16x16/no.png"));
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                Connection.deleteAllConnections(steuerung.getUniverse().getSceneBranchGroup());
            }
        });
        ret.add(mi);

        return ret;
    } // createEditMenu
//---------------------------------------------------------------------------

    /** createViewMenu
    Aufgabe:
    Erzeugt ein Menü, mit welchem die Ansichten der 3D-Darstellung
    ausgewählt werden können
     */
    private JMenu createViewMenu()
    {
        JMenu ret = new JMenu("Ansicht");
        ret.setMnemonic('A');
        JMenuItem mi;

        mi = new JMenuItem("X-Y Ebene");
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setViewPosition(1);
            }
        });
        ret.add(mi);

        mi = new JMenuItem("Y-Z Ebene");
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setViewPosition(2);
            }
        });
        ret.add(mi);

        mi = new JMenuItem("X-Z Ebene");
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                setViewPosition(3);
            }
        });
        ret.add(mi);

        mi = new JMenuItem("Fullscreen", 'F');
        setAltAccelerator(mi, 'F');
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                changeFullscreen();
            }
        });
        ret.add(mi);


        return ret;
    }// createViewMenu
//---------------------------------------------------------------------------

    /** setCtrlAccelerator()
    Aufgabe:
    ErmÃ¶glicht Benutzung der Strg-Taste in Verbund mit Menüs
     */
    private void setCtrlAccelerator(JMenuItem mi, char acc)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(acc, Event.CTRL_MASK);
        mi.setAccelerator(ks);
    }
//---------------------------------------------------------------------------

    /** setCtrlAccelerator()
    Aufgabe:
    Ermöglicht Benutzung der Strg-Taste in Verbund mit Menüs
     */
    private void setAltAccelerator(JMenuItem mi, int acc)
    {
        KeyStroke ks = KeyStroke.getKeyStroke(acc, Event.ALT_MASK);
        mi.setAccelerator(ks);
    }
//---------------------------------------------------------------------------

    /** createInfoMenu()
    Aufgabe:
    Erzeugung des Info-Menüpunktes
     */
    private JMenu createInfoMenu()
    {
        JMenu ret = new JMenu("Info");
        ret.setMnemonic('I');
        JMenuItem mi;

        mi = new JMenuItem("Version", 'O');
        setAltAccelerator(mi, 'O');
        mi.setIcon(new ImageIcon("resource/icons/png/16x16/logo.png"));
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
            	JOptionPane.showMessageDialog(null, "Version 2.0\nby "
                        + "Lange, Mauler, Rönsch, Schönfeld, Tristram, "
                        + "Weigelt, Weiß, Wengefeld", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        ret.add(mi);

        return ret;
    }

    /**
     *
     * @return
     */
    private JMenu createSimMenu()
    {
        JMenu ret = new JMenu("Simulation");
        ret.setMnemonic('S');
        JMenuItem mi;


        mi = new JMenuItem("Init", 'I');
        setAltAccelerator(mi, 'I');
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (m_sim != null)
                {
                    m_sim.delete();
                }

                m_errorEditor.setActiveModelElement(null);
                m_sim = new Simulator();

                if(!m_sim.Init(getCurrentScene().getModel()))
                    m_errorEditor.setActiveModelElement(m_sim.getErrors());
            }
        });
        ret.add(mi);

        mi = new JMenuItem("Start", 'S');
        setAltAccelerator(mi, 'S');
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (m_sim == null)
                {
                    return;
                }
                m_sim.run();
            }
        });
        ret.add(mi);

        mi = new JMenuItem("Step", 'E');
        setAltAccelerator(mi, 'E');
        mi.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (m_sim == null)
                {
                    return;
                }
                m_sim.step();
            }
        });
        //ret.add(mi);


        return ret;
    }

//---------------------------------------------------------------------------
    /** createMenuBar()
    Aufgabe:
    Erzeugung der Menüleiste
     */
    private JMenuBar createMenuBar()
    {
        menubar = new JMenuBar();
        menubar.add(createFileMenu());
        menubar.add(createEditMenu());
        menubar.add(createViewMenu());
        menubar.add(createInfoMenu());

        menubar.add(createSimMenu());

        return menubar;
    }

    public JMenuBar getMenuBar()
    {
        return menubar;
    }
//---------------------------------------------------------------------------

    //erzeugt die Statusleiste
    private StatusBar createStatusBar() {
      statusbar = new StatusBar(this);
      return statusbar;
    }

    public StatusBar getStatusBar() {
      return statusbar;
    }

    public void setStatusBar(float percentage) {
      statusbar.setTrackingLabel(percentage);
  }
//---------------------------------------------------------------------------

    /** createToolbar()
    Aufgabe:
    Erzeugung der Tool-Bar unter der Menüleiste
     */
    private JToolBar createToolBar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(createNewProjectButton());
        toolbar.addSeparator();
        toolbar.add(createObjCreateButton());
        toolbar.add(createObjDeleteButton());
        toolbar.addSeparator();
        toolbar.add(createScreenshotButton());
        toolbar.addSeparator();
        toolbar.add(createLineButton());
        toolbar.add(createDeleteLinesButton());
        toolbar.addSeparator();
        toolbar.add(createViewComboBox());
        toolbar.addSeparator();
        toolbar.add(createScaleCube());
        toolbar.add(createExitButton());
        toolbar.addSeparator();
        toolbar.setRollover(true); //setzt Fokus bei Buttons
        toolbar.setPreferredSize(new Dimension(600, 45));

        return toolbar;
    }

    public JToolBar getToolBar() {
      return toolbar;
    }

//---------------------------------------------------------------------------
    /** createPortNumberPopupOkButton()
    Aufgabe:
    Erzeugung des "OK"-Buttons im PopupMenü für die Anzahl der Ports
     */
    private JButton createPortNumberPopupOkButton()
    {
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new portNumberOkButtonListener());

        return okButton;
    }
//---------------------------------------------------------------------------

    /** createTree()
    Aufgabe:
    Erzeugung der Baumstruktur für die Objekt-Library
     */
    private JTree createTree() {
        tree = new LibraryTree(createTreeNode(), this);



        //tree.setPreferredSize(new Dimension(150, 150));
        //tree.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        return tree;
    }
    
    public JTree getTree() {
        return tree;
    }
//---------------------------------------------------------------------------

    /** createTreeNode()
    Aufgabe:
    Fügt ein neues Element in die Baumstruktur der Objekt-Library ein
     */
    private DefaultMutableTreeNode createTreeNode()
    {

        Library lib = Library.getInstance();



        return lib.getRootNode();
    }

    public JTree getObjectTree()
    {
        return tree;
    }
//---------------------------------------------------------------------------

    /** createScaleCube
    Aufgabe:
    Erzeugung des Schiebereglers zum Skalieren des Würfelobjektes
     */
    private JSlider createScaleCube()
    {
        scale = new JSlider(1, 10, 1);
        scale.setPaintTicks(true);
        scale.setMajorTickSpacing(9);
        scale.setMinorTickSpacing(1);
        scale.setPaintTrack(true);
        scale.setPaintLabels(true);
        scale.setSnapToTicks(true);
        scale.setMinimumSize(new Dimension(50, 45));
        scale.setMaximumSize(new Dimension(200, 45));
        scale.setPreferredSize(new Dimension(50, 45));
        scale.addChangeListener(new scaleListener());
        scale.setVisible(false);

        return scale;
    }
//---------------------------------------------------------------------------



    //---------------------------------------------------------------------------
    /**
     * Wenn sich das selektierte Objekt ändert (bzw. dieses abgewählt wurde),
     * so wird diese Methode als Nachricht von der Steuerungsklasse aufgerufen.
     * Somit ist eine Reaktion auf das Anwaählen und Abwaählen von Objekten möglich.
     * @param e Das neue selektierte Objekt
     */
    /*@Override
    public void selectionChanged(SelectionEvent e)
    {
        line.setEnabled(IGraphicPort.class.isInstance(e.getSelectedObject()));
        miLine.setEnabled(IGraphicPort.class.isInstance(e.getSelectedObject()));
        miNewColor.setEnabled(IColorable.class.isInstance(e.getSelectedObject()));
        miNewCaption.setEnabled(INamable.class.isInstance(e.getSelectedObject()));
        miNewScale.setEnabled(IScalable.class.isInstance(e.getSelectedObject()));
        miDelete.setEnabled(IGraphicElement.class.isInstance(e.getSelectedObject()));
        miSetPosition.setEnabled(IPositionable.class.isInstance(e.getSelectedObject()));
        statusbar.setElement(e);
    } */

//---------------------------------------------------------------------------
    /** Klasse
    newProjectDialogListener
    Aufgabe:
    Dialogfenster beim Erstellen eines neuen Projektes
     */
    class newProjectDialogListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            final JOptionPane optionPane = new JOptionPane("Wollen sie "
                    + "wirklich neu beginnen\n" + "ACHTUNG!\n" + "das "
                    + "aktuelle Projekt wird hiermit geloescht",
                    JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
            final JDialog dialog = new JDialog(newProjectDialog, "Treffen"
                    + " sie eine Auswahl", true);
            dialog.setContentPane(optionPane);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            optionPane.addPropertyChangeListener(new PropertyChangeListener()
            {

                @Override
                public void propertyChange(PropertyChangeEvent e)
                {
                    String prop = e.getPropertyName();
                    if (dialog.isVisible() && (e.getSource() == optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop)))
                    {
                        dialog.setVisible(false);
                    } // if
                } // propertyChange
            } // propertyChangeListener
                    );
            dialog.pack();
            dialog.setLocationRelativeTo(newProjectDialog);
            dialog.setVisible(true);

            int value = ((Integer) optionPane.getValue()).intValue();
            if (value == JOptionPane.YES_OPTION)
            {
                steuerung.universe.clear();
                setProject("Neues Projekt");
                //addScene(); wenn mehrere Tabs verwendet werden ..  ist aber noch fehlerbehaftet!

            } else
            {
                if (value == JOptionPane.NO_OPTION)
                {
                } else
                {
                }
            }
        }
    } // newProjectDialogListener
//---------------------------------------------------------------------------

    /** Klasse
    loadProjectDialogListener
    Aufgabe:
    Dialogfenster beim Laden eines neuen Projektes
     */
    class loadProjectDialogListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            int returnVal = JOptionPane.showConfirmDialog(null, "Beim Laden eines alten "
                    + "Projektes wird das Jetztige gelöscht", "Laden", JOptionPane.YES_NO_OPTION);
            if (returnVal == JOptionPane.YES_OPTION)
            {
                try
                {
                    //addScene();


                    Model newModel = new Model(getCurrentScene().getSceneUniverse());
                    if (LoadNStore.loadModel(newModel) == 0)
                    {
                        getCurrentScene().setModel(newModel);
                        m_propertyEditor.setModel(getCurrentScene().getModel());
                        //setProject(newModel.getTitle());
                    }
                } catch (Exception ex)
                {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    } // loadProjectDialogListener

//---------------------------------------------------------------------------
    /** Klasse
    loadProjectDialogListener
    Aufgabe:
    Dialogfenster beim Speichern eines neuen Projektes
     */
    class saveProjectDialogListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            LoadNStore.storeModel(getCurrentScene().getModel());
            setTitleAt(getSelectedIndex(), getCurrentScene().getModel().getTitle());
        }
    } // saveProjectDialogListener


//---------------------------------------------------------------------------
    /** Klasse
    ScreenshotListener
    Aufgabe:
    Erzeugt ein Sreecshot
     */
    class ScreenshotListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            LoadNStore.screenshot(getCurrentScene());
        }
    }
//---------------------------------------------------------------------------

    /** Klasse
    portNumberOkButtonListener
    Aufgabe: ???
    Originalkommentar: Kontrolliert die Bestätigung des OK Buttons der Port Nummern
     */
    class portNumberOkButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
//            sceneUniverse.createPort(bgroup,Integer.valueOf(inputPortNumberSpinner.
//                    getValue().toString()).intValue(),Integer.valueOf(outputPortNumberSpinner.
//                    getValue().toString()).intValue(),pickTGroup);
//            inputPortNumberSpinner.getModel().setValue(0);
//            outputPortNumberSpinner.getModel().setValue(0);
//
//            portNumberPopup.setVisible(false);
        }
    }
//---------------------------------------------------------------------------

    /** Klasse
    scaleListener
    Aufgabe: Kontrolliert die Werte des Skalierungsschiebereglers
     */
    class scaleListener implements ChangeListener
    {
        // scaleListener an neue Cube-Klasse anpassen

        @Override
        public void stateChanged(ChangeEvent e)
        {
            /*            if(objMarked==true){
            float q;
            Transform3D tempT3D=new Transform3D();
            Transform3D t3d=new Transform3D();
            t=((JSlider) e.getSource()).getValue();

            pickTGroup.getTransform(t3d);
            q =(float) t3d.getScale();
            float fac = t/q;

            //Transformations-Matrix vom markierten Objekt
            matr=new Matrix4f(fac,0,0,0,0,fac,0,0,0,0,fac,0,0,0,0,1);

            tempT3D.set(matr);
            t3d.mul(tempT3D);
            pickTGroup.setTransform(t3d);
            }*/
        }
    }

//---------------------------------------------------------------------------
    public void openContextMenu(IPickable selected, int x, int y) {
      openContextMenu(selected, x, y, getSelectedComponent());
    }
    
    /**
     * Fordert die Gui dazu auf, das Kontextmenü für das angegebene Objekt zu öffnen
     * @param selected Das Objekt, dessen Kontextmenü geöffnet werden soll
     */
    public void openContextMenu(IPickable selected, int x, int y, Component c)
    {
        

        if (selected instanceof IGraphicElement)
        {
            PopupMenue popup = new PopupMenue((IGraphic) selected);
            popup.show(c, selected, x, y, steuerung);
            System.out.println("Kontextmenü eines Würfels geöffnet");
        } else if (selected instanceof IGraphicPort)
        {
            // KontextMenü eines Ports wurde geöffnet
            PortPopup pp = new PortPopup();
            pp.show(c, selected, x, y);
            System.out.println("KontextMenü eines Ports geöffnet");
        } else if (selected instanceof IGraphicInflexPoint)
        {
            Inflexpopup Ipopup = new Inflexpopup();
            // KontextMenü eines Knickpunktes wurde geöffnet
            Ipopup.show(c, selected, x, y);
            System.out.println("KontextMenü eines Knickpunktes geöffnet");
        } else
        {
            // KontextMenü für den Hintergrund? (Ansonsten entfÃ¤llt letztes else)
            System.out.println("KontextMenü des Hintergrundes geöffnet");
        }
    }

    /**
     * Diese Methode wird aufgerufen, sobald sich der Verbindungs-Zustand der
     * Wiimote ändert, sodass das Menü entsprechend angepasst werden kann.
     * @param connected Der neue Zustand, ob eine Wiimote verbunden ist
     * @param enabled Der neue Zustand, ob die Wiimote aktiviert ist
     */
    public void notifyWiimoteStateChanged(boolean connected, boolean enabled)
    {
        miWiiConnect.setEnabled(!connected);
        miWiiDisconnect.setEnabled(connected);
        miWiiEnable.setEnabled(connected && !enabled);
        miWiiDisable.setEnabled(connected && enabled);
    }

//---------------------------------------------------------------------------
    /** setViewPosition(...)
    Aufgabe:
    Legt die Kamerapositionen für die festen Ansichten fest
     */
    public void setViewPosition(int view)
    {
        switch (view)
        {
            case 1:
                steuerung.setTranslation(new Vector3d(0, 1, 30));
                steuerung.setRotation(0, 0);
                break;
            case 2:
                steuerung.setTranslation(new Vector3d(30, 1, 0));
                steuerung.setRotation(0, Math.PI / 2);
                break;
            case 3:
                steuerung.setTranslation(new Vector3d(0, 30, 0));
                steuerung.setRotation(-Math.PI / 2, Math.PI);
        }
    }
//---------------------------------------------------------------------------

    /** changeFullscreen()
    Aufgabe:
    Wechselt zwischen Fullscreen und Normalen Modus
     */
    public void changeFullscreen()
    {
        fullscreenStatus = !fullscreenStatus;
        toolbar.setVisible(fullscreenStatus);
        tree.setVisible(fullscreenStatus);
    }

    @Override
    protected void processComponentEvent(ComponentEvent e)
    {
        switch (e.getID())
        {
            case ComponentEvent.COMPONENT_RESIZED:
                //Workaround: Fehler im Canvas3D verursacht das bei jedem resize die minsize auf die aktuelle grÃ¶ÃŸe gesetzt wird
                // dies zeile setzt nach jedem resize die minimale grÃ¶ÃŸe neu
                getCurrentScene().setMinimumSize(new Dimension(1, 1));
                break;
        }

    }

//---------------------------------------------------------------------------
    /**
    Main Methode
    @param args
    @throws java.lang.InterruptedException
    main(...)
    Aufgabe:
    Was eine Main-Funktion eben so alles macht *g*
     */
    public static void main(String[] args) throws InterruptedException
    {
        try
        {

            //JFrame mainWindow = new MainWindow(this);



            JPopupMenu.setDefaultLightWeightPopupEnabled(false);

            ToolTipManager ttm = ToolTipManager.sharedInstance();
            ttm.setLightWeightPopupEnabled(false);


            JFrame mainWindow = new MainWindow();

        } catch (Exception ex)
        {
            JOptionPane.showMessageDialog(null, "Es ist ein Fehler in der Anwendung aufgetreten!\n" + ex.getMessage(), "Simulator", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger("main").log(Level.SEVERE, null, ex);
        }
    }
//---------------------------------------------------------------------------
} // class GUI
//###########################################################################

