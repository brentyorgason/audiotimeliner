package ui.media;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoundedRangeModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.SliderUI;

import org.apache.log4j.Logger;

import ui.timeliner.TimelineSlider;

/**
 * VariationsWindows Slider UI
 * This class implements an alternate user interface for a slider object that
 * uses the VariationsWindowsLookAndFeel.
 *
 * @author Brent Yorgason
 */

 public class VariationsWindowsSliderUI extends SliderUI {

    protected JSlider slider;

    protected Insets focusInsets = null;
    protected Insets insetCache = null;
    protected boolean leftToRightCache = true;
    protected Rectangle focusRect = null;
    protected Rectangle contentRect = null;
    protected Rectangle labelRect = null;
    protected Rectangle tickRect = null;
    protected Rectangle trackRect = null;
    protected Rectangle thumbRect = null;
    private static Logger log = Logger.getLogger(TimelineSlider.class);

    protected int trackBuffer = 0;  // The distance that the track is from the side of the control

    private static final Dimension PREFERRED_HORIZONTAL_SIZE = new Dimension(200, 21);
    private static final Dimension PREFERRED_VERTICAL_SIZE = new Dimension(21, 200);
    private static final Dimension MINIMUM_HORIZONTAL_SIZE = new Dimension(36, 21);
    private static final Dimension MINIMUM_VERTICAL_SIZE = new Dimension(21, 36);

    private transient boolean isDragging;
    private transient boolean draggingMarker;
    private transient boolean hasVolumeWedge;

    private Vector<Rectangle> labelRects = new Vector<>(0);

    protected TrackListener trackListener;
    protected ChangeListener changeListener;
    protected ComponentListener componentListener;
    protected FocusListener focusListener;
    protected ScrollListener scrollListener;
    protected PropertyChangeListener propertyChangeListener;

    // Colors
    private Color shadowColor;
    private Color highlightColor;
    private Color focusColor;

   protected Color getShadowColor() {
        return shadowColor;
    }

    protected Color getHighlightColor() {
        return highlightColor;
    }

    protected Color getFocusColor() {
        return focusColor;
    }


    // ComponentUI Interface Implementation methods

     public static ComponentUI createUI(JComponent c)    {
        return new VariationsWindowsSliderUI((JSlider)c);
    }

    public VariationsWindowsSliderUI(JSlider c) {
    }

    public void installUI( JComponent c ) {
        slider = (JSlider) c;

        slider.setEnabled(slider.isEnabled());
        slider.setOpaque(true);

        isDragging = false;
        draggingMarker = false;
        hasVolumeWedge = false;

        trackListener = createTrackListener( slider );
        changeListener = createChangeListener( slider );
        componentListener = createComponentListener( slider );
        focusListener = createFocusListener( slider );
        scrollListener = createScrollListener( slider );
        propertyChangeListener = createPropertyChangeListener( slider );

        installDefaults( slider );
        installListeners( slider );
        installKeyboardActions( slider );

        insetCache = slider.getInsets();
        leftToRightCache = VariationsWindowsLookAndFeel.isLeftToRight(slider);
        focusRect = new Rectangle();
        contentRect = new Rectangle();
        labelRect = new Rectangle();
        tickRect = new Rectangle();
        trackRect = new Rectangle();
        thumbRect = new Rectangle();

        calculateGeometry(); // This figures out where the labels, ticks, track, and thumb are.

   }

        public void uninstallUI(JComponent c) {
        if ( c != slider )
            throw new IllegalComponentStateException(
                                                    this + " was asked to deinstall() "
                                                    + c + " when it only knows about "
                                                    + slider + ".");

        LookAndFeel.uninstallBorder(slider);

        uninstallListeners( slider );
        uninstallKeyboardActions(slider);

        focusInsets = null;
        insetCache = null;
        leftToRightCache = true;
        focusRect = null;
        contentRect = null;
        labelRect = null;
        tickRect = null;
        trackRect = null;
        thumbRect = null;
        trackListener = null;
        changeListener = null;
        componentListener = null;
        focusListener = null;
        scrollListener = null;
        propertyChangeListener = null;
        slider = null;
    }

        protected void installDefaults( JSlider slider ) {
        LookAndFeel.installBorder(slider, "Slider.border");
        LookAndFeel.installColors(slider, "Slider.background", "Slider.foreground");
        highlightColor = UIManager.getColor("Slider.highlight");
        shadowColor = UIManager.getColor("Slider.shadow");
        focusColor = UIManager.getColor("Slider.focus");
        focusInsets = (Insets)UIManager.get( "Slider.focusInsets" );
    }

    protected TrackListener createTrackListener( JSlider slider ) {
        return new TrackListener();
    }

    protected ChangeListener createChangeListener( JSlider slider ) {
        return new ChangeHandler();
    }

    protected ComponentListener createComponentListener( JSlider slider ) {
        return new ComponentHandler();
    }

    protected FocusListener createFocusListener( JSlider slider ) {
        return new FocusHandler();
    }

    protected ScrollListener createScrollListener( JSlider slider ) {
        return new ScrollListener();
    }

    protected PropertyChangeListener createPropertyChangeListener( JSlider slider ) {
        return new PropertyChangeHandler();
    }

    protected void installListeners( JSlider slider ) {
        slider.addMouseListener(trackListener);
        slider.addMouseMotionListener(trackListener);
        slider.addFocusListener(focusListener);
        slider.addComponentListener(componentListener);
        slider.addPropertyChangeListener( propertyChangeListener );
        slider.getModel().addChangeListener(changeListener);
    }

    protected void uninstallListeners( JSlider slider ) {
        slider.removeMouseListener(trackListener);
        slider.removeMouseMotionListener(trackListener);
        slider.removeFocusListener(focusListener);
        slider.removeComponentListener(componentListener);
        slider.removePropertyChangeListener( propertyChangeListener );
        slider.getModel().removeChangeListener(changeListener);
    }

    protected void installKeyboardActions( JSlider slider ) {
        InputMap km = getInputMap(JComponent.WHEN_FOCUSED);

        SwingUtilities.replaceUIInputMap(slider, JComponent.WHEN_FOCUSED,
                                       km);
        ActionMap am = getActionMap();

        SwingUtilities.replaceUIActionMap(slider, am);
    }

    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_FOCUSED) {
            return (InputMap)UIManager.get("Slider.focusInputMap");
        }
        return null;
    }

    ActionMap getActionMap() {
          ActionMap  map = createActionMap();
            if (map != null) {
                UIManager.put("Slider.actionMap", map);
            }
        return map;
    }

    ActionMap createActionMap() {
        ActionMap map = new ActionMapUIResource();
        map.put("positiveUnitIncrement", new SharedActionScroller
                (1, false));
        map.put("positiveBlockIncrement", new SharedActionScroller
                (1, false));
        map.put("negativeUnitIncrement", new SharedActionScroller
                (1, false));
        map.put("negativeBlockIncrement", new SharedActionScroller
                (1, false));
        map.put("minScroll", new SharedActionScroller(1, false));
        map.put("maxScroll", new SharedActionScroller(1, false));
       return map;
    }

    protected void uninstallKeyboardActions( JSlider slider ) {
        SwingUtilities.replaceUIActionMap(slider, null);
        SwingUtilities.replaceUIInputMap(slider, JComponent.WHEN_FOCUSED,
                                         null);
    }

     /**
     * Returns the longer dimension of the slide bar.  (The slide bar is only the
     * part that runs directly under the thumb)
     */
    protected int getTrackLength() {
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            return trackRect.width;
        }
        return trackRect.height;
    }

    public Dimension getPreferredHorizontalSize() {
        return PREFERRED_HORIZONTAL_SIZE;
    }

    public Dimension getPreferredVerticalSize() {
        return PREFERRED_VERTICAL_SIZE;
    }

    public Dimension getMinimumHorizontalSize() {
        return MINIMUM_HORIZONTAL_SIZE;
    }

    public Dimension getMinimumVerticalSize() {
        return MINIMUM_VERTICAL_SIZE;
    }

    public Dimension getPreferredSize(JComponent c)    {
        recalculateIfInsetsChanged();
        Dimension d;
        if ( slider.getOrientation() == JSlider.VERTICAL ) {
            d = new Dimension(getPreferredVerticalSize());
            d.width = insetCache.left + insetCache.right;
            d.width += focusInsets.left + focusInsets.right;
            d.width += trackRect.width + tickRect.width + labelRect.width;
        }
        else {
            d = new Dimension(getPreferredHorizontalSize());
            d.height = insetCache.top + insetCache.bottom;
            d.height += focusInsets.top + focusInsets.bottom;
            d.height += trackRect.height + tickRect.height + labelRect.height;
        }

        return d;
    }

    public Dimension getMinimumSize(JComponent c)  {
        recalculateIfInsetsChanged();
        Dimension d;

        if ( slider.getOrientation() == JSlider.VERTICAL ) {
            d = new Dimension(getMinimumVerticalSize());
            d.width = insetCache.left + insetCache.right;
            d.width += focusInsets.left + focusInsets.right;
            d.width += trackRect.width + tickRect.width + labelRect.width;
        }
        else {
            d = new Dimension(getMinimumHorizontalSize());
            d.height = insetCache.top + insetCache.bottom;
            d.height += focusInsets.top + focusInsets.bottom;
            d.height += trackRect.height + tickRect.height + labelRect.height;
        }

        return d;
    }

    public Dimension getMaximumSize(JComponent c) {
        Dimension d = getPreferredSize(c);
        if ( slider.getOrientation() == JSlider.VERTICAL ) {
            d.height = Short.MAX_VALUE;
        }
        else {
            d.width = Short.MAX_VALUE;
        }

        return d;
    }

    protected void calculateGeometry() {
        calculateFocusRect();
        calculateContentRect();
        calculateThumbSize();
        calculateTrackBuffer();
        calculateTrackRect();
        calculateTickRect();
        calculateLabelRect();
        calculateThumbLocation();
    }

    protected void calculateFocusRect() {
        focusRect.x = insetCache.left;
        focusRect.y = insetCache.top;
        focusRect.width = slider.getWidth() - (insetCache.left + insetCache.right);
        focusRect.height = slider.getHeight() - (insetCache.top + insetCache.bottom);
    }

    protected void calculateThumbSize() {
        Dimension size = getThumbSize();
        thumbRect.setSize( size.width, size.height );
    }

    protected void calculateContentRect() {
        contentRect.x = focusRect.x + focusInsets.left;
        contentRect.y = focusRect.y + focusInsets.top;
        contentRect.width = focusRect.width - (focusInsets.left + focusInsets.right);
        contentRect.height = focusRect.height - (focusInsets.top + focusInsets.bottom);
    }

    protected void calculateThumbLocation() {
        if ( slider.getSnapToTicks() ) {
            int sliderValue = slider.getValue();
            int snappedValue = sliderValue;
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();
            int tickSpacing = 0;

            if ( minorTickSpacing > 0 ) {
                tickSpacing = minorTickSpacing;
            }
            else if ( majorTickSpacing > 0 ) {
                tickSpacing = majorTickSpacing;
            }

            if ( tickSpacing != 0 ) {
                // If it's not on a tick, change the value
                if ( (sliderValue - slider.getMinimum()) % tickSpacing != 0 ) {
                    float temp = (float)(sliderValue - slider.getMinimum()) / (float)tickSpacing;
                    int whichTick = Math.round( temp );
                    snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                }

                if( snappedValue != sliderValue ) {
                    slider.setValue( snappedValue );
                }
            }
        }

        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            int valuePosition = xPositionForValue(slider.getValue());

            thumbRect.x = valuePosition - (thumbRect.width / 2);
            thumbRect.y = trackRect.y;
        }
        else {
            int valuePosition = yPositionForValue(slider.getValue());

            thumbRect.x = trackRect.x;
            thumbRect.y = valuePosition - (thumbRect.height / 2);
        }
    }

    protected void calculateTrackBuffer() {
        if ( slider.getPaintLabels() && slider.getLabelTable()  != null ) {
            Component highLabel = getHighestValueLabel();
            Component lowLabel = getLowestValueLabel();

            if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
                trackBuffer = Math.max( highLabel.getBounds().width, lowLabel.getBounds().width ) / 2;
                trackBuffer = Math.max( trackBuffer, thumbRect.width / 2 );
            }
            else {
                trackBuffer = Math.max( highLabel.getBounds().height, lowLabel.getBounds().height ) / 2;
                trackBuffer = Math.max( trackBuffer, thumbRect.height / 2 );
            }
        }
        else {
            if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
                trackBuffer = thumbRect.width / 2;
            }
            else {
                trackBuffer = thumbRect.height / 2;
            }
        }
    }


    protected void calculateTrackRect() {
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            trackRect.x = contentRect.x + trackBuffer;
            trackRect.y = contentRect.y;
            trackRect.width = contentRect.width - (trackBuffer * 2);
            trackRect.height = thumbRect.height;
        }
        else {
            if(VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                trackRect.x = contentRect.x;
            }
            else {
                int tickLength = 0;
                if ( slider.getPaintTicks() ) {
                    tickLength = getTickLength();
                }
                trackRect.x = contentRect.x + tickLength +
                                      getWidthOfWidestLabel();
            }
            trackRect.y = contentRect.y + trackBuffer;
            trackRect.width = thumbRect.width;
            trackRect.height = contentRect.height - (trackBuffer * 2);
        }
    }

    /**
     * Gets the height of the tick area for horizontal sliders and the width of the
     * tick area for vertical sliders.  BasicSliderUI uses the returned value to
     * determine the tick area rectangle.  If you want to give your ticks some room,
     * make this larger than you need and paint your ticks away from the sides in paintTicks().
     */
    protected int getTickLength() {
        return 8;
    }

    protected void calculateTickRect() {
        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            tickRect.x = trackRect.x;
            tickRect.y = trackRect.y + trackRect.height;
            tickRect.width = trackRect.width;
            tickRect.height = getTickLength();

            if ( !slider.getPaintTicks() ) {
                --tickRect.y;
                tickRect.height = 0;
            }
        }
        else {
            if(VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                tickRect.x = trackRect.x + trackRect.width;
                tickRect.width = getTickLength();
            }
            else {
                tickRect.width = getTickLength();
                tickRect.x = trackRect.x - tickRect.width;
            }
            tickRect.y = trackRect.y;
            tickRect.height = trackRect.height;

            if ( !slider.getPaintTicks() ) {
                --tickRect.x;
                tickRect.width = 0;
            }
        }
    }

    protected void calculateLabelRect() {
        if ( slider.getPaintLabels() ) {
            if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
                labelRect.x = tickRect.x - trackBuffer;
                labelRect.y = tickRect.y + tickRect.height;
                labelRect.width = tickRect.width + (trackBuffer * 2);
                labelRect.height = getHeightOfTallestLabel();
            }
            else {
                if(VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                    labelRect.x = tickRect.x + tickRect.width;
                    labelRect.width = getWidthOfWidestLabel();
                }
                else {
                    labelRect.width = getWidthOfWidestLabel();
                    labelRect.x = tickRect.x - labelRect.width;
                }
                labelRect.y = tickRect.y - trackBuffer;
                labelRect.height = tickRect.height + (trackBuffer * 2);
            }
        }
        else {
            if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
                labelRect.x = tickRect.x;
                labelRect.y = tickRect.y + tickRect.height;
                labelRect.width = tickRect.width;
                labelRect.height = 0;
            }
            else {
                if(VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                    labelRect.x = tickRect.x + tickRect.width;
                }
                else {
                    labelRect.x = tickRect.x;
                }
                labelRect.y = tickRect.y;
                labelRect.width = 0;
                labelRect.height = tickRect.height;
            }
        }
    }

     /**
     * Returns the amount that the thumb goes past the slide bar.
     */
    protected int getThumbOverhang() {
        return 0;
    }

    protected Dimension getThumbSize() {
        Dimension size = new Dimension();

        if ( slider.getOrientation() == JSlider.VERTICAL ) {
            size.width = 20;
            size.height = 11;
        }
        else {
            size.width = 11;
            size.height = 20;
        }

        return size;
    }

        public class PropertyChangeHandler implements PropertyChangeListener {
          public void propertyChange( PropertyChangeEvent e ) {
            String propertyName = e.getPropertyName();
             if (propertyName.equals( "orientation" ) ||
                 propertyName.equals( "inverted" ) ||
                 propertyName.equals( "labelTable" )  ||
                 propertyName.equals( "majorTickSpacing" )  ||
                 propertyName.equals( "minorTickSpacing" )  ||
                 propertyName.equals( "paintTicks" )  ||
                 propertyName.equals( "paintTrack" )  ||
                 propertyName.equals( "paintLabels" ) ) {

                calculateGeometry();
            }
            else if ( propertyName.equals( "model" ) ) {
                ((BoundedRangeModel)e.getOldValue()).removeChangeListener( changeListener );
                ((BoundedRangeModel)e.getNewValue()).addChangeListener( changeListener );
                calculateThumbLocation();
                slider.repaint();
            }
        }
    }

    protected int getWidthOfWidestLabel() {
        Dictionary dictionary = slider.getLabelTable();
        int widest = 0;
        if ( dictionary != null ) {
            Enumeration keys = dictionary.keys();
            while ( keys.hasMoreElements() ) {
                Component label = (Component)dictionary.get( keys.nextElement() );
                widest = Math.max( label.getPreferredSize().width, widest );
            }
        }
        return widest;
    }

    protected int getHeightOfTallestLabel() {
        Dictionary dictionary = slider.getLabelTable();
        int tallest = 0;
        if ( dictionary != null ) {
            Enumeration keys = dictionary.keys();
            while ( keys.hasMoreElements() ) {
                Component label = (Component)dictionary.get( keys.nextElement() );
                tallest = Math.max( label.getPreferredSize().height, tallest );
            }
        }
        return tallest;
    }

    protected int getWidthOfHighValueLabel() {
        Component label = getHighestValueLabel();
        int width = 0;

        if ( label != null ) {
            width = label.getPreferredSize().width;
        }

        return width;
    }

    protected int getWidthOfLowValueLabel() {
        Component label = getLowestValueLabel();
        int width = 0;

        if ( label != null ) {
            width = label.getPreferredSize().width;
        }

        return width;
    }

    protected int getHeightOfHighValueLabel() {
        Component label = getHighestValueLabel();
        int height = 0;

        if ( label != null ) {
            height = label.getPreferredSize().height;
        }

        return height;
    }

    protected int getHeightOfLowValueLabel() {
        Component label = getLowestValueLabel();
        int height = 0;

        if ( label != null ) {
            height = label.getPreferredSize().height;
        }

        return height;
    }

    protected boolean drawInverted() {
        if (slider.getOrientation()==JSlider.HORIZONTAL) {
            if(VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                return slider.getInverted();
            } else {
                return !slider.getInverted();
            }
        } else {
            return slider.getInverted();
        }
    }

    /**
     * Returns the label that corresponds to the highest slider value in the label table.
     * @see JSlider#setLabelTable
     */
    protected Component getLowestValueLabel() {
        Dictionary dictionary = slider.getLabelTable();
        Component label = null;

        if ( dictionary != null ) {
            Enumeration keys = dictionary.keys();
            if ( keys.hasMoreElements() ) {
                int lowestValue = ((Integer)keys.nextElement()).intValue();

                while ( keys.hasMoreElements() ) {
                    int value = ((Integer)keys.nextElement()).intValue();
                    lowestValue = Math.min( value, lowestValue );
                }

                label = (Component)dictionary.get( new Integer( lowestValue ) );
            }
        }

        return label;
    }

    /**
     * Returns the label that corresponds to the lowest slider value in the label table.
     * @see JSlider#setLabelTable
     */
    protected Component getHighestValueLabel() {
        Dictionary dictionary = slider.getLabelTable();
        Component label = null;

        if ( dictionary != null ) {
            Enumeration keys = dictionary.keys();
            if ( keys.hasMoreElements() ) {
                int highestValue = ((Integer)keys.nextElement()).intValue();

                while ( keys.hasMoreElements() ) {
                    int value = ((Integer)keys.nextElement()).intValue();
                    highestValue = Math.max( value, highestValue );
                }

                label = (Component)dictionary.get( new Integer( highestValue ) );
            }
        }

        return label;
    }

    public void paint( Graphics g, JComponent c )   {
        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        Rectangle clip = g.getClipBounds();

        if ( slider.getPaintTrack() && clip.intersects( trackRect ) ) {
            paintTrack( g );
        }
        if ( slider.getPaintTicks() && clip.intersects( tickRect ) ) {
            paintTicks( g );
        }
        if ( slider.getPaintLabels() && clip.intersects( labelRect ) ) {
            paintLabels( g );
        }
        if ( slider.hasFocus() && clip.intersects( focusRect ) ) {
            paintFocus( g );
        }
        if ( clip.intersects( thumbRect ) ) {
            paintThumb( g );
        }
    }

    protected void recalculateIfInsetsChanged() {
        Insets newInsets = slider.getInsets();
        if ( !newInsets.equals( insetCache ) ) {
            insetCache = newInsets;
            calculateGeometry();
        }
    }

    protected void recalculateIfOrientationChanged() {
        boolean ltr = VariationsWindowsLookAndFeel.isLeftToRight(slider);
        if ( ltr!=leftToRightCache ) {
            leftToRightCache = ltr;
            calculateGeometry();
        }
    }

    public void paintFocus(Graphics g)  {
        g.setColor( getFocusColor() );

        VariationsWindowsLookAndFeel.drawDashedRect( g, focusRect.x, focusRect.y,
                                           focusRect.width, focusRect.height );
    }

    public void paintTrack(Graphics g)  {
        int cx, cy, cw, ch;
        int pad;

        Rectangle trackBounds = trackRect;

        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            pad = trackBuffer;
            cx = pad;
            cy = (trackBounds.height / 2) - 2;
            cw = trackBounds.width;

            g.translate(trackBounds.x, trackBounds.y + cy);

            g.setColor(getShadowColor());
            g.drawLine(0, 0, cw - 1, 0);
            g.drawLine(0, 1, 0, 2);
            g.setColor(getHighlightColor());
            g.drawLine(0, 3, cw, 3);
            g.drawLine(cw, 0, cw, 3);
            g.setColor(Color.black);
            g.drawLine(1, 1, cw-2, 1);

            // add wedge for volume sliders
            if (this.hasVolumeWedge) {
              RenderingHints renderHints =  new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
              renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
              renderHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
              Graphics2D g2d = (Graphics2D)g;
              g2d.setRenderingHints(renderHints);
              g2d.setColor(Color.gray);
              g2d.drawLine(0, 0, cw - 2, -7);
              g2d.drawLine(cw - 2, -7, cw - 2, 0);
            }

            g.translate(-trackBounds.x, -(trackBounds.y + cy));

        }
        else {
            pad = trackBuffer;
            cx = (trackBounds.width / 2) - 2;
            cy = pad;
            ch = trackBounds.height;

            g.translate(trackBounds.x + cx, trackBounds.y);

            g.setColor(getShadowColor());
            g.drawLine(0, 0, 0, ch - 1);
            g.drawLine(1, 0, 2, 0);
            g.setColor(getHighlightColor());
            g.drawLine(3, 0, 3, ch);
            g.drawLine(0, ch, 3, ch);
            g.setColor(Color.black);
            g.drawLine(1, 1, 1, ch-2);

            g.translate(-(trackBounds.x + cx), -trackBounds.y);
        }
    }

    public void paintTicks(Graphics g)  {
        Rectangle tickBounds = tickRect;
        int i;
        int maj, min, max;
        int w = tickBounds.width;
        int h = tickBounds.height;
        //int centerEffect, tickHeight;

        g.setColor(slider.getBackground());
        g.fillRect(tickBounds.x, tickBounds.y, w, h);
        g.setColor(Color.black);

        maj = slider.getMajorTickSpacing();
        min = slider.getMinorTickSpacing();

        if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
           g.translate( 0, tickBounds.y);

            int value = slider.getMinimum();
            int xPos = 0;

            if ( slider.getMinorTickSpacing() > 0 ) {
                while ( value <= slider.getMaximum() ) {
                    xPos = xPositionForValue( value );
                    paintMinorTickForHorizSlider( g, tickBounds, xPos );
                    value += slider.getMinorTickSpacing();
                }
            }

            if ( slider.getMajorTickSpacing() > 0 ) {
                value = slider.getMinimum();

                while ( value <= slider.getMaximum() ) {
                    xPos = xPositionForValue( value );
                    paintMajorTickForHorizSlider( g, tickBounds, xPos );
                    value += slider.getMajorTickSpacing();
                }
            }

            g.translate( 0, -tickBounds.y);
        }
        else {
           g.translate(tickBounds.x, 0);

            int value = slider.getMinimum();
            int yPos = 0;

            if ( slider.getMinorTickSpacing() > 0 ) {
                int offset = 0;
                if(!VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                    offset = tickBounds.width - tickBounds.width / 2;
                    g.translate(offset, 0);
                }

                while ( value <= slider.getMaximum() ) {
                    yPos = yPositionForValue( value );
                    paintMinorTickForVertSlider( g, tickBounds, yPos );
                    value += slider.getMinorTickSpacing();
                }

                if(!VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                    g.translate(-offset, 0);
                }
            }

            if ( slider.getMajorTickSpacing() > 0 ) {
                value = slider.getMinimum();
                if(!VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                    g.translate(2, 0);
                }

                while ( value <= slider.getMaximum() ) {
                    yPos = yPositionForValue( value );
                    paintMajorTickForVertSlider( g, tickBounds, yPos );
                    value += slider.getMajorTickSpacing();
                }

                if(!VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                    g.translate(-2, 0);
                }
            }
            g.translate(-tickBounds.x, 0);
        }
    }

    protected void paintMinorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        g.drawLine( x, 0, x, tickBounds.height / 2 - 1 );
    }

    protected void paintMajorTickForHorizSlider( Graphics g, Rectangle tickBounds, int x ) {
        g.drawLine( x, 0, x, tickBounds.height - 2 );
    }

    protected void paintMinorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        g.drawLine( 0, y, tickBounds.width / 2 - 1, y );
    }

    protected void paintMajorTickForVertSlider( Graphics g, Rectangle tickBounds, int y ) {
        g.drawLine( 0, y,  tickBounds.width - 2, y );
    }


    public void paintLabels( Graphics g ) {
        Rectangle labelBounds = labelRect;

        Dictionary dictionary = slider.getLabelTable();
        if ( dictionary != null ) {
          labelRects.clear();
            Enumeration keys = dictionary.keys();
            while ( keys.hasMoreElements() ) {
                Integer key = (Integer)keys.nextElement();
                Component label = (Component)dictionary.get( key );

                if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
                    g.translate( 0, (int)(thumbRect.getY() + thumbRect.getHeight()+ 1));
                    paintHorizontalLabel( g, key.intValue(), label );
                    g.translate( 0, -(int)(thumbRect.getY() + thumbRect.getHeight() + 1));
                }
                else {
                    int offset = 0;
                    if(!VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                        offset = labelBounds.width -
                                      label.getPreferredSize().width;
                    }
                    g.translate( labelBounds.x + offset, 0 );
                    paintVerticalLabel( g, key.intValue(), label );
                    g.translate( -labelBounds.x - offset, 0 );
                }
            }
        }

    }

    /**
     * labelClicked recieves a point, determines if it is within a label, and returns the index
     * of the label clicked
     */
    private int labelClicked (Point p) {
      for (int i = 0; i < labelRects.size(); i++) {
        if (((Rectangle)(labelRects.elementAt(i))).contains(p)) {
          return i + 1;
        }
      }
      return -1;
    }

    /**
     * getLabelPosition returns the x position (slider value) of the label passed
     */
    private int getLabelPosition(int labelNum) {
      Dictionary dictionary = slider.getLabelTable();
      if ( dictionary != null ) {
        Enumeration keys = dictionary.keys();
        int count = 0;
        while ( keys.hasMoreElements() ) {
          Integer key = (Integer)keys.nextElement();
          count++;
          if (count == labelNum) {
            return key.intValue();
          }
        }
      }
      return -1;
    }

    /**
     * Called for every label in the label table.  Used to draw the labels for horizontal sliders.
     * The graphics have been translated to labelRect.y already.
     * @see JSlider#setLabelTable
     */
    protected void paintHorizontalLabel( Graphics g, int value, Component label ) {
        int labelCenter = xPositionForValue( value );
        int labelLeft = labelCenter - (label.getPreferredSize().width / 2);
        Rectangle lblRect = new Rectangle(labelLeft, (int)(thumbRect.getY() + thumbRect.getHeight()+ 1), (int)label.getPreferredSize().getWidth(), (int)label.getPreferredSize().getHeight());
        if (!labelRects.contains(lblRect)) {
          labelRects.add(lblRect);
        }
        g.translate( labelLeft, 0 );
        label.paint( g );
        g.translate( -labelLeft, 0 );
    }

    /**
     * Called for every label in the label table.  Used to draw the labels for vertical sliders.
     * The graphics have been translated to labelRect.x already.
     * @see JSlider#setLabelTable
     */
    protected void paintVerticalLabel( Graphics g, int value, Component label ) {
        int labelCenter = yPositionForValue( value );
        int labelTop = labelCenter - (label.getPreferredSize().height / 2);
        g.translate( 0, labelTop );
        label.paint( g );
        g.translate( 0, -labelTop );
    }

    public void paintThumb(Graphics g)  {
        Rectangle knobBounds = thumbRect;
        int w = knobBounds.width;
        int h = knobBounds.height;

        g.translate(knobBounds.x, knobBounds.y);

        if ( slider.isEnabled() ) {
            g.setColor(slider.getBackground());
        }
        else {
            g.setColor(slider.getBackground().darker());
        }

        if ( !slider.getPaintTicks() ) {

            // "plain" version
            g.fillRect(0, 0, w, h);

            g.setColor(Color.black);
            g.drawLine(0, h-1, w-1, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.setColor(highlightColor);
            g.drawLine(0, 0, 0, h-2);
            g.drawLine(1, 0, w-2, 0);

            g.setColor(shadowColor);
            g.drawLine(1, h-2, w-2, h-2);
            g.drawLine(w-2, 1, w-2, h-3);
        }
        else if ( slider.getOrientation() == JSlider.HORIZONTAL ) {
            int cw = w / 2;
            g.fillRect(1, 1, w-3, h-1-cw);
            Polygon p = new Polygon();
            p.addPoint(1, h-cw);
            p.addPoint(cw-1, h-1);
            p.addPoint(w-2, h-1-cw);
            g.fillPolygon(p);

            g.setColor(highlightColor);
            g.drawLine(0, 0, w-2, 0);
            g.drawLine(0, 1, 0, h-1-cw);
            g.drawLine(0, h-cw, cw-1, h-1);

            g.setColor(Color.black);
            g.drawLine(w-1, 0, w-1, h-2-cw);
            g.drawLine(w-1, h-1-cw, w-1-cw, h-1);

            g.setColor(shadowColor);
            g.drawLine(w-2, 1, w-2, h-2-cw);
            g.drawLine(w-2, h-1-cw, w-1-cw, h-2);
        }
        else {  // vertical
            int cw = h / 2;
            if(VariationsWindowsLookAndFeel.isLeftToRight(slider)) {
                  g.fillRect(1, 1, w-1-cw, h-3);
                  Polygon p = new Polygon();
                  p.addPoint(w-cw-1, 0);
                  p.addPoint(w-1, cw);
                  p.addPoint(w-1-cw, h-2);
                  g.fillPolygon(p);

                  g.setColor(highlightColor);
                  g.drawLine(0, 0, 0, h - 2);                  // left
                  g.drawLine(1, 0, w-1-cw, 0);                 // top
                  g.drawLine(w-cw-1, 0, w-1, cw);              // top slant

                  g.setColor(Color.black);
                  g.drawLine(0, h-1, w-2-cw, h-1);             // bottom
                  g.drawLine(w-1-cw, h-1, w-1, h-1-cw);        // bottom slant

                  g.setColor(shadowColor);
                  g.drawLine(1, h-2, w-2-cw,  h-2 );         // bottom
                  g.drawLine(w-1-cw, h-2, w-2, h-cw-1 );     // bottom slant
            }
            else {
                  g.fillRect(5, 1, w-1-cw, h-3);
                  Polygon p = new Polygon();
                  p.addPoint(cw, 0);
                  p.addPoint(0, cw);
                  p.addPoint(cw, h-2);
                  g.fillPolygon(p);

                  g.setColor(highlightColor);
                  g.drawLine(cw-1, 0, w-2, 0);             // top
                  g.drawLine(0, cw, cw, 0);                // top slant

                  g.setColor(Color.black);
                  g.drawLine(0, h-1-cw, cw, h-1 );         // bottom slant
                  g.drawLine(cw, h-1, w-1, h-1);           // bottom

                  g.setColor(shadowColor);
                  g.drawLine(cw, h-2, w-2,  h-2 );         // bottom
                  g.drawLine(w-1, 1, w-1,  h-2 );          // right
            }
        }

        g.translate(-knobBounds.x, -knobBounds.y);
    }

    // Used exclusively by setThumbLocation()
    private static Rectangle unionRect = new Rectangle();

    public void setThumbLocation(int x, int y)  {
        unionRect.setBounds( thumbRect );

        thumbRect.setLocation( x, y );

        SwingUtilities.computeUnion( thumbRect.x, thumbRect.y, thumbRect.width, thumbRect.height, unionRect );
        slider.repaint( unionRect.x, unionRect.y, unionRect.width, unionRect.height );
    }

    /**
     * I have provided this alternate method to have the thumb jump to the click on the track,
     * rather than scrolling by block --Brent
     *
     * I might want to add checks to see whether a label was clicked and have it go precisely
     * to the location of a track or a bookmark, but this will not be the behavior for
     * a marker on the timeline, which needs to be draggable
     */
    protected void jumpDueToClickInTrack(int mouseX) {

      synchronized(slider) {
         slider.setValue(valueForXPosition(mouseX));
      }
    }

    protected int xPositionForValue( int value )    {
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        int trackLength = trackRect.width;
        int valueRange = slider.getMaximum() - slider.getMinimum();
        double pixelsPerValue = (double)trackLength / (double)valueRange;
        int trackLeft = trackRect.x;
        int trackRight = trackRect.x + (trackRect.width - 1);
        int xPosition;

        if ( !drawInverted() ) {
            xPosition = trackLeft;
            xPosition += Math.round( pixelsPerValue * (double)(value - min) );
        }
        else {
            xPosition = trackRight;
            xPosition -= Math.round( pixelsPerValue * (double)(value - min) );
        }

        xPosition = Math.max( trackLeft, xPosition );
        xPosition = Math.min( trackRight, xPosition );
        
        return xPosition;
    }

    protected int yPositionForValue( int value )  {
        int min = slider.getMinimum();
        int max = slider.getMaximum();
        int trackLength = trackRect.height;
        int valueRange = slider.getMaximum() - slider.getMinimum();
        double pixelsPerValue = (double)trackLength / (double)valueRange;
        int trackTop = trackRect.y;
        int trackBottom = trackRect.y + (trackRect.height - 1);
        int yPosition;

        if ( !drawInverted() ) {
            yPosition = trackTop;
            yPosition += Math.round( pixelsPerValue * (double)(max - value ) );
        }
        else {
            yPosition = trackTop;
            yPosition += Math.round( pixelsPerValue * (double)(value - min) );
        }

        yPosition = Math.max( trackTop, yPosition );
        yPosition = Math.min( trackBottom, yPosition );

        return yPosition;
    }

    /**
     * Returns a value give a y position.  If yPos is past the track at the top or the
     * bottom it will set the value to the min or max of the slider, depending if the
     * slider is inverted or not.
     */
    public int valueForYPosition( int yPos ) {
        int value;
        final int minValue = slider.getMinimum();
        final int maxValue = slider.getMaximum();
        final int trackLength = trackRect.height;
        final int trackTop = trackRect.y;
        final int trackBottom = trackRect.y + (trackRect.height - 1);

        if ( yPos <= trackTop ) {
            value = drawInverted() ? minValue : maxValue;
        }
        else if ( yPos >= trackBottom ) {
            value = drawInverted() ? maxValue : minValue;
        }
        else {
            int distanceFromTrackTop = yPos - trackTop;
            int valueRange = maxValue - minValue;
            double valuePerPixel = (double)valueRange / (double)trackLength;
            int valueFromTrackTop = (int)Math.round( distanceFromTrackTop * valuePerPixel );

            value = drawInverted() ? minValue + valueFromTrackTop : maxValue - valueFromTrackTop;
        }

        return value;
    }

    /**
     * Returns a value give an x position.  If xPos is past the track at the left or the
     * right it will set the value to the min or max of the slider, depending if the
     * slider is inverted or not.
     */
    public int valueForXPosition( int xPos ) {
        int value;
        final int minValue = slider.getMinimum();
        final int maxValue = slider.getMaximum();
        final int trackLength = trackRect.width;
        final int trackLeft = trackRect.x;
        final int trackRight = trackRect.x + (trackRect.width - 1);

        if ( xPos <= trackLeft ) {
            value = drawInverted() ? maxValue : minValue;
        }
        else if ( xPos >= trackRight ) {
            value = drawInverted() ? minValue : maxValue;
        }
        else {
            int distanceFromTrackLeft = xPos - trackLeft;
            int valueRange = maxValue - minValue;
            double valuePerPixel = (double)valueRange / (double)trackLength;
            int valueFromTrackLeft = (int)Math.round( distanceFromTrackLeft * valuePerPixel );

            value = drawInverted() ? maxValue - valueFromTrackLeft :
              minValue + valueFromTrackLeft;
        }

        return value;
    }

    /////////////////////////////////////////////////////////////////////////
    /// Model Listener Class
    /////////////////////////////////////////////////////////////////////////
    /**
     * Data model listener.
     *
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class ChangeHandler implements ChangeListener {
        public void stateChanged(ChangeEvent e)                {
            if ( !isDragging ) {
                calculateThumbLocation();
                slider.repaint();
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////
    /// Track Listener Class
    /////////////////////////////////////////////////////////////////////////
    /**
     * Track mouse movements.
     *
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class TrackListener extends MouseInputAdapter {
        protected transient int offset;
        protected transient int currentMouseX, currentMouseY;

        public void mouseReleased(MouseEvent e)                {
            if ( !slider.isEnabled() )
                return;

            offset = 0;

            // This is the way we have to determine snap-to-ticks.  It's hard to explain
            // but since ChangeEvents don't give us any idea what has changed we don't
            // have a way to stop the thumb bounds from being recalculated.  Recalculating
            // the thumb bounds moves the thumb over the current value (i.e., snapping
            // to the ticks).
            if ( slider.getSnapToTicks() /*|| slider.getSnapToValue()*/ ) {
                isDragging = false;
                slider.setValueIsAdjusting(false);
            }
            else {
                slider.setValueIsAdjusting(false);
                isDragging = false;
            }
            slider.repaint();
        }

        /**
        * If the mouse is pressed above the "thumb" component
        * then reduce the scrollbars value by one page ("page up"),
        * otherwise increase it by one page.  If there is no
        * thumb then page up if the mouse is in the upper half
        * of the track.
        */
        public void mousePressed(MouseEvent e)                {
            if ( !slider.isEnabled() )
                return;

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            slider.requestFocus();

            // Clicked in the Thumb area?
            if ( thumbRect.contains(currentMouseX, currentMouseY) ) {
                switch ( slider.getOrientation() ) {
                case JSlider.VERTICAL:
                    offset = currentMouseY - thumbRect.y;
                    break;
                case JSlider.HORIZONTAL:
                    offset = currentMouseX - thumbRect.x;
                    break;
                }
                isDragging = true;
                slider.setValueIsAdjusting(true);
                return;
            }
            else {
             isDragging = false;
             slider.setValueIsAdjusting(true);
           //  Dimension sbSize = slider.getSize();

              int lblClicked = labelClicked(new Point(currentMouseX, currentMouseY));

              if (lblClicked != -1) {  // they clicked on a label (bookmark or track tick)
                int labelX = getLabelPosition(lblClicked);
                slider.setValue(labelX);
              }
              else {
                jumpDueToClickInTrack(currentMouseX);
              }
            }
       }

        public boolean shouldScroll(int direction) {
            Rectangle r = thumbRect;
            if ( slider.getOrientation() == JSlider.VERTICAL ) {
                if ( drawInverted() ? direction < 0 : direction > 0 ) {
                    if ( r.y + r.height  <= currentMouseY ) {
                        return false;
                    }
                }
                else if ( r.y >= currentMouseY ) {
                    return false;
                }
            }
            else {
                if ( drawInverted() ? direction < 0 : direction > 0 ) {
                    if ( r.x + r.width  >= currentMouseX ) {
                        return false;
                    }
                }
                else if ( r.x <= currentMouseX ) {
                    return false;
                }
            }

            if ( direction > 0 && slider.getValue() + slider.getExtent() >= slider.getMaximum() ) {
                return false;
            }
            else if ( direction < 0 && slider.getValue() <= slider.getMinimum() ) {
                return false;
            }

            return false; // should NEVER scroll in this version
        }

        /**
        * Set the models value to the position of the top/left
        * of the thumb relative to the origin of the track.
        */
        public void mouseDragged( MouseEvent e ) {
            int thumbMiddle = 0;

            if ( !slider.isEnabled() )
                return;

            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if ( !isDragging )
                return;

            switch ( slider.getOrientation() ) {
            case JSlider.VERTICAL:
                int halfThumbHeight = thumbRect.height / 2;
                int thumbTop = e.getY() - offset;
                int trackTop = trackRect.y;
                int trackBottom = trackRect.y + (trackRect.height - 1);

                thumbTop = Math.max( thumbTop, trackTop - halfThumbHeight );
                thumbTop = Math.min( thumbTop, trackBottom - halfThumbHeight );

                setThumbLocation(thumbRect.x, thumbTop);

                thumbMiddle = thumbTop + halfThumbHeight;
                slider.setValue( valueForYPosition( thumbMiddle ) );
                break;
            case JSlider.HORIZONTAL:
                int halfThumbWidth = thumbRect.width / 2;
                int thumbLeft = e.getX() - offset;
                int trackLeft = trackRect.x;
                int trackRight = trackRect.x + (trackRect.width - 1);

                thumbLeft = Math.max( thumbLeft, trackLeft - halfThumbWidth );
                thumbLeft = Math.min( thumbLeft, trackRight - halfThumbWidth );

                setThumbLocation( thumbLeft, thumbRect.y);

                thumbMiddle = thumbLeft + halfThumbWidth;
                slider.setValue( valueForXPosition( thumbMiddle ) );
                break;
            default:
                return;
            }
        }

        public void mouseMoved(MouseEvent e)    {}

    }

    /**
     * Scroll-event listener.
     *
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class ScrollListener implements ActionListener {
        boolean useBlockIncrement;

        public ScrollListener() {
            useBlockIncrement = true;
        }

        public ScrollListener(int dir, boolean block)   {
            useBlockIncrement = block;
        }


        public void actionPerformed(ActionEvent e) {
        }
    };

    /**
     * Listener for resizing events.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class ComponentHandler extends ComponentAdapter {
        public void componentResized(ComponentEvent e)  {
            calculateGeometry();
            slider.repaint();
        }
    };

    /**
     * Focus-change listener.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class FocusHandler implements FocusListener {
        public void focusGained(FocusEvent e) { slider.repaint();}
        public void focusLost(FocusEvent e) { slider.repaint();}
    };

    /**
     * As of Java 2 platform v1.3 this undocumented class is no longer used.
     * The recommended approach to creating bindings is to use a
     * combination of an <code>ActionMap</code>, to contain the action,
     * and an <code>InputMap</code> to contain the mapping from KeyStroke
     * to action description. The InputMap is is usually described in the
     * LookAndFeel tables.
     * <p>
     * Please refer to the key bindings specification for further details.
     * <p>
     * This inner class is marked &quot;public&quot; due to a compiler bug.
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of <Foo>.
     */
    public class ActionScroller extends AbstractAction {

		private static final long serialVersionUID = 1L;
		int dir;
        boolean block;
        JSlider slider;

        public ActionScroller( JSlider slider, int dir, boolean block) {
            this.dir = dir;
            this.block = block;
            this.slider = slider;
        }

        public void actionPerformed(ActionEvent e) {
       }
        public boolean isEnabled() {
            boolean b = true;
            if (slider != null) {
                b = slider.isEnabled();
            }
            return b;
        }

    };


    /**
     * A static version of the above.
     */
    static class SharedActionScroller extends AbstractAction {

		private static final long serialVersionUID = 1L;
		int dir;
        boolean block;

        public SharedActionScroller(int dir, boolean block) {
            this.dir = dir;
            this.block = block;
        }

        public void actionPerformed(ActionEvent e) {
     }
    }
    public void setDraggableThumb(boolean state) {
      slider.setEnabled(state);
      this.isDragging = false;
    }

    public void hasVolumeWedge(boolean state) {
      this.hasVolumeWedge = state;
    }
}
