/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp;

import java.io.FileOutputStream;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author svenjager
 */
public class SaveVisitor
{
    
    FileOutputStream m_stream = null;
    Document m_doc = null;

    Stack<Element> m_stack = null;


    Element m_connectionRoot = null;

    public SaveVisitor(FileOutputStream stream)
    {
        try
        {
            m_stream = stream;
            m_stack = new Stack<Element>();

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            m_doc = builder.newDocument();

            m_doc.setXmlStandalone(true);
            m_doc.createDocumentFragment();
            

        } catch (ParserConfigurationException ex)
        {
            Logger.getLogger(SaveVisitor.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void write()
    {
        try
        {
            Source source = new DOMSource(m_doc);
            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            
            Result result = new StreamResult(m_stream);
            xformer.transform(source, result);
        } catch (TransformerException ex)
        {
            Logger.getLogger(SaveVisitor.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public void startDocument()
    {
        Element elm = m_doc.createElement("Model");
        m_connectionRoot = m_doc.createElement("Connections");
        elm.appendChild(m_connectionRoot);

        m_stack.push(elm);
        m_doc.appendChild(elm);
    }

    public void endDocument()
    {
        m_stack.pop();
    }

    public void startModelElement()
    {
      
       Element elm = m_doc.createElement("ModelElement");

       m_stack.peek().appendChild(elm);
       m_stack.push(elm);

    }

    public void endModelElement()
    {
        m_stack.pop();
    }

    public void addStringAttribute(String name, String value)
    {
        m_stack.peek().setAttribute(name, value);
    }

    public void addPoint(String name, Vector3d pos)
    {
        Element elem = m_doc.createElement("Point");

        elem.setAttribute("name", name);
        elem.setAttribute("x", String.valueOf(pos.x));
        elem.setAttribute("y", String.valueOf(pos.y));
        elem.setAttribute("z", String.valueOf(pos.z));

        m_stack.peek().appendChild(elem);
    }

    public void addColor(String name, Color3f color)
    {
        Element elem = m_doc.createElement("Color");

        elem.setAttribute("name", name);
        elem.setAttribute("r", String.valueOf(color.x));
        elem.setAttribute("g", String.valueOf(color.y));
        elem.setAttribute("b", String.valueOf(color.z));
        m_stack.peek().appendChild(elem);
    }

    public void startPort()
    {
       Element elem = m_doc.createElement("Port");
       m_stack.peek().appendChild(elem);
       m_stack.push(elem);
    }

    public void endPort()
    {
        m_stack.pop();
    }

    public void startConnection()
    {
       Element elem = m_doc.createElement("Connection");
       m_connectionRoot.appendChild(elem);
       m_stack.push(elem);
    }

    public void endConnection()
    {
        m_stack.pop();
    }

    public void addParameter(String name, String value)
    {
        Element elem = m_doc.createElement("Parameter");

        elem.setAttribute("name", name);
        elem.setAttribute("value", value);
        m_stack.peek().appendChild(elem);
    }




   
}
