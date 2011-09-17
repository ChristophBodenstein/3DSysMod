package swp.steuerung;

import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.SimpleUniverse;

import swp.*;
import swp.gui.*;

/**
 *
 * This class is used to translate the results of the face detection to a translation of the camera.
 *
 */
public class HeadTracker implements InputDevice, Runnable {

    private Transform3D nominal = new Transform3D();
    private TransformGroup  targetTG;
    private PhysicalBody physicalBody;

    private Thread headTrackerThread;
    
    private Sensor headTrackingSensor[] = new Sensor[1];
    private int currentProcessingMode = DEMAND_DRIVEN;
    private SensorRead sensorRead = new SensorRead();
    private Transform3D newTransform = new Transform3D();

    private int faceRadius = 1;
    private int faceX = 1;
    private int faceY = 1;
    private int videoFrameSizeX = 1;
    private int videoFrameSizeY = 1;

    private float transformZ = 0;
    private float transformX = 0;
    private float transformY = 0;
    private float rotationX = 0;
    
    private boolean trackingEnabled = true;

    private float detectionSuccessRatio = 0;
    private int detectionSuccessRange = 20;
    private boolean detectionSuccessArray[];
    private int detectionSuccessArrayPosition = 0;
    private int detectionFailCount = detectionSuccessRange;
    private int detectionSuccessCount = 0;
    private int overallDetectionFailCount = 0;
    private int overallDetectionSuccessCount = 0;
    
    //private HeadPositionPanel headPositionPanel;

    private GUI gui;

    private native int detectFace();

    public HeadTracker(TransformGroup targetTG, PhysicalBody physicalBody, GUI gui) {
      this.gui = gui;
      detectionSuccessArray = new boolean[detectionSuccessRange];
      for(int i = 0; i < detectionSuccessRange; i++) detectionSuccessArray[i] = false;
      headTrackerThread = new Thread(this, "headTrackerThread");
      headTrackerThread.start();
      gui.statusbar.initTrackingLabel(this);
    }

    
    public void startThread() {
      headTrackerThread.start();
    }
    
    public boolean initialize() {
      headTrackingSensor[0] = new Sensor(this);
      return true;
    }

    public void run() {
      try{
    	 
        System.loadLibrary( "headtracker" );
        System.out.println(System.getProperty("java.library.path"));
      } catch(UnsatisfiedLinkError ule) {
        System.out.println("ERROR: " + ule);
      }
      catch(Exception ex)
      {
    	  System.out.println("ERROR: " + ex);
      }
      detectFace();
  
    }

    public void setFace(int radius, int centerX, int centerY) {
      if(!trackingEnabled) {
    	  System.out.println("trackingEnabled is not successful");
    	  return;
    	  }
      if(radius == 0) {
        //Es wurde kein Gesicht erkannt.
        setDetectionFailCount();
        gui.statusbar.setTrackingLabel(getDetectionSuccessRatio());
        System.out.println("Es wurde kein Gesicht erkannt");
        return;
      }
      System.out.println("FACE SET : " + radius + "; " + centerX + "; " + centerY);
      faceRadius = radius;
      faceX = centerX;
      faceY = centerY;
      setDetectionSuccessCount();
      gui.statusbar.setTrackingLabel(getDetectionSuccessRatio());
      
    }

    public void setVideoFrameSize(int sizeX, int sizeY) {
      videoFrameSizeX = sizeX;
      videoFrameSizeY = sizeY;
      //System.out.println("video frame size: " + sizeX + " : " + sizeY);
    }

//    public HeadPositionPanel getHeadPositionPanel() {
//      return headPositionPanel;
//    }
  
    private void setDetectionFailCount() {
      overallDetectionFailCount++;
      if(detectionSuccessArrayPosition == detectionSuccessRange) {
        detectionSuccessArrayPosition = 0;
      }
      if(detectionSuccessArray[detectionSuccessArrayPosition]) {
        detectionSuccessCount--;
        detectionFailCount++;
        detectionSuccessArray[detectionSuccessArrayPosition] = false;
      }
      detectionSuccessArrayPosition++;
    }

    private void setDetectionSuccessCount() {
      overallDetectionSuccessCount++;
      if(detectionSuccessArrayPosition == detectionSuccessRange) {
        detectionSuccessArrayPosition = 0;
      }
      if(!detectionSuccessArray[detectionSuccessArrayPosition]) {
        detectionSuccessCount++;
        detectionFailCount--;
        detectionSuccessArray[detectionSuccessArrayPosition] = true;
      }
      detectionSuccessArrayPosition++;
    }

    public float getDetectionSuccessRatio() {
      if(detectionSuccessCount == 0) return 0;
      return detectionSuccessCount*100/(detectionSuccessCount+detectionFailCount);
    }

    public float getDetectionOverallSuccessRatio() {
      if(overallDetectionSuccessCount == 0) return 0;
      return overallDetectionSuccessCount*100/(overallDetectionSuccessCount+overallDetectionFailCount);
    }

    public Sensor getSensor(int sensorIndex) {
      return headTrackingSensor[sensorIndex];
    }
  
    public int getSensorCount() {
      return headTrackingSensor.length;
    }
    
    public void setProcessingMode(int mode) {
      /* TODO:
      public static final int BLOCKING
      Signifies that the driver for a device is a blocking driver and that it should be scheduled for regular reads by Java 3D. A blocking driver is defined as a driver that can cause the thread accessing the driver (the Java 3D implementation thread calling the pollAndProcessInput method) to block while the data is being accessed from the driver.

      public static final int NON_BLOCKING
      Signifies that the driver for a device is a non-blocking driver and that it should be scheduled for regular reads by Java 3D. A non-blocking driver is defined as a driver that does not cause the calling thread to block while data is being retrieved from the driver. If no data is available from the device, pollAndProcessInput should return without updating the sensor read value.

      public static final int DEMAND_DRIVEN
      Signifies that the Java 3D implementation should not schedule regular reads on the sensors of this device; the Java 3D implementation will only call pollAndProcessInput when one of the device's sensors' getRead methods is called. A DEMAND_DRIVEN driver must always provide the current value of the sensor on demand whenever pollAndProcessInput is called. This means that DEMAND_DRIVEN drivers are non-blocking by definition.
      */
    }
  
    public int getProcessingMode() {
      return currentProcessingMode;
    }

    public void close() {
      //kill thread etc.
      trackingEnabled = false;
      gui.statusbar.setTrackingLabel(-1);
    }

    public void start() {
      System.out.println("activating headtracker");
      trackingEnabled = true;
    }
    
    public void stopHeadTracker() {
      detectFace();
    }

    public void processStreamInput() {
      //not used
    }
    
    public void pollAndProcessInput() {
      //transformation
      if(!trackingEnabled) return;
      sensorRead.setTime(System.currentTimeMillis());
      Transform3D t3D = new Transform3D();
      Transform3D rot = new Transform3D();
      float scal = 0.001f;
      float x = (((faceX-500)*scal) - transformX);
      transformX = transformX + x;

      float y = 0;
      float z = 0;
      t3D.setTranslation(new Vector3f(x, y, z));
      newTransform.mul(t3D);
      y = (((faceY-500)*scal) - transformY);
      transformY = transformY + y;
      rot.setTranslation(new Vector3f(0, y, 0));    
      newTransform.mul(rot);

      sensorRead.set(newTransform);
      headTrackingSensor[0].setNextSensorRead(sensorRead);
    }
    
    public void setNominalPositionAndOrientation() {
      //This method sets the device's current position and orientation as the devices nominal position and orientation (establish its reference frame relative to the "Tracker base" reference frame).
      sensorRead.setTime( System.currentTimeMillis() );
      //setting noimalPosition and Orientation to identity
      sensorRead.set( new Transform3D());
      headTrackingSensor[0].setNextSensorRead( sensorRead );

    }
    public void setRotationX() {
        sensorRead.get(newTransform);
        Transform3D t = new Transform3D();
        //t.rotX(Math.PI/36);
        newTransform.mul(t);
    }

    public void setRotationY() {
        sensorRead.get(newTransform);
        Transform3D t = new Transform3D();
        //t.rotY(Math.PI/36);
        newTransform.mul(t);
    }

    public void setRotationZ() {
        sensorRead.get(newTransform);
        Transform3D t = new Transform3D();
        //t.rotZ(Math.PI/36);
        newTransform.mul(t);
    }

    public void setTranslationX() {
        sensorRead.get(newTransform);
        Transform3D t = new Transform3D();
        t.setTranslation(new Vector3d(0.1*faceX, 0.0,0.0));
        newTransform.mul(t);
    }

    public void setTranslationY() {
        sensorRead.get(newTransform);
        Transform3D t = new Transform3D();
        //t.setTranslation(new Vector3d(0.0, 0.1,0.0));
        //t.rotY(Math.PI/36);
        newTransform.mul(t);
    }

    public void setTranslationZ() {
        sensorRead.get(newTransform);
        Transform3D t = new Transform3D();
        //t.setTranslation(new Vector3d(0.0, 0.0,0.1));
        //newTransform.mul(t);
    }
}
