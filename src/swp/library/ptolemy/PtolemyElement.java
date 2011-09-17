/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package swp.library.ptolemy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import org.apache.commons.beanutils.BeanUtils;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.kernel.util.AbstractSettableAttribute;
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
public class PtolemyElement extends ModelElement
{

    private Class m_class = null;

    public PtolemyElement(Class ptolemyclass)
    {
        super();

        m_class = ptolemyclass;
        m_uniqueName = new String(ptolemyclass.getSimpleName() + "_" + m_id);

        this.setColor(new Color3f(0.0f, 1.0f, 0.0f));
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
            super.initElement();
            
            Constructor constr = m_class.getConstructors()[0];
            Object obj = constr.newInstance(new Object[]
                    {
                        new CompositeActor(), ""
                    });



            Field[] fields = m_class.getFields();

            if (ptolemy.actor.lib.Source.class.isInstance(obj))
            {
                this.setColor(new Color3f(1.0f, 0.0f, 0.0f));
            }

            if (ptolemy.actor.lib.Sink.class.isInstance(obj))
            {
                this.setColor(new Color3f(0.0f, 0.0f, 1.0f));
            }

            
            //Ports auslesen
            for (int i = 0; i < fields.length; i++)
            {

                //System.out.println(fields[i].getName());
                //System.out.println(fields[i].getType());

                if (fields[i].getType().equals(ptolemy.actor.TypedIOPort.class))
                {
                    TypedIOPort port = (TypedIOPort) fields[i].get(obj);

                    if (port.isInput())
                    {
                        ModelPort modport = new ModelPort(this, ModelPort.PortType.INPUT);
                        modport.setName(port.getName());
                        modport.setRequierd(true);
                        this.addPort(modport);
                    }
                    if (port.isOutput())
                    {
                        ModelPort modport = new ModelPort(this, ModelPort.PortType.OUTPUT);
                        modport.setName(port.getName());
                        modport.setRequierd(true);
                        this.addPort(modport);
                    }
                }



                if (AbstractSettableAttribute.class.isInstance(fields[i].get(obj)))
                {
                    AbstractSettableAttribute par = (AbstractSettableAttribute) fields[i].get(obj);
                    if (par != null)
                    {
                        this.add(par.getName());
                        m_paramater.put(par.getName(), par.getExpression());
                    }
                }


                if (fields[i].getType().equals(PortParameter.class))
                {
                    PortParameter port = (PortParameter) fields[i].get(obj);

                    ModelPort modport = new ModelPort(this, ModelPort.PortType.INPUT);
                    modport.setName(port.getName());
                    modport.setRequierd(true);
                    this.addPort(modport);
                    if (port != null)
                    {
                        this.add(port.getName());
                        m_paramater.put(port.getName(), port.getExpression());
                    }
                }
                if (fields[i].getType().equals(FilePortParameter.class))
                {
                    FilePortParameter port = (FilePortParameter) fields[i].get(obj);

                    ModelPort modport = new ModelPort(this, ModelPort.PortType.INPUT);
                    modport.setName(port.getName());
                    modport.setRequierd(true);
                    this.addPort(modport);
                    if (port != null)
                    {
                        this.add(port.getName());
                        m_paramater.put(port.getName(), port.getExpression());
                    }
                }
            }

            //this.setRestricted(true);
            BeanUtils.populate(this.newInstance(), m_paramater);
            

        }  catch (InstantiationException ex)
        {
            Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex)
        {
            Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex)
        {
            Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex)
        {
            Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
        }


    }

    @Override
    public void visitBySimulator(SimulatorVisitor visitor) throws IllegalActionException
    {
        try
        {
            Constructor constr = m_class.getConstructors()[0];
            Object obj = constr.newInstance(new Object[]
                    {
                        visitor.getTopLevelActor(), m_uniqueName
                    });


            //benutzerdefinierte Ports hinzufügen
            if (AtomicActor.class.isInstance(obj))
            {
                AtomicActor atAct = (AtomicActor) obj;

                Iterator<ModelPort> iterator = m_modelPortList.iterator();

                while (iterator.hasNext())
                {
                    ModelPort modelPort = iterator.next();
                    if (!modelPort.isRequiered())
                    {
                        if (modelPort.getPortType() == ModelPort.PortType.INPUT)
                        {
                            new TypedIOPort(atAct, modelPort.getName(), true, false);
                        }
                        if (modelPort.getPortType() == ModelPort.PortType.OUTPUT)
                        {
                            new TypedIOPort(atAct, modelPort.getName(), false, true);
                        }
                    }
                }
            }

            Enumeration<String> key = m_paramater.keys();
            while (key.hasMoreElements())
            {
                String parName = key.nextElement();

                try
                {

                    Field pField = m_class.getField(parName);

                    if (pField != null)
                    {
                        AbstractSettableAttribute parObject = (AbstractSettableAttribute) pField.get(obj);

                        //lese die information des Beans aus
                        if (m_bean != null)
                        {
                            parObject.setExpression((String) m_bean.get(parName));
                            //System.out.println((String)m_bean.get(parName));
                        }
                    }
                }
                catch(NoSuchFieldException ex)
                {
                    System.out.println("Parameter "+ parName+" wurde nicht gesetzt.");
                    //Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
                }
            }



        } catch (Exception ex)
        {
            visitor.addError(this, ex);
            Logger.getLogger(PtolemyElement.class.getName()).log(Level.SEVERE, null, ex);
        } 
        super.visitBySimulator(visitor);
    }

    @Override
    public String getName()
    {
        return this.getClass().getName();
    }

    @Override
    public void visitBySaver(SaveVisitor visitor)
    {
        super.visitBySaver(visitor);

        visitor.addStringAttribute("creator", PtolemyLibrary.class.getName());
        visitor.addStringAttribute("classname", m_class.getName());

    }

    @Override
    public void visitByLoader(ModelLoader loader)
    {
        super.visitByLoader(loader);
    }
}
