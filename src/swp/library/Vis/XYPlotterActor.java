/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package swp.library.Vis;

import java.util.Hashtable;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Sink;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 *
 * @author svenjager
 */
public class XYPlotterActor extends Sink
{

    private Hashtable<Integer, Hashtable<Double, Token>> m_history = null;

    
    
    
    public XYPlotterActor(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException
    {
        super(container, name);
        input.setMultiport(true);
        m_history = new Hashtable<Integer, Hashtable<Double, Token>>();
    }

    @Override
    public void initialize() throws IllegalActionException
    {
        super.initialize();
        m_history.clear();
        for (int i = 0; i < input.getWidth(); i++)
        {
            m_history.put(i, new Hashtable<Double, Token>() );
        }

        
    }

    @Override
    public boolean postfire() throws IllegalActionException
    {
        super.postfire();

        for (int i = 0; i < input.getWidth(); i++)
        {
            if (input.hasToken(i))
            {
                m_history.get(i).put(getDirector().getModelTime().getDoubleValue(), input.get(i));
            }
        }
        return true;
    }


    public Hashtable<Integer, Hashtable<Double, Token>> getHistory()
    {
        return m_history;
    }


    
}
