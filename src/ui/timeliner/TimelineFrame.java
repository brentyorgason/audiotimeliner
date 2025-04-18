package ui.timeliner;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import ui.common.BasicWindow;
import ui.common.UIUtilities;
import ui.common.WindowManager;
import util.logging.UIEventType;
//import org.apache.commons.io.*;

//import javafx.scene.control.SplitPane;

/**
 * Timeline Frame
 * This is a frame containing a menu bar, a timeline panel and a timeline control panel
 * control panel.
 *
 * @author Brent Yorgason
 * @author Jim Halliday
 */

public class TimelineFrame extends BasicWindow  {

  private static final long serialVersionUID = 1L;
// external components
  private TimelinePanel pnlTimeline;
  private TimelineControlPanel pnlControl;
  private JSplitPane pnlSplit;
  private Container contentPane;
  private TimelineMenuBar menubTimeline;
  JMenuItem menuiTimelineHelp = new JMenuItem();
  protected TimelineWizard wizard;
  private static Logger log = Logger.getLogger(TimelineControlPanel.class);

  // variables
  private int height;
  protected Vector initialTimepoints = new Vector();
  protected Vector initialMarkers = new Vector();
  public boolean isNewTimeline = true;
  public String originalMedia = "";
  public String temporaryMedia = "";

  // timeline versions
  public boolean isStandaloneVersion = false;
  public boolean isUsingLocalAudio = false;
  public boolean isNewAudio = false;

  // scrollbars
  protected JScrollPane scrollPane;
  protected JScrollBar hscrollBar;
  protected JScrollBar vscrollBar;

  //the player associated with this timeline
  private TimelinePlayer tPlayer;
  private TimelineLocalPlayer tLocalPlayer;

  // window sizes
  static private int INITIAL_X_SIZE = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
  static private int INITIAL_Y_SIZE = UIUtilities.scaleHeight(750); // 600; // was 450
  final static int X_MINIMUM = 800;
  final static int Y_MINIMUM = 500; // was 400

  // window title
  final static int WINDOW_TITLE_MAXLENGTH = 80;
  public static int windowNumber = 1;

  // the height of the control panel
  static int CONTROL_PANEL_HEIGHT = UIUtilities.scaleHeight(350); // 240; // 210

  // amount of space between bottom of timeline panel and timeline
  final static int BOTTOM_SPACE = UIUtilities.scalePixels(50);

  // amount of "extra vertical space" (after adding control panel height and timeline panel height)
  static int SPACER = UIUtilities.scalePixels(78);

  // amount of space on the sides of the timeline panel (half of this value on either side of the timeline)
  static int SIDE_SPACE = UIUtilities.scalePixels(120);

  // amount of space taken up by sides of frame
  static int FRAME_SIDE_SPACE = UIUtilities.scalePixels(15);

  /**
   * constructor
   */
  public TimelineFrame() {
    super(WindowManager.WINTYPE_TIMELINE, true);        //add the menubar with common menus already there

    jbInit();

    // display frame
    addPanes();
    this.setVisible(true);

  }

  /**
   * constructor
   */
  public TimelineFrame(boolean standalone) {
    super(WindowManager.WINTYPE_LOCAL_TIMELINE, true);        //add the menubar with common menus already there

    if (standalone == true) {
      this.isStandaloneVersion = true;
      this.isUsingLocalAudio = true;
    }

    jbInit();

    // display frame
    addPanes();
    setVisible(true);
    //show();
  }

  /**
   * jbInit: initialization code
   */
  private void jbInit() {
    //initial title only - may be overwritten
    this.setTitle("Audio Timeline Window " + TimelineFrame.windowNumber);

    windowNumber = windowNumber + 1;
    contentPane = getContentPane();
    Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
    if (screenDimensions.getHeight() < INITIAL_Y_SIZE) {
  	  INITIAL_Y_SIZE = (int)screenDimensions.getHeight()-40;
    }

// set size
    this.setSize(new Dimension(INITIAL_X_SIZE, INITIAL_Y_SIZE));

// set location
//    this.setLocation(this.getX(), this.getY() + (((windowNumber-1)%15)*20)); // each window cascaded 20 pixels lower (does not shift right)
    height = INITIAL_Y_SIZE;

// Mac specific layout
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      CONTROL_PANEL_HEIGHT = (int)(height * .5); //UIUtilities.scaleHeight(350); // 203
      SPACER = UIUtilities.scalePixels(52);
    }

// create display and control panels
    CONTROL_PANEL_HEIGHT = (int)(height * .5); //UIUtilities.scaleHeight(350); // 203
    int screenWidth = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    int pnlWidth = screenWidth - SPACER;
    int pnlHeight = height - CONTROL_PANEL_HEIGHT - SPACER;
    pnlTimeline = new TimelinePanel(this, pnlWidth, pnlHeight);
    pnlControl = new TimelineControlPanel(this, pnlWidth, CONTROL_PANEL_HEIGHT);
    pnlTimeline.setControlPanel(pnlControl);

// set up menus
    menubTimeline = new TimelineMenuBar(this);
    pnlTimeline.setMenuBar(menubTimeline);
    pnlControl.setMenuBar(menubTimeline);
    
    if (System.getProperty("os.name").startsWith("Mac OS") ) {
    	if (Desktop.isDesktopSupported()) {
    		//log.debug("desktop is supported!");
    		//Desktop.getDesktop().setDefaultMenuBar(menubTimeline);
    	}
    	//Desktop.getDesktop().setDefaultMenuBar(menubTimeline);
    }

// initially disable some menu options
    menubTimeline.setPrintEnabled(false);
    menubTimeline.setSaveEnabled(false);
    menubTimeline.setTimelineMenuEnabled(false);

// help menu action
    menuiTimelineHelp.setText("Open Help File...");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
        menuiTimelineHelp.setFont(UIUtilities.fontMenusMac);
    } else {
        menuiTimelineHelp.setFont(UIUtilities.fontMenusWin);
    }
    this.basicMenuBar.menuHelp.add(this.menuiTimelineHelp, 0);

    menuiTimelineHelp.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        menuiTimelineHelp_actionPerformed(e);
      }
    });

//no player until we get content
    tPlayer = null;
    tLocalPlayer = null;

// window listeners that refresh the timeline
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent ev) {
        if (tryClose())
          doWindowClose();
      }

      public void windowActivated(WindowEvent ev) {
        if (pnlTimeline.getTimeline() != null) {
          pnlTimeline.scheduleRefresh();
        }
      }
      public void windowDeiconified(WindowEvent ev) {
        if (pnlTimeline.getTimeline() != null) {
          pnlTimeline.scheduleRefresh();
        }
      }
    });

// resize action
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        this_componentResized(e);
      }
    });
  }

  /**
   * Sets audio content for this window. This method should always be called immediately after the window is created
   * and may be called again later if content changes.
   *
   * @param start Start time in milliseconds
   * @param stop Stop time in milliseconds
   */
  public void setContent(String containerID, int start, int stop) {
    try {
      tPlayer = new TimelinePlayer(containerID, start, stop, this);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Error adding audio to Timeline. Make sure to remove brackets and other special characters from file names. \n",
                                    "Audio error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    //necessary for QT to work now!
    if (tPlayer.getQTComponent() != null) {
      this.pnlControl.add(tPlayer.getQTComponent());
    }

    //go to beginning of excerpt
    tPlayer.setOffset(0);

    //tell components that audio is ready at this point
    pnlTimeline.setPlayer(tPlayer, isNewTimeline);
    pnlControl.setPlayer(tPlayer);
    pnlControl.doPlayerEnable();
    pnlControl.lblStatus.setText(TimelineControlPanel.STATUS_IDLE);

    // enable some menu options
    menubTimeline.setPrintEnabled(true);
    menubTimeline.setSaveEnabled(true);
    menubTimeline.setTimelineMenuEnabled(true);

    uilogger.log(UIEventType.WINDOW_OPENED, "timeline: ID=" + containerID + ", start= " + start + ", end= " + stop);
  }

  /**
   * Sets audio content for this window. This method should always be called immediately after the window is created
   * and may be called again later if content changes.
   *
   * @param start Start time in milliseconds
   * @param stop Stop time in milliseconds
   */
  public void setContent(File filename, int start, int stop) {
    try {
      tLocalPlayer = new TimelineLocalPlayer(filename, start, stop, this);
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Error adding audio to Timeline. Make sure to remove brackets and other special characters from file names. \n" + filename + ".",
                                    "Audio error", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
    }
    // preserve original media path
    
    tLocalPlayer.originalMedia = originalMedia;
    
    // enable some menu options
    menubTimeline.setPrintEnabled(true);
    menubTimeline.setSaveEnabled(true);
    menubTimeline.setTimelineMenuEnabled(true);
    //this.setTitle(tLocalPlayer.filename.toString());
    //System.out.println(start);
    WindowManager.toFront(this);
  }
  
  public void setOriginalMedia(String media) {
	  
	  originalMedia = media;
	  
  }
  
  public void setTemporaryMedia(String media) {
	  
	  temporaryMedia = media;
	  
  }
  
  public void bringToFront() {
	  WindowManager.toFront(this);
  }

  /**
   * create a timeline in the timeline panel (called externally)
   */
  public void createTimeline() {
    if (pnlTimeline != null) {
      pnlTimeline.createTimeline();
      pnlTimeline.menubTimeline.menuiRevertToSaved.setEnabled(false);
    }
  }

  /**
   * import tracks as initial timepoints
   */
  public void importTracks(Vector tracks) {
    for (int i = 0; i < tracks.size(); i++) {
      initialTimepoints.addElement(tracks.elementAt(i));
    }
  }

  /**
   * import bookmarks as initial timepoints or markers
   */
  public void importBookmarks(Vector bookmarks, boolean importAsMarkers) {
    if (importAsMarkers) {
      for (int i = 0; i < bookmarks.size(); i++) {
        initialMarkers.addElement(bookmarks.elementAt(i));
      }
    }
    else {
      for (int i = 0; i < bookmarks.size(); i++) {
        initialTimepoints.addElement(bookmarks.elementAt(i));
      }
    }
  }

  /**
   * processes window close event
   */
  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      if (tryClose())
        doWindowClose();
    } else {
      super.processWindowEvent(e);
    }
  }

  /**
   * Close this window
   */
  protected void doWindowClose() {
    Timeline timeline = pnlTimeline.getTimeline();
    if (timeline != null) {
      uilogger.log(UIEventType.WINDOW_CLOSED, "timeline: ID= " + timeline.getPlayerContent());
    }

    if (tPlayer != null) {
      tPlayer.pause();
      tPlayer.turnOffTimer();
      tPlayer = null;
    }
    if (tLocalPlayer != null) {
      tLocalPlayer.pause();
      tLocalPlayer.turnOffTimer();
      tLocalPlayer = null;
    }

    super.doWindowClose();
  }

  /**
   * tries to close this window
   */
  public boolean tryClose() {
    // warn
    int x = 0;
    Timeline timeline = pnlTimeline.getTimeline();
    if (timeline != null && timeline.isDirty) {
      x = JOptionPane.showConfirmDialog(this,
                                        "Would you like to save the current timeline before closing?",
                                        "Save current timeline", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if (x == JOptionPane.YES_OPTION) {
        pnlTimeline.saveTimeline(false);
        uilogger.log(UIEventType.BUTTON_CLICKED, "accept save upon exit warning");
      }
      else if (x == JOptionPane.CANCEL_OPTION) {
        uilogger.log(UIEventType.BUTTON_CLICKED, "cancel save upon exit warning");
        return false;
      }
    }
    
    // delete any temporary files
    if (temporaryMedia !="") {
    	
    	File temp = new File(temporaryMedia); 
        temp.deleteOnExit();
    }
    
    return true;
  }

  /**
   * doWindowResize: Checks whenever user resizes window, to make sure it hasn't gotten too small.
   * If it has, the window is resized to a set minimum size.
   */
  protected void doWindowResize() {
    int x = this.getWidth();
    int y = this.getHeight();
    int mx = TimelineFrame.X_MINIMUM;
    int my = TimelineFrame.Y_MINIMUM;

    if (x < mx) {
      x = mx;
    }
    if (y < my) {
      y = my;
    }

    this.setSize(x, y);

    // update control panel size
    //int pnlHeight;
    int controlHeight;
    
    this.repaint();
    controlHeight = y - scrollPane.getHeight() - SPACER;
    Dimension d3 = new Dimension(pnlTimeline.getWidth(), controlHeight);
    pnlControl.setMinimumSize(new Dimension(pnlTimeline.getWidth(), pnlControl.minHeight));
    pnlControl.setPreferredSize(d3);
    pnlControl.setSize(d3);
    pnlControl.height = controlHeight;
   // pnlControl.scrpAnnotations.setPreferredSize(d3);
    pnlControl.revalidate();

    if (pnlTimeline.getTimeline() != null) {
	    if (pnlTimeline.getTimeline().getHighestBubbleHeight() + BOTTOM_SPACE > (y - CONTROL_PANEL_HEIGHT - SPACER)) {
	      pnlTimeline.doResize(pnlTimeline.getTimeline().getLineLength());
	    }
	    else {
	    	
	        pnlTimeline.repositionTimeline();
	        pnlTimeline.scheduleRefresh();
	        scrollPane.getVerticalScrollBar().setValue( scrollPane.getVerticalScrollBar().getMaximum() );

	    }
    }
  }

  /**
   * This method will be called when the player reaches the end of the content (EOS). Note that this
   * method only gets called if the stream ends on its own (at the end of the excerpt),
   * NOT if the user initiates a stop or pause.
   */
  protected void endOfContent() {
    pnlControl.btn_stopAction();
    String totalString = "";
    totalString = UIUtilities.convertOffsetToHoursMinutesSeconds(pnlTimeline.getTimeline().getPlayerDuration());
    String elapsedString = UIUtilities.convertOffsetToHoursMinutesSeconds(0);
    pnlControl.lblElapsed.setText("" + elapsedString + " / " + totalString);
  }

  /**
   * addPanes: adds the timeline scroll pane and control panel
   */
  private void addPanes() {
	  
    contentPane.setLayout(new GridLayout(1,1));

    // scrollpanes and scrollbars
    scrollPane = new JScrollPane(pnlTimeline);
    scrollPane.setMinimumSize(new Dimension(TimelineFrame.X_MINIMUM, 200));
    hscrollBar = scrollPane.getHorizontalScrollBar();
    hscrollBar.addMouseListener(new MouseAdapter () {
      public void mouseEntered (MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }
    });
    hscrollBar.addAdjustmentListener(new AdjustmentListener(){
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          pnlTimeline.refreshTimeline();
          doWindowResize();
        }
      }
    });
    hscrollBar.setUnitIncrement(50);
    vscrollBar = scrollPane.getVerticalScrollBar();
    vscrollBar.addMouseListener(new MouseAdapter () {
      public void mouseEntered (MouseEvent e) {
        setCursor(Cursor.getDefaultCursor());
      }
    });
    vscrollBar.addAdjustmentListener(new AdjustmentListener(){
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          pnlTimeline.refreshTimeline();
          doWindowResize();
        }
      }
    });
    vscrollBar.setUnitIncrement(50);

    // add timeline panel and control panel
    pnlSplit = new JSplitPane(SwingConstants.HORIZONTAL, scrollPane, pnlControl);
    contentPane.add(pnlSplit); 
    
//    contentPane.add(scrollPane, BorderLayout.NORTH);
//    contentPane.add(pnlControl, BorderLayout.SOUTH);
    
    pnlSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, 
    new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent pce) {
        	
	        //scrollPane.revalidate();
         	//doWindowResize();
         	if (pnlTimeline.getTimeline() != null) {
         		scrollPane.getVerticalScrollBar().setValue( scrollPane.getVerticalScrollBar().getMaximum() );
         	}
        }
});
    

  }

  /**
   * getControlPanel: returns a reference to the control panel
   */
  protected TimelineControlPanel getControlPanel() {
    return pnlControl;
  }

  /**
   * getTimelineMenuBar: returns a reference to the menu bar
   */
  protected TimelineMenuBar getTimelineMenuBar() {
    return (TimelineMenuBar)menubTimeline;
  }

  /**
   * getTimelinePanel: returns a reference to the timeline panel
   */
  public TimelinePanel getTimelinePanel() {
    return pnlTimeline;
  }

  /**
   * getTimelinePlayer: returns a reference to the timeline player
   */
  public TimelinePlayer getTimelinePlayer() {
    return tPlayer;
  }

  /**
   * getTimelineLocalPlayer: returns a reference to the local timeline player
   */
  public TimelineLocalPlayer getTimelineLocalPlayer() {
    return tLocalPlayer;
  }


  /**
   * resize event handler
   */
  private void this_componentResized(ComponentEvent e) {
    if (e.getComponent() == this) {
      doWindowResize();
      uilogger.log(UIEventType.WINDOW_RESIZED, "x:" + (int)this.getSize().getWidth() + " y:" + (int)this.getSize().getHeight());
    }
  }

  /**
   * help menu event handler
   */
  void menuiTimelineHelp_actionPerformed(ActionEvent e) {
    uilogger.log(UIEventType.MENUITEM_SELECTED,
                 "Timeline Help Page Initiated");
    if (Desktop.isDesktopSupported()) {
        try {
        	String fileSeparator = File.separator;
        	String input = "";
        	if (System.getProperty("os.name").startsWith("Mac OS")) {
        		input = "resources" + fileSeparator + "Audio Timeliner Help.pdf";
        	} else {
        		input = "resources/Audio Timeliner Help.pdf";
        	}
        	InputStream helpfile = getClass().getClassLoader().getResourceAsStream(input);
        	
        	Path temphelp = Files.createTempFile("Audio Timeliner Help", ".pdf");
        	temphelp.toFile().deleteOnExit();
        	Files.copy(helpfile,  temphelp, StandardCopyOption.REPLACE_EXISTING);
        	File myFile = new File(temphelp.toFile().getPath());
        	//log.debug(myFile.getPath());
            //File myFile = new File(AppEnv.getAppDir()+ "resources/Audio Timeliner Help.pdf");
            Desktop.getDesktop().open(myFile);
        } catch (IOException ex) {
            // no application registered for PDFs
        }
    }
  }

  public void launchWizard() {
    wizard = new TimelineWizard(this);
  }
}