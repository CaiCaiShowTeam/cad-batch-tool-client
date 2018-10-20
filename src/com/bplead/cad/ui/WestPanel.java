/**
 * WestPanel.java 2018年10月20日
 */
package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.io.Documents;

import priv.lee.cad.model.MiddleAlignGap;
import priv.lee.cad.ui.AbstractPanel;

/**
 * @author zjw 2018年10月20日上午9:59:05
 */
public class WestPanel extends AbstractPanel {

    private static final Logger logger = Logger.getLogger (WestPanel.class);
    private static final long serialVersionUID = 5556854897406754302L;
    protected CadTablePanel cadTablePanel;
    protected ContainerPanel containerPanel;
    private Documents documents;
    private MiddleAlignGap gap = new MiddleAlignGap (10,10);

    public WestPanel(Documents documents) {
	this.documents = documents;
    }

    @Override
    public double getHorizontalProportion() {
	return 0.495d;
    }

    @Override
    public double getVerticalProportion() {
	return 0.69d;
    }

    @Override
    public void initialize() {
	setLayout (new FlowLayout (FlowLayout.CENTER,gap.hGap,gap.vGap));

	setBorder (BorderFactory.createLineBorder (Color.BLACK));

	logger.info ("initialize " + getClass () + " container attribute panel...");
	containerPanel = new ContainerPanel ();
	add (containerPanel);

	logger.info ("initialize " + getClass () + " table attribute panel...");
	cadTablePanel = new CadTablePanel (documents);
	add (cadTablePanel);
    }
}
