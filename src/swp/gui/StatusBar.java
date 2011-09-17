package swp.gui;

import swp.*;
import swp.graphic.*;
import swp.graphic.java3d.*;
import swp.steuerung.*;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.event.*;

public class StatusBar extends JPanel {

  private GUI gui;

  private JLabel elementLabel;
  private JLabel messageLabel;
  private JLabel trackingLabel;
  
  private IPickable selectedObject;
  private HeadTracker headTracker;

  private StatusBar() { }

  public StatusBar(GUI gui) {
    this.gui = gui;
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(0, 20));
    elementLabel = new JLabel("Kein Objekt selektiert.");
    elementLabel.addMouseListener(new ElementMouseListener());
    elementLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.WHITE));
    add(elementLabel, BorderLayout.WEST);
    messageLabel = new JLabel("<html> </html>");
    messageLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.WHITE));
    add(messageLabel, BorderLayout.CENTER);
    trackingLabel = new JLabel("<html> </html>", SwingConstants.CENTER);
    trackingLabel.setOpaque(true);
    trackingLabel.addMouseListener(new HeadtrackingContextMenu());
    trackingLabel.setPreferredSize(new Dimension(60, trackingLabel.getPreferredSize().height));
    trackingLabel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.WHITE));
    add(trackingLabel, BorderLayout.EAST);
  }
  
  public Color getBGColorByPercentage(float percentage) {
    //Ermittelt zum gegebenen Prozentwert eine Farbe zwischen rot (0%) und gruen (100%)
    if((percentage > 100) || (percentage < 0)) return new Color(0, 0, 255);
    //bis 50% ist der Rotanteil maximal, danach faellt er linear bis auf 0
    int red = Math.round((percentage <= 50) ? 255 : (510 * (1 - (percentage/100))));
    //bis 50% steigt der Gruenanteil an, danach ist er maximal
    int green = Math.round((percentage >= 50) ? 255 : (510 * (percentage/100)));
    return new Color(red, green, 0);
  }
  
  public void setTrackingLabel(float percentage) {
    //zeigt den Prozentsatz erfolgreicher Gesichtserkennungen an
    if(percentage == -1) {
      // der Head Tracker ist nicht aktiviert
      trackingLabel.setBackground(new Color(255, 255, 255));
      trackingLabel.setText("aus");
      return;
    }
    trackingLabel.setBackground(getBGColorByPercentage(percentage));
    trackingLabel.setText(Math.round(percentage) + "%");
  }

  public void initTrackingLabel(HeadTracker tracker) {
    headTracker = tracker;
  }
  
  public HeadTracker getHeadTracker() {
    return headTracker;
  }

  public void setElement(SelectionEvent e) {
    setElement(e.getSelectedObject());
  }
  public void setElement(Object o) {
    if(IPickable.class.isInstance(o)) {
      selectedObject = (IPickable)o;
      System.out.println(o.toString());
    } else {
      selectedObject = null;
    }
    if(INamable.class.isInstance(o)) {
      elementLabel.setText("Objekt '" + ((INamable)o).getText() + "' selektiert.");
    } else if(InflexPoint.class.isInstance(o)) {
      elementLabel.setText("Verbindungspunkt selektiert.");
    } else if(swp.graphic.java3d.Port.class.isInstance(o)) {
      elementLabel.setText(((swp.graphic.java3d.Port)o).getTypeName() + "-Port des Objekts '" + ((swp.graphic.java3d.Port)o).getCubeName() + "' selektiert.");
    } else {
      elementLabel.setText("Kein Objekt selektiert.");
    }

  }

  public void setMessage(String message) {
      messageLabel.setText(message);
  }

  private class HeadtrackingContextMenu extends JPopupMenu implements MouseListener {

    private JMenuItem headtrackingSwitch;
    private JMenuItem showHeadPositionPanel;

    private String menuItemStoppText = "Headtracker stoppen";
    private String menuItemStartText = "Headtracker starten";
    private String menuItemHeadPositionText = "Show Head Position";

    public HeadtrackingContextMenu() {
      headtrackingSwitch = new JMenuItem(menuItemStoppText);
      headtrackingSwitch.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(e.getActionCommand().equals(menuItemStoppText)) {
            headTracker.close();
            headtrackingSwitch.setText(menuItemStartText);
            //showHeadPositionPanel.setEnabled(false);
          } else {
            headTracker.start();
            headtrackingSwitch.setText(menuItemStoppText);
            //showHeadPositionPanel.setEnabled(true);
          }
        }
      });
      add(headtrackingSwitch);
      /*
      showHeadPositionPanel = new JMenuItem(menuItemHeadPositionText);
      showHeadPositionPanel.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          if(e.getActionCommand().equals(menuItemHeadPositionText)) {
            //open a frame that shows the head position in the video frame
            headTracker.getHeadPositionPanel().createWindow().setVisible(true);
          } else {
            //there is no else yet
          }
        }
      });
      add(showHeadPositionPanel);
      */
      //addMouseListener(this);
    }
    
    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        show(trackingLabel, e.getX(), e.getY());
      }
    }

    public void mouseClicked(MouseEvent e) { }
    public void mouseEntered(MouseEvent e) { }
    public void mouseExited(MouseEvent e) { }
    public void mousePressed(MouseEvent e) { }
  }

  private class ElementMouseListener extends MouseAdapter {
    private ElementMouseListener() { }
    
    public void mouseReleased(MouseEvent e) {
      if(selectedObject == null) return;
      gui.openContextMenu(selectedObject, e.getX(), e.getY(), elementLabel);
    }
  }

}