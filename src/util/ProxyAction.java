package util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextPane;

import org.apache.log4j.Logger;

import ui.timeliner.TimelinePanel;

public class ProxyAction extends AbstractAction {

    private Action action;
    private static Logger log = Logger.getLogger(ProxyAction.class);

    public ProxyAction(Action action) {
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
        JTextPane tpAnnotation = ((JTextPane)e.getSource());
        System.out.println(tpAnnotation.getParent());
        tpAnnotation.revalidate();

    }

}