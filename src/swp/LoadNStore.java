package swp;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.vecmath.*;
import javax.swing.*;

import javax.imageio.ImageIO;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsContext3D;
import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.Raster;

import org.xml.sax.helpers.*;
import swp.model.Model;

/**
 * Laden/Speichern Klasse
 *
 * DefaultHandler wird vom Saxparser benötigt
 * @author Marco Lange, Patrick Rönsch
 */
public class LoadNStore extends DefaultHandler
{

    /**
     * Konstruktor
     * @param scene sceneUniverse
     */
    LoadNStore()
    {
        
    }

// ----------------------------------------------------------------------------
    /**
     * Wird aus der GUI aufgerufen wenn man den zuständigen Menüeintrag betätigt.
     * Löscht zuerst das ganze bisherige Gebilde und baut dann anhand der XML-Datei
     * und mit Hilfe des Saxparser alles neu.
     * @throws java.lang.Exception
     */
    static public int loadModel(Model model) throws Exception
    {
        JFileChooser filechooser = new JFileChooser();

        int returnVal = filechooser.showOpenDialog(null);

        if (returnVal == 0)
        {
            ModelLoader modLoader = new ModelLoader(filechooser.getSelectedFile());

            modLoader.load(model);


        }
        return returnVal;
    }

    /**
     * Löscht zuerst das ganze bisherige Gebilde und baut dann anhand der XML-Datei
     * und mit Hilfe des Saxparser alles neu.
     * @throws java.lang.Exception
     */
/*    static public int loadModel(String xmlFile) throws Exception
    {
        System.out.println(xmlFile);
        //JFileChooser filechooser = new JFileChooser();

        //int returnVal = filechooser.showOpenDialog(null);
        Model newModel = new Model(sceneUniverse);
        try {
            ModelLoader modLoader = new ModelLoader(new File(xmlFile));

            modLoader.load(model);
        } catch(Exception e) {

        }



        //return returnVal;

        return 0;
    }
  */
// ---------------------------------------------------------------------------
    /**
     * Wird aus der GUI aufgerufen wenn man den zuständigen Menüeintrag betätigt.
     * Speichert alle benötigten Daten in ein XML-File mit einer von uns vorgegeben
     * Formatierung.
     */
    static public int storeModel(Model model)
    {
        FileOutputStream outputobject;
        PrintStream printobject;

        JFileChooser filechooser = new JFileChooser();
        filechooser.setSelectedFile(new File (model.getTitle()));
        filechooser.setDialogTitle("Modell speichern");

        int returnVal = filechooser.showSaveDialog(null);
        if(returnVal!=0)
                return returnVal;

        try
        {
            File f = filechooser.getSelectedFile();
            model.setTitle(f.getName());
            outputobject = new FileOutputStream(f);
            printobject = new PrintStream(outputobject);


            SaveVisitor visitor = new SaveVisitor(outputobject);

            model.saveModel(visitor);
            
            printobject.close();

        } catch (FileNotFoundException ex)
        {
            Logger.getLogger(LoadNStore.class.getName()).log(Level.SEVERE, null, ex);
            returnVal = JFileChooser.ERROR_OPTION;
        }

        return returnVal;
    }

    static public void screenshot(Canvas3D c3d)
    {
        JFileChooser filechooser = new JFileChooser();
        filechooser.setDialogTitle("Screenshot speichern");
        filechooser.setSelectedFile(new File("Screenshot3DSysMod_" + System.currentTimeMillis() + ".png"));
        //Initialisiere das BufferedImage
        if (filechooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                int width = c3d.getSize().width;
                int height = c3d.getSize().height;
                GraphicsContext3D ctx = c3d.getGraphicsContext3D();
                BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                ImageComponent2D ic = new ImageComponent2D(ImageComponent.FORMAT_RGB, bi);
                Raster ras = new Raster(new Point3f(-1.0f, -1.0f, -1.0f),
                        Raster.RASTER_COLOR, 0, 0, width, height, ic, null);
                ctx.readRaster(ras);
                ImageIO.write(ras.getImage().getImage(), "png", filechooser.getSelectedFile().getAbsoluteFile());
            } catch (IOException ex)
            {
                // Fehlermeldung anzeigen?
            }
        }
    }
}
