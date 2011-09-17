/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.library.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.python.PythonScript;
import ptolemy.kernel.util.IllegalActionException;
import swp.ModelLoader;
import swp.SaveVisitor;
import swp.model.ModelElement;
import swp.model.ModelPort;
import swp.sim.SimulatorVisitor;

/**
 *
 * @author svenjager
 */
public class ScriptElement extends ModelElement implements IScriptElement
{

    String m_source;
    String m_filename;

    public ScriptElement(String filename)
    {
        super();

        m_uniqueName = new String("script_" + m_id);
        this.setColor(new Color3f(0.0f, 1.0f, 1.0f));

        m_filename = filename;

    }

    @Override
    public String getScenePath()
    {
        return null;
    }

    @Override
    public String getImagePath()
    {
        return null;
    }

    @Override
    public void initElement()
    {
        try
        {


            if (!m_filename.isEmpty())
            {
                File file = new File(m_filename);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                m_source = new String();

                StringBuffer source = new StringBuffer();
                String line = null;
                while ((line = reader.readLine()) != null)
                {
                    if (line.startsWith("# port "))
                    {
                        //port deklaration
                        String[] portDekla = line.split(" ");

                        if (portDekla.length >= 4)
                        {
                            String porttype = portDekla[2];
                            String portname = portDekla[3];



                            if (porttype.toLowerCase().equals("input"))
                            {
                                ModelPort newport = new ModelPort(this, ModelPort.PortType.INPUT);
                                newport.setName(portname);
                                this.addPort(newport);
                            } else
                            {
                                ModelPort newport = new ModelPort(this, ModelPort.PortType.OUTPUT);
                                newport.setName(portname);
                                this.addPort(newport);
                            }

                        }

                    }


                    source.append(line).append("\n");
                }

                m_source = source.toString();
            }


        } catch (Exception ex)
        {
            Logger.getLogger(ScriptElement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void visitBySimulator(SimulatorVisitor visitor) throws IllegalActionException
    {
        try
        {

            PythonScript pscript = new PythonScript(visitor.getTopLevelActor(), m_uniqueName);

            Iterator<ModelPort> iterator = m_modelPortList.iterator();



            while (iterator.hasNext())
            {
                ModelPort modelPort = iterator.next();


                if (!modelPort.isRequiered())
                {
                    if (modelPort.getPortType() == ModelPort.PortType.INPUT)
                    {
                        new TypedIOPort(pscript, modelPort.getName(), true, false);


                    }
                    if (modelPort.getPortType() == ModelPort.PortType.OUTPUT)
                    {
                        new TypedIOPort(pscript, modelPort.getName(), false, true);


                    }
                }
            }

            pscript.script.setExpression(m_source);



        } catch (Exception ex)
        {
            visitor.addError(this, ex);
            Logger.getLogger(ScriptElement.class.getName()).log(Level.SEVERE, null, ex);
        }


        super.visitBySimulator(visitor);


    }

    @Override
    public void visitBySaver(SaveVisitor visitor)
    {
        super.visitBySaver(visitor);

        visitor.addStringAttribute("creator", ScriptLibrary.class.getName());
        visitor.addStringAttribute(
                "classname", m_filename);
        visitor.addParameter(
                "source", m_source);

    }

    @Override
    public void visitByLoader(ModelLoader loader)
    {
        super.visitByLoader(loader);
        m_source = loader.getParameterValue("source");


    }

    @Override
    public String getName()
    {
        return this.getClass().getName();


    }

    @Override
    public String getSource()
    {
        return m_source;


    }

    @Override
    public void setSource(String source)
    {
        m_source = source;

    }
}
