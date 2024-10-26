package ui.timeliner;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
//import org.apache.log4j.Logger;
import javax.swing.text.html.HTMLEditorKit;

import org.apache.log4j.Logger;

import com.borland.jbcl.layout.VerticalFlowLayout;

import ui.common.UIUtilities;
import ui.common.WindowManager;
import ui.media.AudioControlPanel;
import ui.media.VolumePanel;
import util.logging.UIEventType;
import util.logging.UILogger;
import util.ProxyAction;

/**
 * Timeline Control Panel
 * This panel contains a default user interface for the basic functions that are used
 * to manipulate a timeline.
 *
 * @author Brent Yorgason
 */

public class TimelineControlPanel extends JPanel {
  private static final long serialVersionUID = 1L;
// external components
  private TimelinePanel pnlTimeline;
  private Timeline timeline;
  private TimelineFrame frmTimeline;
  private TimelineMenuBar menubTimeline;
  public JScrollPane treeView;
  private TimelinePlayer tPlayer;
  private TimelineLocalPlayer tLocalPlayer;
  protected MarkerEditor dlgMarkerEditor;
  protected TimepointEditor dlgTimepointEditor;
  protected AudioControlPanel pnlAudioControl = new AudioControlPanel();
  VolumePanel pnlVolumeControl = new VolumePanel();
  private static Logger logger = Logger.getLogger(TimelineControlPanel.class);

  protected UILogger uilogger;

  // fonts
  protected java.awt.Font timelineFont;
  private java.awt.Font annotationFont;
  protected String unicodeFont = "Arial Unicode MS";
  private int annotationFontSize = 18;

  // control panel buttons
  protected JButton btnAddTimepoint;
  protected JButton btnAddMarker;
  protected JButton btnEditTimepointOrMarker;
  protected JButton btnDeleteTimepointOrMarker;
  protected JButton btnEditBubble;
  protected JButton btnChangeColor;
  protected JButton btnDeleteBubble;
  protected JButton btnZoomTo;
  protected JButton btnGroupBubbles;
  protected JButton btnUngroupBubbles;
  protected JButton btnClearAll;
  protected JButton btnEditProperties;
  protected JButton btnFitToWindow;
  protected JButton btnFontLarger;
  protected JButton btnFontSmaller;
  protected JLabel lblElapsed;
  protected JLabel lblStatus = new JLabel();
  protected JLabel lblDuration = new JLabel();
  protected JLabel lblAnnotations = new JLabel("Annotations");
  protected JRadioButton radAllLevels;
  protected JRadioButton radSelectedLevels;
  protected JCheckBox chkShowMarkers;
  protected JCheckBox chkShowTimes;
  protected JCheckBox chkShowMarkerTimes;
  protected ButtonGroup grpShowLevels = new ButtonGroup();
  protected final JSlider slideVolume = pnlVolumeControl.slideVolume;
  protected ImageIcon sPlay; 
  protected ImageIcon sStop;
  protected ImageIcon sPause;
  protected ImageIcon sPrev;
  protected ImageIcon sNext;
  protected ImageIcon sRW;
  protected ImageIcon sFF;
  protected ImageIcon sSpeaker;
  protected ImageIcon sMute;

  // icons
//  final static ImageIcon icoEdit = UIUtilities.icoEdit;
//  final static ImageIcon icoAdd = UIUtilities.icoAdd;
//  final static ImageIcon icoAddMarker = UIUtilities.icoAddMarker;
//  final static ImageIcon icoBigger = UIUtilities.icoBigger;
//  final static ImageIcon icoSmaller = UIUtilities.icoSmaller;
  final ImageIcon icoEdit = new ImageIcon(getClass().getClassLoader().getResource("resources/media/editbubble.gif"));
  final ImageIcon icoAdd = new ImageIcon(getClass().getClassLoader().getResource("resources/media/addtimepoint.gif"));
  final ImageIcon icoAddMarker = new ImageIcon(getClass().getClassLoader().getResource("resources/media/addmarker.GIF"));
  final ImageIcon icoBigger = new ImageIcon(getClass().getClassLoader().getResource("resources/media/bigger.gif"));
  final ImageIcon icoSmaller = new ImageIcon(getClass().getClassLoader().getResource("resources/media/smaller.gif"));
  final ImageIcon play = new ImageIcon(getClass().getClassLoader().getResource("resources/media/playPL.gif"));
  final ImageIcon stop = new ImageIcon(getClass().getClassLoader().getResource("resources/media/stopPL.gif"));
  final ImageIcon pause = new ImageIcon(getClass().getClassLoader().getResource("resources/media/pausePL.gif"));
  final ImageIcon prev = new ImageIcon(getClass().getClassLoader().getResource("resources/media/prevPL.gif"));
  final ImageIcon next = new ImageIcon(getClass().getClassLoader().getResource("resources/media/nextPL.gif"));
  final ImageIcon ff = new ImageIcon(getClass().getClassLoader().getResource("resources/media/fastforwardPL.gif"));
  final ImageIcon rw = new ImageIcon(getClass().getClassLoader().getResource("resources/media/rewindPL.gif"));
  final ImageIcon speaker = new ImageIcon(getClass().getClassLoader().getResource("resources/media/speaker.gif"));
  final ImageIcon mute = new ImageIcon(getClass().getClassLoader().getResource("resources/media/speaker-mute.gif"));

  // layout elements
  private JPanel pnlStatus = new JPanel();
  private JPanel pnlDuration = new JPanel();
  private JPanel pnlPlayback = new JPanel();
  private JPanel pnlElapsedVolume = new JPanel();
  private JPanel pnlBubbleButtons = new JPanel();
  private JPanel pnlTimepointButtons = new JPanel();
  private JPanel pnlTimelineButtons = new JPanel();
  private JPanel pnlAnnotations = new JPanel();
  private JPanel pnlAnnotationTools = new JPanel(); 
  private JPanel pnlAnnotationTools2 = new JPanel(); 
  private JPanel pnlAnnotationTools3 = new JPanel();
  private JPanel pnlLevels = new JPanel();
  private JPanel pnlMarkers = new JPanel();
  private JPanel pnlShow = new JPanel();
  private JPanel pnlShow2 = new JPanel();
  private JPanel pnlFontButtons = new JPanel();
  private TitledBorder titledBorderPlayback;
  private TitledBorder titledBorderBubbles;
  private TitledBorder titledBorderTimepoints;
  private TitledBorder titledBorderTimeline;
  private TitledBorder titledBorderAnnotations;
  private TitledBorder titledBorderLevels;
  private TitledBorder titledBorderShow;  
  private TitledBorder titledBorderShow2;

  // media variables
  protected boolean playing = false;
  protected boolean buffering = false;
  protected boolean muted = false;
  protected final static int VOLUME_INCREMENTS = 200;
  protected float vol = 1f;
  protected final static int TIMER_FIRE_FREQUENCY = 80;
  protected boolean timerDisable = false;
  protected boolean timerCanceled = false;

  //tracking variables
  protected final static int INITIAL_TRACKING_AMOUNT = 150;
  protected int shiftAmount = INITIAL_TRACKING_AMOUNT;
  protected final static int TRACKING_NONE = 0;
  protected final static int TRACKING_FF = 1;
  protected final static int TRACKING_RW = -1;
  protected int trackingState = TRACKING_NONE;

  // status messages
  final static String STATUS_INITIALIZING = "Status: Initializing Timeliner";
  final static String STATUS_BUFFERING = "Status: Buffering Stream";
  final static String STATUS_PLAYING = "Status: Playing Content";
  final static String STATUS_IDLE = "Status: Idle";
  final static String STATUS_TRACKING = "Status: Adjusting Playback Position";
  final static String STATUS_STREAM_ERROR = "Status: Stream Error";
  final static String STATUS_STREAM_NOT_FOUND = "Status: Audio Not Found";
  final static String STATUS_CONVERTING = "Status: Converting Audio";

  // annotation pane
  protected JTextPane tpAnnotations = new JTextPane();
  protected JScrollPane scrpAnnotations = new JScrollPane(tpAnnotations);
  protected boolean showAllAnnotations = true;
  protected StyledDocument doc;
  protected Style selectedStyle;
  protected Style normalStyle;
  protected Style boldStyle;
  protected boolean isDescriptionShowing = false;
  protected int timelineFontSize;
  StyledEditorKit sek = new StyledEditorKit();
  HTMLEditorKit hek = new HTMLEditorKit();

  // other variables
  protected int height;
  protected int minHeight = 300;

  /**
   * constructor
   * receives a timeline frame, initial width, and initial height
   */
  public TimelineControlPanel(TimelineFrame tf, int initWidth, int initHeight) {
    frmTimeline = tf;
    pnlTimeline = frmTimeline.getTimelinePanel();
    height = initHeight;
    this.setMinimumSize(new Dimension(initWidth, minHeight)); // height));
    //this.setPreferredSize(new Dimension(initWidth, height));
    uilogger = frmTimeline.getUILogger();

    try {
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    this.disableAllControls();
  }

  /**
   * jbInit: Initializes the panel layout, then creates and adds the buttons and timepoint list
   */
  private void jbInit() throws Exception {

    // set font
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      timelineFont = UIUtilities.fontDialogMacSmallest;
      timelineFontSize = UIUtilities.convertFontSize(10);
      annotationFont = new Font(unicodeFont, 0, annotationFontSize - 2);
    } else {
      timelineFont = UIUtilities.fontDialogWin;
      timelineFontSize = UIUtilities.convertFontSize(11);
      annotationFont = new Font(unicodeFont, 0, annotationFontSize);
    }

    // button scaling
	  //Image playBtn = UIUtilities.icoPlay.getImage(); 
      Image playBtn = play.getImage();
	  Image scaledPlay = playBtn.getScaledInstance(UIUtilities.scalePixels(play.getIconHeight()), UIUtilities.scalePixels(play.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sPlay = new ImageIcon(scaledPlay);
	  //Image stopBtn = UIUtilities.icoStop.getImage(); 
	  Image stopBtn = stop.getImage();
	  Image scaledStop = stopBtn.getScaledInstance(UIUtilities.scalePixels(stop.getIconHeight()), UIUtilities.scalePixels(stop.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sStop = new ImageIcon(scaledStop);
	  //Image pauseBtn = UIUtilities.icoPause.getImage(); 
	  Image pauseBtn = pause.getImage();
	  Image scaledPause = pauseBtn.getScaledInstance(UIUtilities.scalePixels(pause.getIconHeight()), UIUtilities.scalePixels(pause.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sPause = new ImageIcon(scaledPause);
	  //Image prevBtn = UIUtilities.icoPrev.getImage(); 
	  Image prevBtn = prev.getImage();
	  Image scaledPrev = prevBtn.getScaledInstance(UIUtilities.scalePixels(prev.getIconHeight()), UIUtilities.scalePixels(prev.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sPrev = new ImageIcon(scaledPrev);
	  //Image nextBtn = UIUtilities.icoNext.getImage(); 
	  Image nextBtn = next.getImage();
	  Image scaledNext = nextBtn.getScaledInstance(UIUtilities.scalePixels(next.getIconHeight()), UIUtilities.scalePixels(next.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sNext = new ImageIcon(scaledNext);
	  //Image rwBtn = UIUtilities.icoRW.getImage(); 
	  Image rwBtn = rw.getImage();
	  Image scaledRW = rwBtn.getScaledInstance(UIUtilities.scalePixels(rw.getIconHeight()), UIUtilities.scalePixels(rw.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sRW = new ImageIcon(scaledRW);
	  //Image ffBtn = UIUtilities.icoFF.getImage(); 
	  Image ffBtn = ff.getImage();
	  Image scaledFF = ffBtn.getScaledInstance(UIUtilities.scalePixels(ff.getIconHeight()), UIUtilities.scalePixels(ff.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sFF = new ImageIcon(scaledFF);
	  //Image speakerBtn = UIUtilities.icoSpeaker.getImage(); 
	  Image speakerBtn = speaker.getImage();
	  Image scaledSpeaker = speakerBtn.getScaledInstance(UIUtilities.scalePixels(speaker.getIconHeight()), UIUtilities.scalePixels(speaker.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sSpeaker = new ImageIcon(scaledSpeaker);
	  //Image muteBtn = UIUtilities.icoSpeakerMute.getImage(); 
	  Image muteBtn = mute.getImage();
	  Image scaledMute = muteBtn.getScaledInstance(UIUtilities.scalePixels(mute.getIconHeight()), UIUtilities.scalePixels(mute.getIconWidth()),  java.awt.Image.SCALE_SMOOTH);
	  sMute= new ImageIcon(scaledMute);

	  
    // set panel layout models
    GridBagLayout gridbagMain = new GridBagLayout();
    GridBagLayout gridBagBubbles = new GridBagLayout();
    GridBagLayout gridBagTimepoints = new GridBagLayout();
    GridBagLayout gridBagTimeline = new GridBagLayout();
    GridBagLayout gridBagAnnotations = new GridBagLayout();
    GridBagLayout gridBagAnnotationTools = new GridBagLayout();
    GridBagLayout gridBagElapsed = new GridBagLayout();
    this.setLayout(gridbagMain);
    pnlStatus.setLayout(new VerticalFlowLayout());
    pnlDuration.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
    pnlPlayback.setLayout(new VerticalFlowLayout());
    pnlAudioControl.flowLay.setVgap(1);
    pnlAudioControl.setBorder(null);
    pnlAudioControl.setMinimumSize(null);
    pnlAudioControl.setPreferredSize(null);
    pnlElapsedVolume.setLayout(gridBagElapsed);
    pnlBubbleButtons.setLayout(gridBagBubbles);
    pnlTimepointButtons.setLayout(gridBagTimepoints);
    pnlTimelineButtons.setLayout(gridBagTimeline);
    pnlAnnotations.setLayout(gridBagAnnotations);
    pnlAnnotationTools.setLayout(new GridLayout());
    pnlAnnotationTools2.setLayout(gridBagAnnotationTools);
    pnlAnnotationTools3.setLayout(gridBagAnnotationTools);
    pnlLevels.setLayout(new BorderLayout());
    pnlMarkers.setLayout(new BorderLayout());
    pnlShow.setLayout(new GridLayout());
    pnlShow2.setLayout(new GridLayout());
    pnlFontButtons.setLayout(new VerticalFlowLayout(FlowLayout.RIGHT));

    // set up annotation pane
    tpAnnotations.setEditable(false);
    tpAnnotations.setFont(annotationFont);
    tpAnnotations.setContentType("text/html");
    scrpAnnotations.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
    // create panel borders
    titledBorderPlayback = new TitledBorder(" Playback ");
    titledBorderTimepoints = new TitledBorder(" Timepoints and Markers ");
    titledBorderTimeline = new TitledBorder(" Timeline ");
    titledBorderBubbles = new TitledBorder(" Bubbles ");
    titledBorderAnnotations = new TitledBorder(" Annotations ");
    titledBorderShow = new TitledBorder(" Show Annotations ");
    titledBorderShow2 = new TitledBorder(" Show Times ");
    titledBorderLevels = new TitledBorder(" Levels: ");
    titledBorderPlayback.setTitleFont(timelineFont);
    titledBorderShow.setTitleFont(timelineFont);
    titledBorderShow2.setTitleFont(timelineFont);
    titledBorderTimepoints.setTitleFont(timelineFont);
    titledBorderTimeline.setTitleFont(timelineFont);
    titledBorderBubbles.setTitleFont(timelineFont);
    titledBorderAnnotations.setTitleFont(timelineFont);
    titledBorderLevels.setTitleFont(timelineFont);
    pnlPlayback.setBorder(titledBorderPlayback);
    pnlTimepointButtons.setBorder(titledBorderTimepoints);
    pnlTimelineButtons.setBorder(titledBorderTimeline);
    pnlBubbleButtons.setBorder(titledBorderBubbles);
    pnlAnnotations.setBorder(titledBorderAnnotations);
    pnlAnnotationTools.setBorder(titledBorderShow); 
    pnlAnnotationTools2.setBorder(titledBorderShow2);

    // set up status bar
    lblStatus.setFont(timelineFont);
    lblStatus.setText(STATUS_INITIALIZING);
    lblDuration.setFont(timelineFont);
    lblDuration.setText("");
    lblAnnotations.setFont(timelineFont);
    //pnlStatus.setBorder(BorderFactory.createLoweredBevelBorder());
   // pnlDuration.setBorder(BorderFactory.createLoweredBevelBorder());
    pnlStatus.add(lblStatus);
    lblStatus.setBorder(BorderFactory.createLoweredBevelBorder());
    //lblStatus.setMinimumSize(new Dimension(200, 100));
    pnlDuration.add(lblDuration, null);

    // add panels to inner panels
    pnlPlayback.add(pnlAudioControl);
    pnlPlayback.add(pnlElapsedVolume, null);
    
    //pnlAnnotationTools.add(pnlShow);
    //pnlAnnotationTools.add(pnlMarkers);
    //pnlAnnotationTools.add(pnlFontButtons);
    
    // set up grid bag layout constraints and add inner panels to the main panel
    TimelineUtilities.createConstraints(this, pnlPlayback, 0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 2, 0);
    TimelineUtilities.createConstraints(this, pnlTimelineButtons, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlAnnotationTools, 0, 2, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlAnnotationTools2, 1, 2, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlTimepointButtons, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlBubbleButtons, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 2, 0);
    TimelineUtilities.createConstraints(this, pnlAnnotations, 2, 0, 1, 5, 1.0, 0.8, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(this, pnlStatus, 0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
//    TimelineUtilities.createConstraints(this, pnlDuration, 2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);

    // create controls
    createPlaybackControls();
    createTimelinerButtons();

    // disable selection buttons
    this.disableAllBubbleControls();
    this.disableSelectedTimepointControls();
    this.doPlayerDisable();

    // change cursor listener
    this.addMouseListener(new MouseAdapter() {
      public void mouseEntered (MouseEvent e) {
        frmTimeline.setCursor(Cursor.getDefaultCursor());
      }
    });
  }

  /**
   * SetShowTimes
   */
  
  public void setShowTimes(Boolean b) {
	this.chkShowTimes.setSelected(b);  
  }
  
  /**
   * SetShowMarkerTimes
   */
  
  public void setShowMarkerTimes(Boolean b) {
	this.chkShowMarkerTimes.setSelected(b);  
  }
  
  /**
   * createPlaybackControls: adds playback controls to the control panel and adds
   * listeners to each control
   */
  private void createPlaybackControls() {

    // play button

	pnlAudioControl.btnPlay.setIcon(sPlay);
    setEnterAction(pnlAudioControl.btnPlay);
    pnlAudioControl.btnPlay.setToolTipText("Play");
    pnlAudioControl.btnPlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline().playerIsPlaying()) {
          btn_pauseAction();
          uilogger.log(UIEventType.BUTTON_CLICKED, "pause");
        }
        else {
          btn_playAction();
          uilogger.log(UIEventType.BUTTON_CLICKED, "play");
        }
      }
    });

    // "stop" button
    pnlAudioControl.btnStop.setIcon(sStop);
    pnlAudioControl.btnStop.setToolTipText("Stop");
    setEnterAction(pnlAudioControl.btnStop);
    pnlAudioControl.btnStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        btn_stopAction();
        uilogger.log(UIEventType.BUTTON_CLICKED, "stop");
      }
    });

    // "previous" button
    pnlAudioControl.btnPrev.setToolTipText("Previous Timepoint");
    pnlAudioControl.btnPrev.setIcon(sPrev);
    setEnterAction(pnlAudioControl.btnPrev);
    pnlAudioControl.btnPrev.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.goToPreviousBubble();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "previous timepoint");
      }
    });

    // "rewind" button
    pnlAudioControl.btnRW.setToolTipText("Press and hold for RW");
    pnlAudioControl.btnRW.setIcon(sRW);
    setEnterAction(pnlAudioControl.btnRW);
    pnlAudioControl.btnRW.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        doRWTracking();
      }
      public void mouseReleased(MouseEvent e) {
        doStopTracking();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "rewind");
      }
    });

    // "forward" button
    pnlAudioControl.btnFF.setToolTipText("Press and hold for FF");
    pnlAudioControl.btnFF.setIcon(sFF);
    setEnterAction(pnlAudioControl.btnFF);
    pnlAudioControl.btnFF.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        doFFTracking();
      }
      public void mouseReleased(MouseEvent e) {
        doStopTracking();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "fast forward");
      }
    });

    // "next" button
    pnlAudioControl.btnNext.setToolTipText("Next Timepoint");
    pnlAudioControl.btnNext.setIcon(sNext);
    setEnterAction(pnlAudioControl.btnNext);
    pnlAudioControl.btnNext.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.goToNextBubble();
        updateAnnotationPane();
        uilogger.log(UIEventType.BUTTON_CLICKED, "next timepoint");
      }
    });

    // "mute" button
    setEnterAction(pnlVolumeControl.btnMute);
    pnlVolumeControl.btnMute.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (muted) {
          uilogger.log(UIEventType.BUTTON_CLICKED,"unmute");
        } else {
          uilogger.log(UIEventType.BUTTON_CLICKED,"mute");
        }
        doMute();
      }
    });

    // elapsed time
    lblElapsed = new JLabel("00:00 / 00:00");
    lblElapsed.setFont(timelineFont);

    // volume slider
    slideVolume.setMinimum(0);
    setEnterAction(slideVolume);
    slideVolume.setMaximum(TimelineControlPanel.VOLUME_INCREMENTS);
    slideVolume.setValue(TimelineControlPanel.VOLUME_INCREMENTS);
    slideVolume.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        vol = ((float)slideVolume.getValue()) / ((float)VOLUME_INCREMENTS);
        doVolumeUpdate();
        uilogger.log(UIEventType.SLIDER_ADJUSTED);
      }
    });
  }

  /**
   * play action
   */
  protected void btn_playAction() {
    WindowManager.stopAllPlayers();
    pnlTimeline.getTimeline().startPlayer();
    pnlTimeline.getTimeline().showTime(false);
    updateAnnotationPane();
    lblStatus.setText(TimelineControlPanel.STATUS_BUFFERING);
    buffering = false;
    playing = true;
    pnlAudioControl.btnPlay.setIcon(sPause);
    pnlTimeline.enableAddTimepoint();
    pnlTimeline.enableAddMarker();
    if (pnlTimeline.btnBubbleEditorPlay != null) {
      pnlTimeline.btnBubbleEditorPlay.setIcon(sPause);
    }
  }

  /**
   * pause action
   */
  protected void btn_pauseAction() {
    pnlTimeline.getTimeline().pausePlayer();
    pnlAudioControl.btnPlay.setIcon(sPlay);
    lblStatus.setText(STATUS_IDLE);
    buffering = false;
    playing = false;
    pnlTimeline.scheduleRefresh();
    if (pnlTimeline.btnBubbleEditorPlay != null) {
      pnlTimeline.btnBubbleEditorPlay.setIcon(sPlay);
    }
  }

  // stop action
  protected void btn_stopAction() {
    timeline = pnlTimeline.getTimeline();
    timeline.stopPlayer();
    timeline.setPlayerOffset(timeline.getLocalStartOffset());
    pnlAudioControl.btnPlay.setIcon(sPlay);
    lblStatus.setText(STATUS_IDLE);
    pnlTimeline.scheduleRefresh();
    timeline.getSlider().setValue(timeline.getLocalStartOffset());
    timeline.showTime(false);
    updateAnnotationPane();
    if (pnlTimeline.btnBubbleEditorPlay != null) {
      pnlTimeline.btnBubbleEditorPlay.setIcon(sPlay);
    }

    timeline.setNextImportantOffset(Math.min(timeline.getSortedPointList()[1], timeline.getMarkerList()[0]));
  }

  /**
   * createTimelinerButtons: adds buttons to the control panel and adds listeners to each button
   */
  private void createTimelinerButtons() {

    // "add timepoint" button
    btnAddTimepoint = new JButton("Add");
    btnAddTimepoint.setIcon(icoAdd);
    btnAddTimepoint.setFont(timelineFont);
    setEnterAction(btnAddTimepoint);
    btnAddTimepoint.setToolTipText("Create a bubble by adding a timepoint at the current location");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnAddTimepoint.setMinimumSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
      btnAddTimepoint.setPreferredSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
    } else {
      btnAddTimepoint.setMinimumSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));//(133, 23));
      btnAddTimepoint.setPreferredSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));//(133, 23));
    }
    btnAddTimepoint.setMargin(new Insets(0, 0, 0, 0));
    btnAddTimepoint.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.addTimepoint();
        uilogger.log(UIEventType.BUTTON_CLICKED, "add timepoint");
      }
    });
    btnAddTimepoint.setEnabled(true); // add timepoint always enabled

    // "add marker" button
    btnAddMarker = new JButton("Mark");
    btnAddMarker.setIcon(icoAddMarker);
    btnAddMarker.setFont(timelineFont);
    setEnterAction(btnAddMarker);
    btnAddMarker.setToolTipText("Mark an event at the current location");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnAddMarker.setMinimumSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
      btnAddMarker.setPreferredSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
    } else {
      btnAddMarker.setMinimumSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));//(133, 23));
      btnAddMarker.setPreferredSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));//(133, 23));
    }
    btnAddMarker.setMargin(new Insets(0, 0, 0, 0));
    btnAddMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.addMarker();
        uilogger.log(UIEventType.BUTTON_CLICKED, "add marker");
      }
    });
    btnAddMarker.setEnabled(true); // add marker always enabled

    // "change bubble color" button
    btnChangeColor = new JButton("Color...");
    btnChangeColor.setFont(timelineFont);
    btnChangeColor.setToolTipText("Change the color of the selected bubble(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnChangeColor.setMinimumSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
      btnChangeColor.setPreferredSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
    } else {
      btnChangeColor.setMinimumSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));
      btnChangeColor.setPreferredSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));
    }
    btnChangeColor.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnChangeColor);
    btnChangeColor.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.changeBubbleColor();
        uilogger.log(UIEventType.BUTTON_CLICKED, "change color of selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "delete" bubble button
    btnDeleteBubble = new JButton("Delete");
    btnDeleteBubble.setFont(timelineFont);
    btnDeleteBubble.setToolTipText("Delete the selected bubble(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnDeleteBubble.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnDeleteBubble.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    } else {
      btnDeleteBubble.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnDeleteBubble.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    }
    btnDeleteBubble.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnDeleteBubble);
    btnDeleteBubble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.deleteSelectedBubbles();
        uilogger.log(UIEventType.BUTTON_CLICKED, "delete selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "delete" timepoint or marker button
    btnDeleteTimepointOrMarker = new JButton("Delete");
    btnDeleteTimepointOrMarker.setFont(timelineFont);
    btnDeleteTimepointOrMarker.setToolTipText("Delete the selected timepoint or marker");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnDeleteTimepointOrMarker.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnDeleteTimepointOrMarker.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    } else {
      btnDeleteTimepointOrMarker.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnDeleteTimepointOrMarker.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    }
    btnDeleteTimepointOrMarker.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnDeleteTimepointOrMarker);
    btnDeleteTimepointOrMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline().areTimepointsSelected()) {
          pnlTimeline.deleteSelectedTimepoint();
          uilogger.log(UIEventType.BUTTON_CLICKED, "delete timepoint "
                       + pnlTimeline.getTimeline().getLastTimepointClicked());
        }
        else {
          pnlTimeline.deleteSelectedMarker();
          uilogger.log(UIEventType.BUTTON_CLICKED, "delete marker "
                       + pnlTimeline.getTimeline().getLastMarkerClicked());
        }
      }
    });

    // "edit properties" button
    btnEditProperties = new JButton("Edit Properties...");
    btnEditProperties.setFont(timelineFont);
    btnEditProperties.setToolTipText("Edit the timeline properties");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnEditProperties.setMinimumSize(new Dimension(UIUtilities.scalePixels(120), UIUtilities.scalePixels(23)));
      btnEditProperties.setPreferredSize(new Dimension(UIUtilities.scalePixels(120), UIUtilities.scalePixels(23)));
    } else {
      btnEditProperties.setMinimumSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
      btnEditProperties.setPreferredSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
    }
    btnEditProperties.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnEditProperties);
    btnEditProperties.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.openTimelineProperties();
        uilogger.log(UIEventType.BUTTON_CLICKED, "edit timeline properties");
      }
    });

    // "fit to window" button
    btnFitToWindow = new JButton("Fit to Window");
    btnFitToWindow.setFont(timelineFont);
    btnFitToWindow.setToolTipText("Fit the timeline to the current window size");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnFitToWindow.setMinimumSize(new Dimension(UIUtilities.scalePixels(95), UIUtilities.scalePixels(23)));
      btnFitToWindow.setPreferredSize(new Dimension(UIUtilities.scalePixels(95), UIUtilities.scalePixels(23)));
    } else {
      btnFitToWindow.setMinimumSize(new Dimension(UIUtilities.scalePixels(82), UIUtilities.scalePixels(23)));
      btnFitToWindow.setPreferredSize(new Dimension(UIUtilities.scalePixels(82), UIUtilities.scalePixels(23)));
    }
    btnFitToWindow.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnFitToWindow);
    btnFitToWindow.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.fitToWindow();
        uilogger.log(UIEventType.BUTTON_CLICKED, "fit timeline to window");
      }
    });

    // "group bubbles" button
    btnGroupBubbles = new JButton("Group");
    btnGroupBubbles.setFont(timelineFont);
    btnGroupBubbles.setToolTipText("Group the selected bubbles");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnGroupBubbles.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnGroupBubbles.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    } else {
      btnGroupBubbles.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnGroupBubbles.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    }
    btnGroupBubbles.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnGroupBubbles);
    btnGroupBubbles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.groupSelectedBubbles();
        uilogger.log(UIEventType.BUTTON_CLICKED, "group selected bubbles: "
                     + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "clear all" button
    btnClearAll = new JButton("Clear All");
    btnClearAll.setFont(timelineFont);
    btnClearAll.setToolTipText("Clear all timepoints, markers, and timeline settings");

    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnClearAll.setMinimumSize(new Dimension(UIUtilities.scalePixels(95), UIUtilities.scalePixels(23)));
      btnClearAll.setPreferredSize(new Dimension(UIUtilities.scalePixels(95), UIUtilities.scalePixels(23)));
    } else {
      btnClearAll.setMinimumSize(new Dimension(UIUtilities.scalePixels(82), UIUtilities.scalePixels(23)));
      btnClearAll.setPreferredSize(new Dimension(UIUtilities.scalePixels(82), UIUtilities.scalePixels(23)));
    }
    btnClearAll.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnClearAll);
    btnClearAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.resetTimeline();
      }
    });


    // "edit..." bubble button
    btnEditBubble = new JButton("Edit..."); //("Label / Annotate...");
    btnEditBubble.setIcon(icoEdit);
    btnEditBubble.setFont(timelineFont);
    btnEditBubble.setToolTipText("Edit the label and annotation of the selected bubble");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnEditBubble.setMinimumSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
      btnEditBubble.setPreferredSize(new Dimension(UIUtilities.scalePixels(85), UIUtilities.scalePixels(23)));
    } else {
      btnEditBubble.setMinimumSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23))); //(80, 23));
      btnEditBubble.setPreferredSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23))); //(80, 23));
    }
    btnEditBubble.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnEditBubble);
    btnEditBubble.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          if (pnlTimeline.getTimeline().getSelectedBubbles().size() == 1) { // currenly can only edit one bubble at a time
            pnlTimeline.dlgBubbleEditor = new TimelineBubbleEditor(frmTimeline);
            uilogger.log(UIEventType.BUTTON_CLICKED, "edit bubble: " + pnlTimeline.getTimeline().getSelectedBubbles());
          }
        }
      }
    });

    // "edit label" timepoint button
    btnEditTimepointOrMarker = new JButton("Edit...");
    btnEditTimepointOrMarker.setFont(timelineFont);
    btnEditTimepointOrMarker.setToolTipText("Edit the selected timepoint or marker");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnEditTimepointOrMarker.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnEditTimepointOrMarker.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    } else {
      btnEditTimepointOrMarker.setMinimumSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
      btnEditTimepointOrMarker.setPreferredSize(new Dimension(UIUtilities.scalePixels(65), UIUtilities.scalePixels(23)));
    }
    btnEditTimepointOrMarker.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnEditTimepointOrMarker);
    btnEditTimepointOrMarker.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          if (pnlTimeline.getTimeline().areTimepointsSelected()) {
            dlgTimepointEditor = new TimepointEditor(frmTimeline);
            uilogger.log(UIEventType.BUTTON_CLICKED, "edit timepoint label: " + pnlTimeline.getTimeline().getLastTimepointClicked());
          }
          else if (pnlTimeline.getTimeline().areMarkersSelected()) {
            dlgMarkerEditor = new MarkerEditor(frmTimeline);
            uilogger.log(UIEventType.BUTTON_CLICKED, "edit marker: " + pnlTimeline.getTimeline().getLastMarkerClicked());
          }
        }
      }
    });

    // "ungroup bubbles" button
    btnUngroupBubbles = new JButton("Ungroup");
    btnUngroupBubbles.setFont(timelineFont);
    btnUngroupBubbles.setToolTipText("Ungroup the selected grouped bubbles");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnUngroupBubbles.setMinimumSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));
      btnUngroupBubbles.setPreferredSize(new Dimension(UIUtilities.scalePixels(80), UIUtilities.scalePixels(23)));
    } else {
      btnUngroupBubbles.setMinimumSize(new Dimension(UIUtilities.scalePixels(55), UIUtilities.scalePixels(23)));
      btnUngroupBubbles.setPreferredSize(new Dimension(UIUtilities.scalePixels(55), UIUtilities.scalePixels(23)));
    }
    btnUngroupBubbles.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnUngroupBubbles);
    btnUngroupBubbles.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        pnlTimeline.ungroupSelectedBubbles();
        uilogger.log(UIEventType.BUTTON_CLICKED, "ungroup selected bubbles: " + pnlTimeline.getTimeline().getSelectedBubbles());
      }
    });

    // "zoom to selection" button
    btnZoomTo = new JButton("Zoom to Selection");
    btnZoomTo.setFont(timelineFont);
    btnZoomTo.setToolTipText("Zoom in to the selected bubble(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnZoomTo.setMinimumSize(new Dimension(UIUtilities.scalePixels(120), UIUtilities.scalePixels(23)));
      btnZoomTo.setPreferredSize(new Dimension(UIUtilities.scalePixels(120), UIUtilities.scalePixels(23)));
    } else {
      btnZoomTo.setMinimumSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
      btnZoomTo.setPreferredSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
    }
    btnZoomTo.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnZoomTo);
    btnZoomTo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
        	// pnlTimeline.getTimeline().timelineZoomed = false;
          pnlTimeline.zoomToSelectedBubbles();
         //  pnlTimeline.doResize(pnlTimeline.timelineLength); //// TESTING
          uilogger.log(UIEventType.BUTTON_CLICKED, "zoom to selection: " + pnlTimeline.getTimeline().getSelectedBubbles());
        }
      }
    });

    // "larger font" button
    btnFontLarger = new JButton("");
    btnFontLarger.setIcon(icoBigger);
    btnFontLarger.setFont(timelineFont);
    btnFontLarger.setToolTipText("Make the annotation text larger");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnFontLarger.setMinimumSize(new Dimension(UIUtilities.scalePixels(45), UIUtilities.scalePixels(23)));
      btnFontLarger.setPreferredSize(new Dimension(UIUtilities.scalePixels(45), UIUtilities.scalePixels(23)));
    } else {
      btnFontLarger.setMinimumSize(new Dimension(UIUtilities.scalePixels(40), UIUtilities.scalePixels(23)));
      btnFontLarger.setPreferredSize(new Dimension(UIUtilities.scalePixels(40), UIUtilities.scalePixels(23)));
    }
    btnFontLarger.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnFontLarger);
    btnFontLarger.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          annotationFontSize = annotationFontSize + 2;
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            annotationFont = new Font(unicodeFont, 0, annotationFontSize - 2);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize - 2);
          //  doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false); !!!!!!!!!!!!
          } else {
            annotationFont = new Font(unicodeFont, Font.PLAIN, annotationFontSize);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize);
          //  doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false); !!!!!!!!!!!!!!!!
          }
          if (isDescriptionShowing) {
            showDescription();
          }
          else {
            updateAnnotationPane();
          }
          uilogger.log(UIEventType.BUTTON_CLICKED, "Annotation text larger: " + annotationFontSize);
        }
      }
    });

    // "smaller font" button
    btnFontSmaller = new JButton("");
    btnFontSmaller.setIcon(icoSmaller);
    btnFontSmaller.setFont(timelineFont);
    btnFontSmaller.setToolTipText("Make the annotation text smaller");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      btnFontSmaller.setMinimumSize(new Dimension(UIUtilities.scalePixels(45), UIUtilities.scalePixels(23)));
      btnFontSmaller.setPreferredSize(new Dimension(UIUtilities.scalePixels(45), UIUtilities.scalePixels(23)));
    } else {
      btnFontSmaller.setMinimumSize(new Dimension(UIUtilities.scalePixels(40), UIUtilities.scalePixels(23)));
      btnFontSmaller.setPreferredSize(new Dimension(UIUtilities.scalePixels(40), UIUtilities.scalePixels(23)));
    }
    btnFontSmaller.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(btnFontSmaller);
    btnFontSmaller.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          if (annotationFontSize > 1) {
            annotationFontSize = annotationFontSize - 2;
          }
          if (System.getProperty("os.name").startsWith("Mac OS")) {
            annotationFont = new Font(unicodeFont, 0, annotationFontSize);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize);
           // doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false); !!!!!!!!!!!!!
          } else {
            annotationFont = new Font(unicodeFont, Font.PLAIN, annotationFontSize);
            Style fontStyle = doc.addStyle("FontSize", null);
            StyleConstants.setFontSize(fontStyle, annotationFontSize);
           // doc.setParagraphAttributes(0, doc.getLength(), fontStyle, false); !!!!!!!!
          }
          if (isDescriptionShowing) {
            showDescription();
          }
          else {
            updateAnnotationPane();
          }
          uilogger.log(UIEventType.BUTTON_CLICKED, "Annotation text smaller: " + annotationFontSize);
        }
      }
    });

    // "all levels" radio button
    radAllLevels = new JRadioButton("All Levels");
    radAllLevels.setFont(timelineFont);
    radAllLevels.setToolTipText("Show the annotations for all bubble levels");
    radAllLevels.setSelected(true);
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      radAllLevels.setMinimumSize(new Dimension(UIUtilities.scalePixels(72), UIUtilities.scalePixels(23)));
      radAllLevels.setPreferredSize(new Dimension(UIUtilities.scalePixels(72), UIUtilities.scalePixels(23)));
    } else {
      radAllLevels.setMinimumSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
      radAllLevels.setPreferredSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
    }
    radAllLevels.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(radAllLevels);
    radAllLevels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          showAllAnnotations = true;
          updateAnnotationPane();
          uilogger.log(UIEventType.RADIOBUTTON_PICKED, "show all levels");
        }
      }
    });

    // "selected levels" radio button
    radSelectedLevels = new JRadioButton("Selected Levels");
    radSelectedLevels.setFont(timelineFont);
    radSelectedLevels.setToolTipText("Show the annotations for the selected bubble level(s)");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
      radSelectedLevels.setMinimumSize(new Dimension(UIUtilities.scalePixels(72), UIUtilities.scalePixels(23)));
      radSelectedLevels.setPreferredSize(new Dimension(UIUtilities.scalePixels(72), UIUtilities.scalePixels(23)));
    } else {
      radSelectedLevels.setMinimumSize(new Dimension(UIUtilities.scalePixels(70), UIUtilities.scalePixels(23)));
      radSelectedLevels.setPreferredSize(new Dimension(UIUtilities.scalePixels(70), UIUtilities.scalePixels(23)));
    }
    radSelectedLevels.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(radSelectedLevels);
    radSelectedLevels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          showAllAnnotations = false;
          updateAnnotationPane();
          uilogger.log(UIEventType.RADIOBUTTON_PICKED, "show selected levels");
        }
      }
    });

    // "markers" check boxes
    chkShowMarkers = new JCheckBox("<html><head></head><body>" + "<DIV STYLE='font-size : " + timelineFontSize + "pt; "
                                   + "font-family : " + timelineFont + "'>"
                                   + "Marker Annotations"); // "Show Marker<br>Annotations");
    chkShowMarkers.setFont(timelineFont);
    chkShowMarkers.setSelected(true);
    chkShowMarkers.setToolTipText("Show the current marker annotations");
    if (System.getProperty("os.name").startsWith("Mac OS")) {
    //  chkShowMarkers.setMinimumSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
    //  chkShowMarkers.setPreferredSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
    } else {
    //  chkShowMarkers.setMinimumSize(new Dimension(UIUtilities.scalePixels(90), UIUtilities.scalePixels(23)));
    //  chkShowMarkers.setPreferredSize(new Dimension(UIUtilities.scalePixels(90), UIUtilities.scalePixels(23)));
    }
    chkShowMarkers.setMargin(new Insets(0, 0, 0, 0));
    setEnterAction(chkShowMarkers);
    chkShowMarkers.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (pnlTimeline.getTimeline() != null) {
          updateAnnotationPane();
          uilogger.log(UIEventType.CHECKBOX_SELECTED, "show marker annotations");
        }
      }
    });
    
    
    
    chkShowMarkerTimes = new JCheckBox("<html><head></head><body>" + "<DIV STYLE='font-size : " + timelineFontSize + "pt; "
            + "font-family : " + timelineFont + "'>"
            + "Markers"); // "Show Marker<br>Annotations");
	chkShowMarkerTimes.setFont(timelineFont);
	chkShowMarkerTimes.setSelected(false);
	chkShowMarkerTimes.setToolTipText("Show the marker times below the timeline");
	if (System.getProperty("os.name").startsWith("Mac OS")) {
		//  chkShowMarkers.setMinimumSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
		//  chkShowMarkers.setPreferredSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
	} else {
		//  chkShowMarkers.setMinimumSize(new Dimension(UIUtilities.scalePixels(90), UIUtilities.scalePixels(23)));
		//  chkShowMarkers.setPreferredSize(new Dimension(UIUtilities.scalePixels(90), UIUtilities.scalePixels(23)));
	}
	chkShowMarkerTimes.setMargin(new Insets(0, 0, 0, 0));
	setEnterAction(chkShowMarkerTimes);
	chkShowMarkerTimes.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		if (pnlTimeline.getTimeline() != null) {
			pnlTimeline.showMarkerTimes(chkShowMarkerTimes.isSelected());
			pnlTimeline.refreshTimeline();
			uilogger.log(UIEventType.CHECKBOX_SELECTED, "show marker times");
		}
	  }
	});

    chkShowTimes = new JCheckBox("<html><head></head><body>" + "<DIV STYLE='font-size : " + timelineFontSize + "pt; "
            + "font-family : " + timelineFont + "'>"
            + "Timepoints"); // "Show Timepoint<br>Annotations");
	chkShowTimes.setFont(timelineFont);
	chkShowTimes.setSelected(false);
	chkShowTimes.setToolTipText("Show the timepoint times below the timeline");
	if (System.getProperty("os.name").startsWith("Mac OS")) {
		//  chkShowMarkers.setMinimumSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
		//  chkShowMarkers.setPreferredSize(new Dimension(UIUtilities.scalePixels(100), UIUtilities.scalePixels(23)));
	} else {
		//  chkShowMarkers.setMinimumSize(new Dimension(UIUtilities.scalePixels(90), UIUtilities.scalePixels(23)));
		//  chkShowMarkers.setPreferredSize(new Dimension(UIUtilities.scalePixels(90), UIUtilities.scalePixels(23)));
	}
	chkShowTimes.setMargin(new Insets(0, 0, 0, 0));
	setEnterAction(chkShowTimes);
	chkShowTimes.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		if (pnlTimeline.getTimeline() != null) {
			pnlTimeline.showTimes(chkShowTimes.isSelected());
			pnlTimeline.refreshTimeline();
			uilogger.log(UIEventType.CHECKBOX_SELECTED, "show timepoint times");
		}
	  }
	});


    // add components to show levels panel
    grpShowLevels.add(radAllLevels);
    grpShowLevels.add(radSelectedLevels);
    pnlLevels.add(radAllLevels, BorderLayout.NORTH);
    pnlLevels.add(radSelectedLevels, BorderLayout.CENTER);
    pnlLevels.add(chkShowMarkers, BorderLayout.SOUTH); 
    pnlMarkers.add(chkShowMarkerTimes, BorderLayout.NORTH);
    pnlMarkers.add(chkShowTimes, BorderLayout.SOUTH);
  //  pnlAnnotationTools.add(pnlLevels, null);
    pnlFontButtons.add(btnFontLarger);
    pnlFontButtons.add(btnFontSmaller);

    // add buttons
    TimelineUtilities.createConstraints(pnlElapsedVolume, lblElapsed, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlElapsedVolume, pnlVolumeControl.btnMute, 1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlElapsedVolume, pnlVolumeControl.slideVolume, 2, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnEditBubble, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnDeleteBubble, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnChangeColor, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlBubbleButtons, btnGroupBubbles, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnAddTimepoint, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnAddMarker, 0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnDeleteTimepointOrMarker, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimepointButtons, btnEditTimepointOrMarker, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnEditProperties, 0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnClearAll, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnZoomTo, 0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlTimelineButtons, btnFitToWindow, 1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotations, scrpAnnotations, 0, 0, 1, 3, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotationTools, pnlLevels, 0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotationTools2, pnlMarkers, 1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 2, 2, 2, 2, 0, 0);
    TimelineUtilities.createConstraints(pnlAnnotationTools2, pnlFontButtons, 2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, 2, 2, 2, 2, 0, 0);
  }

  /**
   * returns height of panel
   */
  public int getHeight() {
    return height;
  }

/// methods to enable or disable certain controls

  /**
   * disables controls for selected timepoints
   */
  protected void disableSelectedTimepointControls() {
    btnDeleteTimepointOrMarker.setEnabled(false);
    btnEditTimepointOrMarker.setEnabled(false);
  }

  /**
   * enables controls for selected timepoints
   */
  protected void enableSelectedTimepointControls() {
    btnDeleteTimepointOrMarker.setEnabled(true);
    btnEditTimepointOrMarker.setEnabled(true);
  }

  /**
   * disables adding timepoints
   */
  protected void disableAddTimepoint() {
    btnAddTimepoint.setEnabled(false);
  }

  /**
   * enables adding timepoints
   */
  protected void enableAddTimepoint() {
    btnAddTimepoint.setEnabled(true);
  }

  /**
   * disables adding markers
   */
  protected void disableAddMarker() {
    btnAddMarker.setEnabled(false);
  }

  /**
   * enables adding markers
   */
  protected void enableAddMarker() {
    btnAddMarker.setEnabled(true);
  }

  /**
   * disables controls for bubbles
   */
  protected void disableAllBubbleControls() {
    btnEditBubble.setEnabled(false);
    btnChangeColor.setEnabled(false);
    btnDeleteBubble.setEnabled(false);
    btnZoomTo.setEnabled(false);
    btnGroupBubbles.setEnabled(false);
    btnUngroupBubbles.setEnabled(false);
  }

  /**
   * enables controls for bubbles
   */
  protected void enableAllBubbleControls() {
    btnEditBubble.setEnabled(true);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
    btnGroupBubbles.setEnabled(true);
    btnUngroupBubbles.setEnabled(true);
  }

  /**
   * enables controls for single selected bubbles
   */
  protected void enableSingleSelectedBubbleControls() {
    btnEditBubble.setEnabled(true);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
  }

  /**
   * enables controls for multiple selected grouped bubbles
   */
  protected void enableMultipleSelectedGroupedBubbleControls() {
    btnEditBubble.setEnabled(false);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
    btnUngroupBubbles.setEnabled(true);
  }

  /**
   * enables controls for multiple selected ungrouped bubbles
   */
  protected void enableMultipleSelectedUngroupedBubbleControls() {
    btnEditBubble.setEnabled(false);
    btnChangeColor.setEnabled(true);
    btnDeleteBubble.setEnabled(true);
    btnZoomTo.setEnabled(true);
    btnGroupBubbles.setEnabled(true);
  }

  /**
   * disableAllControls
   */
  protected void disableAllControls (){
    this.disableAllBubbleControls();
    this.disableAddMarker();
    this.disableAddTimepoint();
    this.disableSelectedTimepointControls();
    this.btnEditProperties.setEnabled(false);
    this.btnFitToWindow.setEnabled(false);
    this.btnFontLarger.setEnabled(false);
    this.btnFontSmaller.setEnabled(false);
    this.btnClearAll.setEnabled(false);
    this.radAllLevels.setEnabled(false);
    this.radSelectedLevels.setEnabled(false);
    this.chkShowMarkers.setEnabled(false);
    this.chkShowMarkerTimes.setEnabled(false);
    this.pnlVolumeControl.btnMute.setEnabled(false);
    this.pnlVolumeControl.slideVolume.setEnabled(false);
  }

  /**
   * enableMostControls: enables the controls for when nothing is selected
   */
  protected void enableMostControls (){
    this.enableAddMarker();
    this.enableAddTimepoint();
    this.btnEditProperties.setEnabled(true);
    this.btnFitToWindow.setEnabled(true);
    this.btnFontLarger.setEnabled(true);
    this.btnFontSmaller.setEnabled(true);
    this.btnClearAll.setEnabled(true);
    this.radAllLevels.setEnabled(true);
    this.radSelectedLevels.setEnabled(true);
    this.chkShowMarkers.setEnabled(true);
    this.chkShowMarkerTimes.setEnabled(true);
    this.pnlVolumeControl.btnMute.setEnabled(true);
    this.pnlVolumeControl.slideVolume.setEnabled(true);
  }

  /**
   * setMenuBar: sets the menu bar for the timeline panel
   */
  protected void setMenuBar(TimelineMenuBar tmb) {
    menubTimeline = tmb;
  }

  /**
   * doPlayerDisable: disables all playback controls
   */
  protected void doPlayerDisable() {
    pnlAudioControl.btnPlay.setEnabled(false);
    pnlAudioControl.btnStop.setEnabled(false);
    pnlAudioControl.btnFF.setEnabled(false);
    pnlAudioControl.btnRW.setEnabled(false);
    pnlAudioControl.btnPrev.setEnabled(false);
    pnlAudioControl.btnNext.setEnabled(false);
  }

  /**
   * doPlayerEnable: enables all playback controls
   */
  protected void doPlayerEnable() {
    pnlAudioControl.btnPlay.setEnabled(true);
    pnlAudioControl.btnStop.setEnabled(true);
    pnlAudioControl.btnFF.setEnabled(true);
    pnlAudioControl.btnRW.setEnabled(true);
    pnlAudioControl.btnPrev.setEnabled(true);
    pnlAudioControl.btnNext.setEnabled(true);
  }

  /**
   * setPlayer: sets the player object for the play controls
   */
  protected void setPlayer (TimelinePlayer tp) {
    tPlayer = tp;
    this.enableMostControls();
  }

  /**
   * setLocalPlayer: sets the local player object for the play controls
   */
  protected void setLocalPlayer (TimelineLocalPlayer tlp) {
    tLocalPlayer = tlp;
    this.enableMostControls();
  }

  /// Media Methods

  /**
   * Stop tracking (RW or FF). Called when a MouseReleased event is received from the FF or RW buttons.
   */
  protected void doStopTracking() {
    timeline = pnlTimeline.getTimeline();
    trackingState = TimelineControlPanel.TRACKING_NONE;
    timeline.setPlayerShift(TimelineControlPanel.INITIAL_TRACKING_AMOUNT);
    if (playing || buffering) {     //start playing again if we were before
      timeline.startPlayer();
    } else {
      lblStatus.setText(TimelineControlPanel.STATUS_IDLE);
    }
  }

  /**
   * Initiate RW. Called when the RW button is pressed.
   */
  protected void doRWTracking() {
    timeline = pnlTimeline.getTimeline();
    if (playing || buffering) {     //stop playing for now
      timeline.pausePlayer();
    }
    trackingState = TimelineControlPanel.TRACKING_RW;
    lblStatus.setText(TimelineControlPanel.STATUS_TRACKING);
  }

  /**
   * Initiate FF. Called when the FF button is pressed.
   */
  protected void doFFTracking() {
    timeline = pnlTimeline.getTimeline();
    if (playing || buffering) {     //stop playing for now
      timeline.pausePlayer();
    }
    trackingState = TimelineControlPanel.TRACKING_FF;
    lblStatus.setText(TimelineControlPanel.STATUS_TRACKING);
  }

  /**
   * Volume slider was moved, or volume menu items were selected; update the volume.
   * Note that vol and the volume slider should already be set correctly before this method is called
   */
  protected void doVolumeUpdate() {
    if (!muted) {
      pnlTimeline.getTimeline().setPlayerVolume(vol);
    }
  }

  /**
   * Mute (or unmute) the volume
   */
  protected void doMute() {
    if (muted) {
      muted = false;
      pnlTimeline.getTimeline().setPlayerVolume(vol);
      pnlVolumeControl.btnMute.setIcon(sSpeaker);
    } else {
      muted = true;
      pnlTimeline.getTimeline().setPlayerVolume(0f);
      pnlVolumeControl.btnMute.setIcon(sMute);
    }
  }

  /**
   * setEnterAction: overrides the default enter action for a component
   */
  protected void setEnterAction(JComponent comp) {
    comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "EnterAction");
    comp.getActionMap().put("EnterAction", new AbstractAction() {
 		private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent ae) {
        pnlTimeline.addTimepoint();
      }
    });
    comp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "ControlEnterAction");
    comp.getActionMap().put("ControlEnterAction", new AbstractAction() {
 		private static final long serialVersionUID = 1L;

	public void actionPerformed(ActionEvent ae) {
        pnlTimeline.addMarker();
      }
    });
  }

  /**
   * setAnnotationText: sets the annotation text to the string passed
   */
  protected void setAnnotationText(String str) {
	  if (str!=null) {
		  //tpAnnotations.invalidate();
		  try {
		  //tpAnnotations.removeAll();
		  tpAnnotations.setText("");
		  tpAnnotations.setText(str);

		  //logger.debug("setting annotation text to " + str);
		  } catch (Exception e) {}
	  }
  }

  /**
   * clearAnnotationPane: clears the annotation pane
   */
  protected void clearAnnotationPane() {
	  setAnnotationText("");
  }

  /**
   * showDescription: shows the timeline description in the annotation pane
   */
  protected void showDescription() {
    if (!isDescriptionShowing) {
      tpAnnotations.setContentType("text/html");
      HTMLEditorKit editorKit = new HTMLEditorKit();
      tpAnnotations.setEditorKit(editorKit);
    }
    try {
      clearAnnotationPane();
      isDescriptionShowing = true;

      //HTMLDocument htmldoc = (HTMLDocument)tpAnnotations.getDocument();
      String description = pnlTimeline.getTimeline().getDescription();
      if (description.length() > 0) {
        // create information icon and description
      	String fileSeparator = File.separator;
    	String input = "";
      	if (System.getProperty("os.name").startsWith("Mac OS")) {
          	 input = "resources" + fileSeparator + "media" + fileSeparator + "info.gif";
    	} else {
    		input = "resources/media/info.gif";
    	}
      	InputStream infofile = getClass().getClassLoader().getResourceAsStream(input);
      	Path tempinfo = Files.createTempFile("TempInfo", ".gif");
      	tempinfo.toFile().deleteOnExit();
      	Files.copy(infofile, tempinfo, StandardCopyOption.REPLACE_EXISTING);
      	//File myFile = new File(tempinfo.toFile().getPath());
        //int infoSize = UIUtilities.scalePixels(20);
    	  int infoSize = 20;
        String imageString = tempinfo.toFile().getPath();
        //tpAnnotations.invalidate();
        //tpAnnotations.removeAll(); // NEW
         tpAnnotations.setText("<html><head></head><body>"
                              + "<DIV STYLE='font-size : " + annotationFontSize + "pt; "
                              + "font-family : " + unicodeFont + "'>"
                              + "<img width = '" + infoSize + "' height = '" + infoSize + "' valign=top src = 'file:" + imageString + "'> &nbsp;"
                              + description + "</div></body></html>");
      }
    } catch (Exception e) {
      System.err.println("Error showing description");
    }
    try {
    tpAnnotations.setCaretPosition(0);
    scrpAnnotations.revalidate();
    } catch (Exception e) {}

  }

  /**
   * updateAnnotationPane: displays the currently active annotations in the annotation pane
   */
  public void updateAnnotationPane() {
	  
	if (pnlTimeline.isUpdating) { // avoid overlapping calls to this function! 
	}
	else {pnlTimeline.isUpdating = true; 

	this.clearAnnotationPane();
	
    Timeline timeline = pnlTimeline.getTimeline();
    Vector currentBubbles = new Vector();
    timeline.getNextMarkerOffset();
    isDescriptionShowing = false;
    boolean markerPrecedesOffset = timeline.lastImportantOffsetIsMarker();
    BubbleTreeNode currNode = timeline.getBaseBubbleNode(timeline.getBaseBubbleNumAtCurrentOffset()-1);

    if (showAllAnnotations) { // add base bubble and all parent bubbles to currentbubbles
      currentBubbles.add(currNode);
      while (!((BubbleTreeNode)currNode.getParent()).isRoot()) {
        currNode = (BubbleTreeNode)currNode.getParent();
        if (currNode.getBubble().getAnnotation()!="" || currNode.getBubble().getLabel()!="") {
          currentBubbles.add(currNode);
        }
      }
    }
    else { // add only bubbles from selected levels to currentbubbles

      // get the selected levels, deselect all bubbles, and reselect bubbles at same levels at current offset
      Vector selectedLevels = timeline.getSelectedLevels();
      timeline.clearSelectedBubbles();

      int level = 1;
      if (selectedLevels.contains(Integer.valueOf(level))) { // base level selected?
        currentBubbles.add(currNode);
        timeline.selectBubble(timeline.topBubbleNode.getPreOrderIndex(currNode), 5);
      }
      while (!((BubbleTreeNode)currNode.getParent()).isRoot()) {
        level++;
        currNode = (BubbleTreeNode)currNode.getParent();
        if (selectedLevels.contains(Integer.valueOf(level))) {
          currentBubbles.add(currNode);
          timeline.selectBubble(timeline.topBubbleNode.getPreOrderIndex(currNode), 5);
        }
      }
      //pnlTimeline.refreshTimeline();
      
    }

    try {
     // clearAnnotationPane();
      doc = (StyledDocument)tpAnnotations.getDocument(); 

      if (doc!=null) {
	      //selectedStyle = doc.addStyle("Selected", null);
	      normalStyle = doc.addStyle("Normal", null);
	      boldStyle = doc.addStyle("Bold", null);
	      StyleConstants.setBold(boldStyle, true);
	      StyleConstants.setFontSize(normalStyle, annotationFontSize);
	      StyleConstants.setFontFamily(normalStyle, unicodeFont);
	      StyleConstants.setFontFamily(boldStyle, unicodeFont);
	      doc.insertString(0, "<html><body><span style='margin-bottom:0em; font-size: " + annotationFontSize + "pt; font-family: " + unicodeFont + "'>", normalStyle);
	      for (int i = currentBubbles.size()- 1; i >= 0; i--) {
	        //int prevLength = doc.getLength();
	        Bubble currBubble = ((BubbleTreeNode)currentBubbles.elementAt(i)).getBubble();
	        String currAnnotation = currBubble.getAnnotation();
	        String currLabel = currBubble.getLabel();
	        
	        if (showAllAnnotations && currBubble.isSelected()) { // selected bubble
	        	
		          int indentamt = ((currentBubbles.size()-1-i) * 20);
		          
		          doc.insertString(doc.getLength(), "<div style='margin-top: 0em; margin-bottom: 0em; margin-left: " + indentamt + "px; font-size: " + annotationFontSize + "pt; font-family: " + unicodeFont + ";'>", normalStyle);

		          if (currLabel.equals("") && !currAnnotation.equals("")) { // if there is no label, use "Level 1", etc.
		            doc.insertString(doc.getLength(), "Level " + currBubble.getLevel(), normalStyle);
		          }
		          else if (!currLabel.equals("")){ // or use the label
		            doc.insertString(doc.getLength(), "<b>" + currLabel + "</b>", boldStyle);
		          } // put in the annotation
		          if (!currAnnotation.equals("")) {
		            doc.insertString(doc.getLength(), ": ", normalStyle);
		            doc.insertString(doc.getLength(), currAnnotation, selectedStyle);
		          }
		          //StyleConstants.setLeftIndent(selectedStyle, ((currentBubbles.size()-1-i) * 20));
		          // doc.setParagraphAttributes(prevLength, doc.getLength()-prevLength, selectedStyle, false); !!!!!!!!
	        	}
	        else { // non selected bubble -- or if selected levels is selected, a selected bubble :)
	              int indentamt = ((currentBubbles.size()-1-i) * 20);
	              doc.insertString(doc.getLength(), "<div style='margin-top: 0em; margin-bottom: 0em; margin-left: " + indentamt + "px; font-size: " + annotationFontSize + "pt; font-family: " + unicodeFont + ";'>", normalStyle);
		          if (currLabel.equals("") && !currAnnotation.equals("")) { // if there is no label, use "Level 1", etc.
		            doc.insertString(doc.getLength(), "Level " + currBubble.getLevel(), normalStyle);
		          }
		          else if (!currLabel.equals("")){ // or use the label
		            doc.insertString(doc.getLength(), "<b>" + currLabel + "</b>", boldStyle);
		          } // put in the annotation
		          if (!currAnnotation.equals("")) {
		            doc.insertString(doc.getLength(), ": " + currAnnotation, normalStyle);
		          }
		          //StyleConstants.setLeftIndent(normalStyle, ((currentBubbles.size()-1-i) * 20));
		          //doc.setParagraphAttributes(prevLength, doc.getLength()-prevLength, normalStyle, false); !!!!!!!!
	        }
	
	        if (currLabel.equals("") && currAnnotation.equals("")) {
	          // do not add a line
	        }
	        else {
	          doc.insertString(doc.getLength(), "<br>", normalStyle); //"\n", normalStyle);
	        }
	        
	      }
	      
	      // now add marker, if any
	      if (chkShowMarkers.isSelected() && markerPrecedesOffset) {
		        Marker currMarker = timeline.getMarker(timeline.previousMarkerOffset);
		        if (currMarker != null && currMarker.getAnnotation()!="") {
			          doc.insertString(doc.getLength(), "<div style='margin-top: 0em; margin-left: 0px; font-size: " + annotationFontSize + "pt; font-family: " + unicodeFont + ";'>", normalStyle);
			          //StyleConstants.setLeftIndent(normalStyle, 0);
			          doc.insertString(doc.getLength(), "\u25B2 " + currMarker.getLabel() + ": ", boldStyle);
			          if (currMarker.isSelected()) {
			            doc.insertString(doc.getLength(), currMarker.getAnnotation(), selectedStyle);
			          }
			          else {
			            doc.insertString(doc.getLength(), currMarker.getAnnotation(), normalStyle);
			          }
		        }
	      }
	      
	      
	      doc.insertString(doc.getLength(),  "</span></body></html>", normalStyle);
	      
	      StringWriter output = new StringWriter();
	      try { 	  sek.write( output, doc, 0, doc.getLength()); 
	    	  } catch (Exception exc) { System.err.println("Error getting annotation"); 
	    	  }
	   	  
	      String html = output.toString();
	      	// logger.debug("setting annotations to " + html);
	
	   	  if (html!= null) {
	        tpAnnotations.setText(html);
	   	  }
      }
      
     // tpAnnotations.setVisible(true);
      
    } catch (BadLocationException ble) {
      System.err.println("Error displaying annotation: " + ble);
      // pnlTimeline.refreshTimeline(); // new
    }
    
   // tpAnnotations.setCaretPosition(0);
    scrpAnnotations.revalidate();

  }
	pnlTimeline.isUpdating = false;
  }

}