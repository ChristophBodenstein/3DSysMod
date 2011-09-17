package swp.steuerung;


import java.awt.AWTEvent;
import java.awt.event.*;
import java.util.Enumeration;
import javax.media.j3d.*;
import javax.vecmath.*;
import swp.graphic.IPickable;
import swp.graphic.IPositionable;

public class MouseControl extends Behavior
{
    // Skalierungsfaktor für die Rotation

    private final double ROTATION_SCALE = 2.5;
    private Steuerung steuerung;
    private WakeupOr wakeups;
    // Aktuell gedrückte Maustaste (wird für MouseDragged benötigt)
    private int pressedButton = 0;
    // Werte beim Start einer Rotation (Drücken der rechten Maustaste)
    private int startX, startY;
    private double startRotX, startRotY;

    /**
     * Konstruktor der Maus-Steuerungsklasse
     * @param steuerung Das übergeordnete Steuerungsobjekt
     */
    public MouseControl(Steuerung steuerung)
    {
        this.steuerung = steuerung;
    }

    @Override
    public void initialize()
    {
        // Kriterien für den Behavior festlegen
        WakeupCriterion[] warray =
        {
            new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED),
            new WakeupOnAWTEvent(MouseEvent.MOUSE_RELEASED),
            new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED),
            new WakeupOnAWTEvent(MouseEvent.MOUSE_CLICKED),
            new WakeupOnAWTEvent(MouseEvent.MOUSE_WHEEL),
        };
        wakeups = new WakeupOr(warray);
        wakeupOn(wakeups);
    }

    @Override
    public void processStimulus(Enumeration criteria)
    {
        AWTEvent[] awt;
        WakeupCriterion genericEvt;

        // Alle auslösenden Events durchgehen und verarbeiten
        while (criteria.hasMoreElements())
        {
            genericEvt = (WakeupCriterion) criteria.nextElement();
            if (genericEvt instanceof WakeupOnAWTEvent)
            {
                awt = ((WakeupOnAWTEvent) genericEvt).getAWTEvent();
                for (int i = 0; i < awt.length; i++)
                {
                    processAWTEvent(awt[i]);
                }
            }
        }

        // Kriterien erneuern
        wakeupOn(wakeups);
    }

    /**
     * Verarbeitet das übergebene Event und realisiert letztendlich die Steuerung
     * @param event Das Event
     */
    private void processAWTEvent(AWTEvent event)
    {
        if (!(event instanceof MouseEvent))
        {
            // Sollte niemals eintreten, da nur auf MouseEvents reagiert werden sollte
            return;
        }

        MouseEvent mouse = (MouseEvent) event;
        switch (mouse.getID())
        {
            case MouseEvent.MOUSE_PRESSED:
                pressedButton = mouse.getButton();
                steuerung.gui.setFocusToCanvas3D();
                if (pressedButton == MouseEvent.BUTTON1)
                {
                    // Picking des Objektes unterhalb des Mauszeigers
                    steuerung.pickObject(mouse.getX(), mouse.getY());

                } else if (pressedButton == MouseEvent.BUTTON3)
                {
                    // Aktuelle Mausposition und Rotation merken, da eventuell
                    // gedreht werden wird (Start der Drehung)
                    startX = mouse.getX();
                    startY = mouse.getY();
                    startRotX = steuerung.getRotationX();
                    startRotY = steuerung.getRotationY();
                }
                break;

            case MouseEvent.MOUSE_RELEASED:
                if (pressedButton == mouse.getButton())
                {
                    pressedButton = 0;
                }
                break;

            case MouseEvent.MOUSE_DRAGGED:
                // MouseDrag ist das Bewegen der Maus mit mind. einer gedrückten Maustaste.
                // Unglücklicherweise wird dabei die Maustaste nicht mitgeliefert,
                // sodass diese im MousePress bereits zwischengespeichert werden muss.
                if (pressedButton == MouseEvent.BUTTON1)
                {
                    // Linke Maustaste = Bewegen des aktuell selektierten Objektes
                    moveObject(mouse.getX(), mouse.getY());
                } else if (pressedButton == MouseEvent.BUTTON3)
                {
                    // Rechte Maustaste = Rotieren der Kamera
                    rotateCam(mouse.getX(), mouse.getY());
                }
                break;

            case MouseEvent.MOUSE_CLICKED:
                // MouseClick ist die Folge eines MousePress + MouseRelease,
                // ohne zwischenzeitliches Bewegen der Maus (dh. kein MouseDrag)
                if (mouse.getButton() == MouseEvent.BUTTON3)
                {
                    // Aufrufen des Kontextmenüs
                    IPickable pick = steuerung.pickObject(mouse.getX(), mouse.getY());
                    steuerung.getGui().openContextMenu(pick, mouse.getX(), mouse.getY());
                }
                break;

            case MouseEvent.MOUSE_WHEEL:
                MouseWheelEvent wheel = (MouseWheelEvent) mouse;
                changeObjectHeight(wheel.getWheelRotation());
                break;
        }
    }

    /**
     * Mit dieser Methode wird das Drehen der Kamera, abhängig von den übergebenen
     * Koordinaten (und den daraus entstehenden Differenzen), gedreht. Dabei
     * erfolgt die Drehung immer parallel zur X- oder Y-Achse, sodass ein
     * Rollen (Drehung um Z-Achse) nicht möglich ist.
     *
     * @param mouseX die aktuelle X-Koordinate der Maus
     * @param mouseY Die aktuelle Y-Koordinate der Maus
     */
    private void rotateCam(int mouseX, int mouseY)
    {
        int diffX = startX - mouseX;
        int diffY = startY - mouseY;
        double scale = ROTATION_SCALE / 1000;

        // Es ist zu beachten, dass eine Drehung um die X-Achse nach oben bzw. unten
        // dreht, entlang der Y-Achse nach links bzw. rechts. Deshalb wird die
        // X-Koordinate der Maus auf die Y-Rotation abgebildet und umgekehrt.
        steuerung.setRotation(startRotX + scale * diffY, startRotY + scale * diffX);
    }

    /**
     * Bewegt das aktuell selektierte Objekt innerhalb der X-Z-Ebene, falls es
     * sich dabei um einen Cube oder einen InfelexPoint handelt.
     * Die Y-Koordinate (Höhe) wird dabei nicht verändert.
     * Modifikation von http://archives.java.sun.com/cgi-bin/wa?A2=ind9910&L=java3d-interest&P=R20329
     * @param mouseX Die aktuelle X-Koordinate der Maus
     * @param mouseY Die aktuelle Y-Koordinate der Maus
     */
    private void moveObject(int mouseX, int mouseY)
    {
        IPickable pick = steuerung.getSelected();

        if (pick == null)
        {
            return;
        }

        if (IPositionable.class.isInstance(pick))
        {
            IPositionable posObj = (IPositionable) pick;

            Canvas3D c3d = steuerung.getUniverse().getCanvas();
            // imagePlateToVworld ist die Transformationsmatrix,
            // die die aktuelle ImagePlate (SichtEbene) in den 3D-Raum abbildet
            Transform3D imagePlateToVworld = new Transform3D();

            // vWorldPt ist der angeklickte Punkt in der ImagePlate-Ebene
            Point3d vWorldPt = new Point3d();
            c3d.getPixelLocationInImagePlate(mouseX, mouseY, vWorldPt);
            c3d.getImagePlateToVworld(imagePlateToVworld);
            imagePlateToVworld.transform(vWorldPt);

            // centerEyePt ist die aktuelle Position des "mittleren" Auges,
            // einem fiktiven Punkt zwischen dem linken und rechten Auge
            Point3d centerEyePt = new Point3d();
            c3d.getCenterEyeInImagePlate(centerEyePt);
            imagePlateToVworld.transform(centerEyePt);

            // Aktuelle Höhe des selektieten Objektes auslesen
            double currentY = posObj.getPosition().y;
            // Schnittpunkt mit der Ebene [y = currentY] ausrechnen
            double alpha = 0;
            if (vWorldPt.y != centerEyePt.y)
            {
                alpha = (centerEyePt.y - currentY) / (vWorldPt.y - centerEyePt.y);
            }
            Vector3d newPos = new Vector3d(
                    centerEyePt.x - alpha * (vWorldPt.x - centerEyePt.x),
                    currentY,
                    centerEyePt.z - alpha * (vWorldPt.z - centerEyePt.z));

            // Berechnete neue Position des selektierten Objektes setzen
            posObj.setPosition(newPos);
        }

    }

    /**
     * Ändert die Höhe des aktuell selektierten Objektes, sofern es sich dabei
     * nicht um einen Port handelt
     * @param wheelRotation Die Bewegung des Mausrades
     */
    private void changeObjectHeight(int wheelRotation)
    {
        IPickable pick = steuerung.getSelected();

        if (pick == null)
        {
            return;
        }

        if (IPositionable.class.isInstance(pick))
        {
            IPositionable posObj = (IPositionable) pick;

            Vector3d pos = posObj.getPosition();
            pos.y -= wheelRotation;
            posObj.setPosition(pos);
        }
    }
}
