package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;

import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;

import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.StyleToolkit;
import priv.lee.cad.model.TieContainer;
import priv.lee.cad.model.impl.DefaultStyleToolkit;
import priv.lee.cad.model.impl.GlobalResourceMap;

public class TabAttributePanel extends JTabbedPane implements ResourceMapper, TieContainer {

    private static final long serialVersionUID = 3070127155164816200L;
    private Documents documents;
    protected String PREFIX;
    private ResourceMap resourceMap;
    protected StyleToolkit toolkit = new DefaultStyleToolkit ();
    {
	PREFIX = getClass ().getSimpleName ();
    }

    public TabAttributePanel(Documents documents) {
	this.documents = documents;
    }

    @Override
    public void activate() {
	this.resourceMap = initWindowResourceMap (getParent ());
	initComponents ();
    }

    private void doSelfAdaption() {
	Dimension dimension = getParent ().getPreferredSize ();
	Double width = dimension.width * getHorizontalProportion ();
	Double height = dimension.height * getVerticalProportion ();
	setPreferredSize (new Dimension (width.intValue (),height.intValue ()));
    }

    private double getHorizontalProportion() {
	return 0.495d;
    }

    @Override
    public ResourceMap getResourceMap() {
	return resourceMap;
    }

    private double getVerticalProportion() {
	return 0.85d;
    }

    public void initComponents() {
	setTabLayoutPolicy (SCROLL_TAB_LAYOUT);

	doSelfAdaption ();

	initialize ();

	validate ();
    }

    @Override
    public void initialize() {
	setBorder (BorderFactory.createLineBorder (Color.RED));

	List<Document> documentL = documents.getDocuments ();
	if (documentL == null || documentL.isEmpty ()) {
	    return;
	}

	for (Document document : documentL) {
	    BasicAttributePanel attributePanel = new BasicAttributePanel (document);

	    // String documentNumber = StringUtils.isEmpty
	    // (document.getEditEnable ()) ? ((CadDocument)document.getObject
	    // ()).getNumber (): document.getNumber ();
	    String documentNumber = ( (CadDocument) document.getObject () ).getNumber ();
	    addTab (documentNumber,attributePanel);
	}
    }

    protected ResourceMap initWindowResourceMap(Container container) {
	if (container instanceof Window) {
	    Window window = (Window) container;
	    return new GlobalResourceMap (PREFIX,window.getClass ());
	}
	return initWindowResourceMap (container.getParent ());
    }

    @Override
    public void setResourceMap(ResourceMap resourceMap) {
	this.resourceMap = resourceMap;
    }
}
