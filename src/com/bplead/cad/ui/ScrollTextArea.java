/**
 * ScrollTextArea.java 2018年10月17日
 */
package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.constant.RemoteMethod;

import priv.lee.cad.model.MiddleAlignGap;

/**
 * 2018年10月17日下午7:33:02
 */
public class ScrollTextArea extends JComponent {

    private static final Logger logger = Logger.getLogger (ScrollTextArea.class);

    private static final long serialVersionUID = 7933930654658273273L;

    public static TextAreaDimension newDimension(Dimension parentSize, double textWidthProportion,
	    double heightProportion) {
	return new TextAreaDimension (parentSize,textWidthProportion,heightProportion);
    }

    private TextAreaDimension dimension;

    private MiddleAlignGap gap = new MiddleAlignGap (5,5);

    private LayoutManager layout = new FlowLayout (FlowLayout.LEFT,gap.hGap,gap.vGap);

    private JScrollPane scrollPane;

    private JTextArea textArea;
    
    private final double TEXTAREA_HEIGHT_PROPORTION = 3d;
    
    private final double SCROLL_HEIGHT_PROPORTION = 2d;

    ScrollTextArea(JScrollPane scrollPane, JTextArea textArea, TextAreaDimension dimension) {
	this.textArea = textArea;
	this.scrollPane = scrollPane;
	this.dimension = dimension;

	initialize ();
    }

    public void append(String text) {
	textArea.append (text);
	// textArea.paintImmediately(textArea.getX(), textArea.getY(),
	// textArea.getWidth(), textArea.getHeight());
	textArea.paintImmediately (textArea.getBounds ());
    }

    public void clear() {
	textArea.setText ("");
    }

    public TextAreaDimension getDimension() {
	return dimension;
    }

    public boolean getEditEnable() {
	return textArea.isEditable ();
    }

    public String getText() {
	return textArea.getText ();
    }

    private ScrollTextArea initialize() {
	
	setLayout (layout);

	if (dimension != null) {
	    if (logger.isDebugEnabled ()) {
		logger.debug ("dimension is -> " + dimension);
	    }
	    textArea.setPreferredSize (new Dimension (dimension.textWidth,((Double)(dimension.height * TEXTAREA_HEIGHT_PROPORTION)).intValue ()));
	}
	textArea.setLineWrap (true);
	textArea.setCaretPosition (textArea.getText ().length ());
	//set textarea border
	if (RemoteMethod.VERBOSE) {
	    textArea.setBorder (BorderFactory.createLineBorder (Color.BLUE));
	}

	scrollPane.setViewportView (textArea);
	scrollPane.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	scrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	scrollPane.setWheelScrollingEnabled (true);
	scrollPane.setPreferredSize (new Dimension (dimension.textWidth,((Double)(dimension.height * SCROLL_HEIGHT_PROPORTION)).intValue ()));
	//set scrollPane border
	if (RemoteMethod.VERBOSE) {
	    scrollPane.setBorder (BorderFactory.createLineBorder (Color.GREEN));
	}

	add (scrollPane);

	return this;
    }

    public void setDefaultText(String defaultText) {
	textArea.setText (defaultText);
    }

    public void setEditEnable(boolean editEnable) {
	textArea.setEditable (editEnable);
    }

    public static class TextAreaDimension {
	public int height;
	public int textWidth;

	public TextAreaDimension(Dimension parentSize, double textWidthProportion, double heightProportion) {
	    this.textWidth = ( (Double) ( parentSize.width * textWidthProportion ) ).intValue ();
	    this.height = ( (Double) ( parentSize.height * heightProportion ) ).intValue ();
	}

	public TextAreaDimension(int textWidth, int height) {
	    this.textWidth = textWidth;
	    this.height = height;
	}

    }

}
