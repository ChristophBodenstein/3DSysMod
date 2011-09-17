/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.beanutils.BeanUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import swp.graphic.IGraphicPort;
import swp.library.Library;
import swp.library.NoElementFoundException;
import swp.model.Model;
import swp.model.ModelElement;
import swp.model.ModelPort;

/**
 *
 * @author svenjager
 */
public class ModelLoader {

    private Document m_doc = null;
    private Node connectionNode = null;
    private File m_filename = null;
    private Model m_model = null;

    private Element m_currentElement = null;

    private Hashtable<String,IGraphicPort> m_porthashtable = new Hashtable<String, IGraphicPort>();

    public ModelLoader(File filename)
    {
            m_filename = filename;
    }

    public synchronized void load(Model model) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException
    {
        m_model = model;
        try
        {
            //1.Schritt xml datei in dom laden
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            m_doc = docBuilder.parse(m_filename);
            m_doc.normalizeDocument();

            if (m_doc.getDocumentElement().getNodeName().equals("Model"))
            {
                String modelname = m_doc.getDocumentElement().getAttribute("name");
                String simTime = m_doc.getDocumentElement().getAttribute("simulationtime");

                BeanUtils.setProperty(model.newInstance(),"Name", modelname);
                BeanUtils.setProperty(model.newInstance(),"SimulationTime", simTime);
                


                //2.Schritt ModelElemente erzeugen
                NodeList childNodes = m_doc.getDocumentElement().getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++)
                {
                    Node item = childNodes.item(i);
                    if (item.getNodeName().equals("Connections"))
                    {
                        connectionNode = item;
                    }
                    if (item.getNodeName().equals("ModelElement"))
                    {
                        Element elem = (Element) item;
                        String creatorName = elem.getAttribute("creator");
                        String className = elem.getAttribute("classname");
                        ModelElement modelElement = Library.getInstance().createElement(creatorName, className);

                        m_model.addModelElement(modelElement);
                        m_currentElement = elem;


                        NodeList portList = m_currentElement.getElementsByTagName("Port");

                        for (int j = 0; j < portList.getLength(); j++)
                        {
                            Element elm = (Element) portList.item(j);

                            String name = elm.getAttribute("name");
                            String type = elm.getAttribute("type");


                            ModelPort mPort = null;

                            if(type.equals("INPUT"))
                            {
                                mPort = new ModelPort(modelElement, ModelPort.PortType.INPUT);
                            }
                            if(type.equals("OUTPUT"))
                            {
                                mPort = new ModelPort(modelElement, ModelPort.PortType.OUTPUT);
                            }

                            mPort.setName(name);
                            modelElement.addPort(mPort);

                        }

                        //3.Schritt lade die nicht öffentlichen eigenschaften
                        modelElement.visitByLoader(this);
                    }
                }
                
                //4.Schritt ModelConnection erzeugen
                if (connectionNode != null)
                {
                   NodeList conNodeList = ((Element)connectionNode).getElementsByTagName("Connection");
                   for(int i = 0 ; i<conNodeList.getLength();i++)
                   {
                       m_currentElement = (Element) conNodeList.item(i);

                       String port1Str = m_currentElement.getAttribute("port1");
                       IGraphicPort port1 = m_porthashtable.get(port1Str);


                       String port2Str = m_currentElement.getAttribute("port2");
                       IGraphicPort port2 = m_porthashtable.get(port2Str);

                        if ((port1 != null) && (port2 != null))
                        {
                            model.addModelConnection(port1, port2).visitByLoader(this);
                        }

                    }

                }
                
            }
        } catch (NoElementFoundException ex)
        {
            Logger.getLogger(ModelLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex)
        {
            Logger.getLogger(ModelLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex)
        {
            Logger.getLogger(ModelLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex)
        {
            Logger.getLogger(ModelLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
}

    public int getElementID()
    {
        String idstr = m_currentElement.getAttribute("id");

        return Integer.parseInt(idstr);

    }

    public String getElementName()
    {
        String name = m_currentElement.getAttribute("name");

        return name;
    }

    public Vector3d getPoint(String name)
    {
        NodeList nList = m_currentElement.getElementsByTagName("Point");

        for(int i = 0; i< nList.getLength();i++)
        {
            Element el =  (Element) nList.item(i);

            if(el.getAttribute("name").equals(name))
            {
                String xStr = el.getAttribute("x");
                String yStr = el.getAttribute("y");
                String zStr = el.getAttribute("z");
                double x = Double.parseDouble(xStr);
                double y = Double.parseDouble(yStr);
                double z = Double.parseDouble(zStr);

                return new Vector3d(x, y, z);

            }
        }
        return null;

    }

    public Color3f getColor(String name)
    {
        NodeList nList = m_currentElement.getElementsByTagName("Color");

        for(int i = 0; i< nList.getLength();i++)
        {
            Element el =  (Element) nList.item(i);

            if(el.getAttribute("name").equals(name))
            {
                String xStr = el.getAttribute("r");
                String yStr = el.getAttribute("g");
                String zStr = el.getAttribute("b");
                float x = Float.parseFloat(xStr);
                float y = Float.parseFloat(yStr);
                float z = Float.parseFloat(zStr);

                return new Color3f(x, y, z);

            }
        }
        return null;
    }

    public String getStringAttribute(String name)
    {
        return m_currentElement.getAttribute(name);
    }

    public int getPortID(String name)
    {
        NodeList pList = m_currentElement.getElementsByTagName("Port");

        for (int i = 0; i < pList.getLength(); i++)
        {
            Element elm = (Element) pList.item(i);
            if(elm.getAttribute("name").equals(name))
            {
                return Integer.parseInt(elm.getAttribute("id"));
            }
        }

        return -1;
    }

    public void addPortRef(String name, IGraphicPort port)
    {
        if(m_porthashtable.contains(name))
            return;
        m_porthashtable.put(name, port);
    }

    public LinkedList<Vector3d> getPointList()
    {
        LinkedList<Vector3d> pList = new LinkedList<Vector3d>();
        
        NodeList conNodeList = m_currentElement.getElementsByTagName("Point");

        
        for (int i = 0; i < conNodeList.getLength(); i++)
        {
            Element point = (Element) conNodeList.item(i);

            String xStr = point.getAttribute("x");
            String yStr = point.getAttribute("y");
            String zStr = point.getAttribute("z");

            double x = Double.parseDouble(xStr);
            double y = Double.parseDouble(yStr);
            double z = Double.parseDouble(zStr);
            System.out.println(x);

            pList.add(new Vector3d(x, y, z));

        }
        return pList;
    }

    public String getParameterValue(String name)
    {
        NodeList pList = m_currentElement.getElementsByTagName("Parameter");

        for (int i = 0; i < pList.getLength(); i++)
        {
            Element elm = (Element) pList.item(i);
            if(elm.getAttribute("name").equals(name))
            {
                return elm.getAttribute("value");
            }
        }

        return new String();
    }
}
