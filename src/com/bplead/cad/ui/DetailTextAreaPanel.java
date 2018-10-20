/**
 * DetailTextAreaPanel.java 2018年10月17日
 */
package com.bplead.cad.ui;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import priv.lee.cad.ui.AbstractPanel;

/**
 * @author zjw 2018年10月17日下午6:57:56
 */
public class DetailTextAreaPanel extends AbstractPanel {

    private static final String DEFAULT_DISPLAY_CONTENT = "default.display.content";
    private static final long serialVersionUID = -4190468776176013154L;
    private static final String TITLE = "title";
    private final double HEIGHT_PROPORTION = 0.3d;
    private ScrollTextArea scrollArea;
    private final double TEXT_PROPORTION = 0.97d;

    public void clear() {
	// scrollArea.clear ();
	scrollArea.setDefaultText (getResourceMap ().getString (DEFAULT_DISPLAY_CONTENT));
    }

    @Override
    public double getHorizontalProportion() {
	return 0.99d;
    }

    @Override
    public double getVerticalProportion() {
	return 0.15d;
    }

    @Override
    public void initialize() {
	ScrollTextArea.TextAreaDimension dimension = ScrollTextArea.newDimension (getPreferredSize (),TEXT_PROPORTION,
		HEIGHT_PROPORTION);

	scrollArea = new ScrollTextArea (new JScrollPane (),new JTextArea (),dimension);

	scrollArea.setDefaultText (getResourceMap ().getString (DEFAULT_DISPLAY_CONTENT));

	setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		getResourceMap ().getString (TITLE),TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,
		toolkit.getFont ()));

	this.add (scrollArea);
    }

    public void print(String message) {
	scrollArea.append (message);
    }

    public void println(String message) {
	scrollArea.append (message + "\n");
    }

}
