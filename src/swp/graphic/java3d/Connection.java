package swp.graphic.java3d;

import java.util.Enumeration;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.j3d.*;
import javax.swing.JOptionPane;
import swp.graphic.IGraphicConnection;
import swp.graphic.IGraphicPort;
import swp.model.IModelElement;
import swp.model.ModelConnection;

/**
 * Klasse Connection
 * @author Martin & David
 */
public class Connection extends BranchGroup implements IGraphicConnection
{
    // Referenzen für die beiden Ports, welche eine Verbindung eingehen sollen
    private Port m_p1;
    private Port m_p2;

    private LinkedList<InflexPoint> m_inflPList = new LinkedList<InflexPoint>();

    private ModelConnection m_ref = null;
    /**
     * Erzeugt eine einfache Branchgroup. Dient nur zum Aufrufen der Methoden
     * in anderen Klassen.
     */
    public Connection()
    {
        super();
    }


    /**
     * Erzeugt eine Verbindung zwischen 2 verschiedenen Ports.
     * Die beiden Ports müssen von zwei verschiedenen Cubes und verschiedenen
     * Typs sein.
     *
     * @param port1 - der erste Port
     * @param port2 - der zweite Port
     * @param parent - die Branchgroup, an welche die Connection anganhangen wird
     */
    public Connection(IGraphicPort port1, IGraphicPort port2, BranchGroup parent)
    {
        super();
        this.setCapability(ALLOW_DETACH);
        this.setCapability(ALLOW_CHILDREN_EXTEND);
        this.setCapability(ALLOW_CHILDREN_READ);
        this.setCapability(ALLOW_CHILDREN_WRITE);
//		this.setCapability(ALLOW_PARENT_READ);

        // Ist der erste Cube der selbe wie der erste?
        if (port1.getCube() == port2.getCube())
        {
            // Ja, dann
            // TODO: Fehlermeldung wegen identischem Würfel
            JOptionPane.showMessageDialog(null, "Es müssen zwei verschiedene Würfel gewählt werden", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        } // nein, dann: Ist der Porttyp der gleiche?
        else if (port1.getType() == port2.getType())
        {
            // ja, dann
            // TODO: Fehlermeldung wegen identischem Porttyp
            JOptionPane.showMessageDialog(null, "Es muss ein Inport und ein Output Port gewählt werden", "ERROR",
                    JOptionPane.ERROR_MESSAGE);
        } else
        {
            // die Cubes sind zwei verschiedene und die Ports sind
            // unterschiedlichen Typs



            // Referenzen auf Ports speichern
            this.m_p1 = (Port) port1;
            this.m_p2 = (Port) port2;

            

            port1.addConnection(this);
            port2.addConnection(this);

            // Connection an den Szenegraph anhängen
            parent.addChild(this);

            // Linie erzeugen
            InflexPoint infPoint = InflexPoint.createLine(this.m_p1, this.m_p2, this);
            this.addChild(infPoint);
            
            m_inflPList.add(infPoint);
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Connection erzeugt!");

        }

    } // Connection -----------------------------------------------------------

    public void addInflexPoint(InflexPoint point)
    {
        m_inflPList.add(point);
        this.addChild(point);

    }



    /**
     * Löscht eine bestehende Verbindung.
     */
    public void deleteConnection()
    {
        // die beiden Listen der attachedLines leeren
        this.removeChild(ALLOW_DETACH);
    } //-----------------------------------------------------------------------

    /**
     * Löscht alle bestehenden Verbindungen.
     * @param parent - die Branchgroup, an der alle Verbindungen hängen
     */
    public static void deleteAllConnections(BranchGroup parent)
    {
        // den Szenengraph nach Connections durchsuchen
        Enumeration en = parent.getAllChildren();
        Node n;

        while (en.hasMoreElements())
        {
            n = (Node) en.nextElement();
            if (n instanceof Connection)
            {
                
                ((Connection) n).delete();
            }
        }
    } // deleteAllConnections -------------------------------------------------




    /**
     * Gibt den ersten Port der Verbindung zurück.
     * @return
     */
    public Port getFirstPort()
    {
        return this.m_p1;
    }

    /**
     * Gibt den zweiten Port der Verbindung zurück.
     * @return
     */
    public Port getSecondPort()
    {
        return this.m_p2;
    }

    public void delete()
    {
        m_p1.removeConnection(this);
        m_p2.removeConnection(this);

        for(int i = 0; i<m_inflPList.size();i++)
        {
            m_inflPList.get(i).delete();
        }

        if (m_ref != null)
        {
            m_ref.delete();
            m_ref = null;
        }
        Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Connection gelöscht!");

        this.detach();

    }

    @Override
    public void update(IGraphicPort port)
    {
        //Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Connection Update!");
        for(int i = 0; i<m_inflPList.size();i++)
        {
            m_inflPList.get(i).update();
        }
    }

    @Override
    public void setModelRef(IModelElement element)
    {
        if (ModelConnection.class.isInstance(element))
        {
            m_ref = (ModelConnection) element;
        }
    }

    @Override
    public IModelElement getModelRef()
    {
        return m_ref;
    }

    @Override
    public LinkedList<InflexPoint> getPointList()
    {
        return m_inflPList;
    }

  


    
} // class Connection #########################################################

