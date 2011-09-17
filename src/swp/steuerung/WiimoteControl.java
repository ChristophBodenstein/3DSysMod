package swp.steuerung;




import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import javax.media.j3d.*;
import javax.vecmath.Vector3d;
import swp.graphic.IPickable;
import swp.graphic.IPositionable;
import swp.graphic.IScalable;

import wiiusej.*;
import wiiusej.wiiusejevents.physicalevents.*;
import wiiusej.wiiusejevents.utils.*;
import wiiusej.wiiusejevents.wiiuseapievents.*;

public class WiimoteControl extends Behavior implements WiimoteListener {
	// Das CLICK_TIMEOUT bestimmt, wie kurz ein Tastendruck (A oder B) sein muss,
	// um als Click ausgewertet zu werden (in Millisekunden). Außerdem bestimmt
	// er gleichzeitig, ab wann ein Tastendruck als Dragging betrachtet werden soll.

	private final static int CLICK_TIMEOUT = 200;
	// Das SCROLL_TIMEOUT bestimmt, wie viel Zeit vergehen muss, bis der Tastendruck
	// von Button 1 bzw. 2 wieder als Scrollen der Maus umgesetzt werden soll.
	private final static int SCROLL_TIMEOUT = 50;
	// Das WIMMOTE_TIMEOUT bestimmt, nach welcher Zeit die WiiMote als "verloren"
	// gezählt werden soll und der Gui dies mitgeteilt wird.
	// (Wichtig beim Ausschalten der WiiMote, um ein Disconnect zu erkennen.)
	private final static int WIIMOTE_TIMEOUT = 1000;
	// Skalierungsfaktoren für das Bewegen der Kamera
	private final static double SCALE_MOVE_X = 0.05;
	private final static double SCALE_MOVE_Y = 0.05;
	private final static double SCALE_MOVE_Z = 0.5;
	// Anzahl der Wiimote-Koordinaten, die in die Durchschnittsbildung einbezogenw erden sollen
	private final static int AVERAGE_COUNT = 16;
	private Steuerung steuerung;
	private Wiimote wiimote = null;
	private WakeupCriterion wakeupFrame, wakeupTime;
	private WakeupCriterion nextWakeup;
	// Robot für die Kontrolle des Mauszeigers via WiiMote
	private Robot robot = null;
	// Temporäres Deaktivieren der WiiMote
	private boolean enabled = false;
	private boolean inactivityState = false; // Animation im Deaktiviertem Zustand
	private long inactivityTime = 0;
	private long lastCall = 0;
	private long lastResponse = 0;
	// Verwaltung der Tastendrücke
	private static final int BUTTON_A = InputEvent.BUTTON1_MASK;
	private static final int BUTTON_B = InputEvent.BUTTON3_MASK;
	private long pressedA = 0, pressedB = 0;
	private int robotPressed = 0; // Aktuell durch den Robot gedrückte Maustaste
	private long lastScroll = 0; // Letzter ZEitpunkt des "Scrollens" (Button 1/2)
	// Werte für das Berechnen der Bewegung mittels WiiMote
	private Vector3d currentIR = new Vector3d(); // Rohdaten der IR-Sensoren
	private Vector3d startIR = null; // Rohdaten zu Beginn der Bewegung
	private Vector3d startPos = null; // Startposition des J3D-Raums
	private Vector3d[] pointArray;
	private Vector3d pointSum = new Vector3d();
	private int pointIndex = 0;

	/**
	 * Konstruktor für die Wiimote-Steuerungsklasse
	 * @param steuerung Das übergeordnete Steuerungsobjekt
	 */
	public WiimoteControl(Steuerung steuerung) {
		this.steuerung = steuerung;
		this.setEnable(false);

		// WiiMote-Koordinaten-Array initialisieren
		pointArray = new Vector3d[AVERAGE_COUNT];
		for (int i = pointArray.length - 1; i >= 0; --i) {
			pointArray[i] = new Vector3d(); // Alle Punkte 0 setzen
		}
	}

	/**
	 * Schaltet den WiiUseApiManager ab
	 */
	public void shutDown() {
		if (wiimote != null) {
			wiimote.disconnect();
		}
		WiiUseApiManager.shutdown();
	}

	/**
	 * Sucht nach WiiMotes, die mit dem Rechner via Bluetooth verbunden sind,
	 * und stellt die zuerst gefundene dem Programm zur Verfügung.
	 */
	public void connect() {
		if (wiimote != null) {
			return; // Bereits verbunden
		}
		// Vorhandene Wiimotes abfragen
		Wiimote[] wiimotes = WiiUseApiManager.getWiimotes(1, true);
		if (wiimotes.length > 0) {
			wiimote = wiimotes[0];

			// Zur Wiimote verbinden und die Einstellungen setzen
			if (wiimote != null) {
				lastResponse = 0;
				wiimote.addWiiMoteEventListeners(this);
				synchronConnect(); // Blockierend

				if (lastResponse == 0) {
					// Wir haben keine Antwort von der Wiimote bekommen
					wiimote = null;
				} else {
					Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
					wiimote.setVirtualResolution(resolution.width, resolution.height);
					wiimote.setOrientationThreshold(0.1f);
					wiimote.setSensorBarBelowScreen();
					wiimote.setTimeout((short) 20, (short) 1000);
					this.enable();
					this.setEnable(true);
					notifyGui();
				}
			}
		}
	}

	/**
	 * Beendet die Verbindung zur WiiMote.
	 */
	public void disconnect() {
		if (wiimote != null) {
			this.disable();
			wiimote.disconnect();
			wiimote.removeWiiMoteEventListeners(this);
			wiimote = null;

			// Der Behavior wird ohne Wiimote vorübergehend nicht benÃ¶tigt
			this.setEnable(false);
			notifyGui();
		}
	}

	/**
	 * Prüft, ob aktuell eine WiiMote verbunden ist.
	 * @return Das Ergebnis des Tests
	 */
	public boolean isConnected() {
		return wiimote != null;
	}

	/**
	 * Aktiviert die WiiMote (speziell deren Sensoren).
	 */
	public void enable() {
		// Nur wenn bereits eine WiiMote verbunden ist, diese auch aktivieren
		if (wiimote != null) {
			try {
				robot = new Robot();
			} catch (AWTException ex) {
			}

			wiimote.activateContinuous();
			wiimote.activateSmoothing();
			wiimote.activateIRTRacking();
			wiimote.activateMotionSensing();
			wiimote.setLeds(true, false, false, false);

			enabled = true;
			nextWakeup = wakeupFrame;
			notifyGui();
		}
	}

	/**
	 * Deaktiviert die WiiMote (speziell deren Sensoren).
	 */
	public void disable() {
		if (wiimote != null) {
			wiimote.deactivateIRTRacking();
			wiimote.deactivateMotionSensing();
			wiimote.deactivateSmoothing();
			wiimote.deactivateContinuous();
		}

		enabled = false;
		nextWakeup = wakeupTime;
		notifyGui();
	}

	/**
	 * Prüft, ob die verbundene WiiMote aktuell aktiviert ist.
	 * @return Das Ergebnis des Tests
	 */
	public boolean isEnabled() {
		return this.isConnected() && enabled;
	}

	/**
	 * Benachrichtigt die Gui, sobald sich der Zustand der WiiMote (verbunden,
	 * aktiviert) geändert hat.
	 */
	private void notifyGui() {
		steuerung.getGui().notifyWiimoteStateChanged(
				this.isConnected(),
				this.isEnabled());
	}

	/**
	 * Synchronisiert das Verbinden einer Wiimote.
	 */
	private synchronized void synchronConnect() {
		wiimote.getStatus();
		try {
			this.wait(WIIMOTE_TIMEOUT); // Maximal einmal das Timeout auf Wiimote warten
		} catch (InterruptedException ex) {
		}
	}

	/**
	 * Erneuert die letzte "Antwort" der Wiimote via Status-Event
	 */
	private synchronized void responseStatus() {
		lastResponse = System.currentTimeMillis();
		this.notifyAll();
	}

//---------- Behavior --------------------------------------------------------//
	@Override
	public void initialize() {
		wakeupFrame = new WakeupOnElapsedFrames(1);
		wakeupTime = new WakeupOnElapsedTime(100);
		wakeupOn(wakeupFrame);
		nextWakeup = wakeupFrame;
	}

	@Override
	public void processStimulus(Enumeration criteria) {
		if (enabled) {
			if (robotPressed == (BUTTON_A | BUTTON_B)) {
				moveCamera();
			}
		} else {
			disabledAnimation();
		}

		long time = System.currentTimeMillis();
		if (time - lastResponse > WIIMOTE_TIMEOUT) {
			// Wir haben die WiiMote wohl verloren...
			this.disconnect();
		} else if (time - lastCall > WIIMOTE_TIMEOUT / 10) {
			// Nachfragen, ob die WiiMote noch antwortet
			wiimote.getStatus();
			lastCall = time;
		}

		wakeupOn(nextWakeup);
	}

	/**
	 * Eine kleine Animation (Blinken einer LED), wenn die WiiMote deaktiviert ist
	 */
	private void disabledAnimation() {
		long time = System.currentTimeMillis();
		if (time > inactivityTime + 1000) {
			wiimote.setLeds(inactivityState, false, false, false);
			inactivityState = !inactivityState;
			inactivityTime = time;
		}
	}

	/**
	 * Bewegt die Kamera.
	 */
	private void moveCamera() {
		Vector3d diff = new Vector3d();
		diff.x = +SCALE_MOVE_X * (currentIR.x - startIR.x);
		diff.y = -SCALE_MOVE_Y * (currentIR.y - startIR.y);
		diff.z = +SCALE_MOVE_Z * (currentIR.z - startIR.z);
		steuerung.transformVector(diff);
		diff.add(startPos);
		steuerung.setTranslation(diff);
	}

//---------- WiiMote Event Listener ------------------------------------------//
	/**
	 * Diese Methode wird aufgerufen, sobald irgendeiner der Buttons der WiiMote
	 * gedrückt oder losgelassen wurde. Ãœber das Event kann abgefragt werden,
	 * was genau zum AuslÃ¶sen des Events geführt hat.
	 *
	 * @param event Das dazugehÃ¶rige Event
	 */
	@Override
	public void onButtonsEvent(WiimoteButtonsEvent event) {
		long time = System.currentTimeMillis();
		int press = 0, release = 0;

		// Die Ã„nderung des A-Knopfes feststellen
		if (event.isButtonAJustPressed()) {
			// Button A wurde gerade erst gedrückt
			pressedA = time;
		} else if (event.isButtonAHeld() && (time - pressedA > CLICK_TIMEOUT)) {
			// Button A wird bereits länger als CLICK_TIMEOUT gedrückt
			if ((robotPressed & BUTTON_A) == 0) {
				press |= BUTTON_A;
			}
		} else if (event.isButtonAJustReleased()) {
			// Button A wurde losgelassen
			if (time - pressedA < CLICK_TIMEOUT) {
				// Wir haben "nur" geklickt
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
			} else {
				release |= BUTTON_A;
			}
			pressedA = 0;
		}
		// Die Ã„nderungen von B feststellen
		if (event.isButtonBJustPressed()) {
			// Button B wurde gerade erst gedrückt
			pressedB = time;
		} else if (event.isButtonBHeld() && (time - pressedB > CLICK_TIMEOUT)) {
			// Button A wird bereits länger als CLICK_TIMEOUT gedrückt
			if ((robotPressed & BUTTON_B) == 0) {
				press |= BUTTON_B;
			}
		} else if (event.isButtonBJustReleased()) {
			// Button B wurde losgelassen
			if (time - pressedB < CLICK_TIMEOUT) {
				// Wir haben "nur" geklickt
				robot.mousePress(InputEvent.BUTTON3_MASK);
				robot.mouseRelease(InputEvent.BUTTON3_MASK);
			} else {
				release |= BUTTON_B;
			}
			pressedB = 0;
		}

		// Durch den Robot gedrückte Maustasten aktualisieren
		// Anmerkung: Werden sowohl A als auch B gedrückt gehalten, so lässt
		// der Robot beide Tasten los, um eine eigene Behandlung zu ermÃ¶glichen
		if (press != 0 || release != 0) {

			if ((robotPressed == (BUTTON_A | BUTTON_B)) && (release > 0)) {
				// Eine von den beiden gedrückten Tasten wurde losgelassen,
				// also muss der Robot die übrig bleibende wieder drücken
				robot.mousePress(robotPressed & ~release);
				startIR = null;
				startPos = null;
			} else if ((robotPressed | press) == (BUTTON_A | BUTTON_B)) {
				// Mit der hinzukommenden Taste sind nun beide Tasten gedrückt,
				// also lässt der Robot die aktuell gedrückte Taste los
				robot.mouseRelease(robotPressed);
				startIR = new Vector3d(currentIR);
				startPos = steuerung.getTranslation();
			} else if (press > 0) {
				// Eine einzelne Taste ist nun gedrückt, also diese mit dem
				// Robot drücken
				robot.mousePress(press);
			} else if (release > 0) {
				// Die einzelne Taste wurde losgelassen, also diese auch mit
				// dem Robot loslassen
				robot.mouseRelease(release);
			}
			robotPressed = (robotPressed | press) & ~release;
		}

		// Home-Button - Aktiviert bzw. Deaktiviert die WiiMote
		if (event.isButtonHomeJustPressed()) {
			if (enabled) {
				this.disable();
			} else {
				this.enable();
			}
		}

		// Steuerkreuz: Alternative Bewegung im Raum (entspricht WASD)
		if (event.isButtonUpJustPressed()) {
			robot.keyPress(KeyEvent.VK_W);
		} else if (event.isButtonUpJustReleased()) {
			robot.keyRelease(KeyEvent.VK_W);
		}
		if (event.isButtonDownJustPressed()) {
			robot.keyPress(KeyEvent.VK_S);
		} else if (event.isButtonDownJustReleased()) {
			robot.keyRelease(KeyEvent.VK_S);
		}
		if (event.isButtonLeftJustPressed()) {
			robot.keyPress(KeyEvent.VK_A);
		} else if (event.isButtonLeftJustReleased()) {
			robot.keyRelease(KeyEvent.VK_A);
		}
		if (event.isButtonRightJustPressed()) {
			robot.keyPress(KeyEvent.VK_D);
		} else if (event.isButtonRightJustReleased()) {
			robot.keyRelease(KeyEvent.VK_D);
		}



		IPickable selected = steuerung.getSelected();
		if (selected != null) {
			// Im Falle eines Würfels oder eines Knickpunktes die HÃ¶he mit 1/2 ändern
			if (IPositionable.class.isInstance(selected) && (time - lastScroll > SCROLL_TIMEOUT)) {
				if (event.isButtonOnePressed()) {
					robot.mouseWheel(-1);
					lastScroll = time;
				} else if (event.isButtonTwoPressed()) {
					robot.mouseWheel(1);
					lastScroll = time;
				}
			}

			// Im Falle eines Würfels die Skalierung mit +/- ändern
			if ( IScalable.class.isInstance(selected)) {
				IScalable cube = (IScalable) selected;
				int scale = cube.getScale();

				if (event.isButtonPlusPressed() && (scale < 100)) {
					cube.setScale(cube.getScale() + 1);
				} else if (event.isButtonMinusPressed() && (scale > 0)) {
					cube.setScale(cube.getScale() - 1);
				}
			}
		}

	}

	/**
	 * Diese Methode wird aufgerufen, sobald die WiiMote eine Veränderung
	 * der IR-Sensoren feststellt, dh die WiiMote bewegt wurde.
	 *
	 * @param event Das dazugehÃ¶rige Event
	 */
	@Override
	public void onIrEvent(IREvent event) {
		Vector3d point = new Vector3d(event.getX(), event.getY(), event.getZ());
		pointSum.sub(pointArray[pointIndex]);
		pointSum.add(point);
		pointArray[pointIndex] = point;
		pointIndex = ++pointIndex % AVERAGE_COUNT;

		currentIR = new Vector3d(pointSum);
		currentIR.scale(1. / AVERAGE_COUNT);
		robot.mouseMove(((Double) currentIR.x).intValue(), ((Double) currentIR.y).intValue());
	}

	/**
	 * Diese Methode wird aufgerufen, sobald ein Sensor eine Bewegung der WiiMote
	 * festgestellt hat (Bewegungssensoren, Gravitationssensoren).
	 *
	 * @param event Das dazugehÃ¶rige Event
	 */
	@Override
	public void onMotionSensingEvent(MotionSensingEvent event) {
	}

	/**
	 * Diese Methode wird aufgerufen, wann immer wiimote.getStatus() ausgeführt
	 * und von der WiiMote abgearbeitet wurde.
	 * @param event Das dazugehÃ¶rige Status-Event
	 */
	@Override
	public void onStatusEvent(StatusEvent event) {
		responseStatus();
	}

	/**
	 * Anmerkung:
	 * Unter Windows ist es nicht mÃ¶glich, auf ein Unterbrechen der Verbindung
	 * zu reagieren, da der Bluethooth-Stack diese Information nicht bereit stellt.
	 * Deshalb wird onDisconnectEvent() unter Windows niemals aufgerufen.
	 */
	@Override
	public void onDisconnectionEvent(DisconnectionEvent event) {
	}

	@Override
	public void onExpansionEvent(ExpansionEvent event) {
	}

	@Override
	public void onNunchukInsertedEvent(NunchukInsertedEvent event) {
	}

	@Override
	public void onNunchukRemovedEvent(NunchukRemovedEvent event) {
	}

	@Override
	public void onGuitarHeroInsertedEvent(GuitarHeroInsertedEvent event) {
	}

	@Override
	public void onGuitarHeroRemovedEvent(GuitarHeroRemovedEvent event) {
	}

	@Override
	public void onClassicControllerInsertedEvent(ClassicControllerInsertedEvent event) {
	}

	@Override
	public void onClassicControllerRemovedEvent(ClassicControllerRemovedEvent event) {
	}
}
