package ui.common.menu;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import ui.common.UIUtilities;
import util.logging.UILogger;

/**
 * The Help menu 
 */
public class MenuHelp extends JMenu {

	private static final long serialVersionUID = 1L;
	// MENU ITEMS
    public JMenuItem menuiHelpAbout= new JMenuItem();
    public JMenuItem menuiHelpShare = new JMenuItem();
    public JMenuItem menuiHelpOpenHelp = new JMenuItem();

    java.awt.Font helpFont;
    protected UILogger uilogger;
    ImageIcon icoTimeliner = new ImageIcon(getClass().getClassLoader().getResource("resources/common/timeliner.gif"));

    public MenuHelp() {
        menuiHelpAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiHelpAbout_actionPerformed(e);
            }
        });
        menuiHelpShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                menuiHelpShare_actionPerformed(e);
            }
        });
        this.setText("Help");
        menuiHelpAbout.setText("About Audio Timeliner");
        menuiHelpShare.setText("Timeline Share");
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            //Mac specific stuff
            helpFont = UIUtilities.fontMenusMac;
            this.add(menuiHelpShare);
            this.add(menuiHelpAbout);
            menuiHelpAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_DOWN_MASK));
            menuiHelpShare.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_DOWN_MASK));
        } else {
            //Windows specific stuff
            helpFont = UIUtilities.fontMenusWin;
            this.add(menuiHelpShare);
            this.add(menuiHelpAbout);
            this.setMnemonic('h');
            menuiHelpAbout.setMnemonic('a');
            menuiHelpShare.setMnemonic('s');
        }
        this.setFont(helpFont);
        menuiHelpAbout.setFont(helpFont);
        menuiHelpShare.setFont(helpFont);
//        menuiHelpOpenHelp.setFont(helpFont);
    }

    void menuiHelpAbout_actionPerformed(ActionEvent e) {
        try {
          JOptionPane.showMessageDialog(this.getParent().getParent(), new Object[] {"Audio Timeliner" + "\n" +
                                             "Version 3.1.0" + "\n" +
                                             "Copyright 2024" + "\n" + "Brent Yorgason" + "\n" + "Brigham Young University"},
                                             "About Audio Timeliner", JOptionPane.INFORMATION_MESSAGE, icoTimeliner);
        } catch (Exception except) {
            JOptionPane.showMessageDialog(this.getParent().getParent(),
                    "Unable to open about box -- possible version conflict.\n" + except,
                    "About box loading error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void menuiHelpShare_actionPerformed(ActionEvent e) {
    	try {
            Desktop.getDesktop().browse(new URI("https://www.singanewsong.org/audiotimeliner/share.html"));
        } catch (Exception e2) {
            e2.printStackTrace();
        }    }

}
