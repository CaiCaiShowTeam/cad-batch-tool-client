package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.constant.RemoteMethod;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;

import priv.lee.cad.model.ResourceMap;
import priv.lee.cad.model.ResourceMapper;
import priv.lee.cad.model.StyleToolkit;
import priv.lee.cad.model.TieContainer;
import priv.lee.cad.model.impl.DefaultStyleToolkit;
import priv.lee.cad.model.impl.GlobalResourceMap;
import priv.lee.cad.util.StringUtils;

public class TabAttributePanel extends JTabbedPane implements ResourceMapper, TieContainer {

    private static final long serialVersionUID = 3070127155164816200L;
    private static Logger logger = Logger.getLogger (TabAttributePanel.class.getName ());
    private Documents documents;
    protected String PREFIX;
    private ResourceMap resourceMap;
    private HashMap<String, BasicAttributePanel> tabbedMap = new HashMap<String, BasicAttributePanel> ();
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
	return 0.69d;
    }

    public void initComponents() {
	setTabLayoutPolicy (SCROLL_TAB_LAYOUT);

	doSelfAdaption ();

	initialize ();

	validate ();
    }

    @Override
    public void initialize() {
	if (RemoteMethod.VERBOSE) {
	    setBorder (BorderFactory.createLineBorder (Color.RED));
	}

	List<Document> documentL = documents.getDocuments ();
	if (documentL == null || documentL.isEmpty ()) {
	    return;
	}

	for (Document document : documentL) {

	    BasicAttributePanel attributePanel = new BasicAttributePanel (document);

	    String documentNumber = StringUtils.isEmpty (document.getOid ())
		    ? ( (CadDocument) document.getObject () ).getNumber ()
		    : document.getNumber ();
	    tabbedMap.put (documentNumber,attributePanel);
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

    public String refreshContainerData(LinkedHashMap<String, Integer> editMap, String text) {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshContainerData is -> editMap=" + editMap + " text=[" + text + "]");
	}
	StringBuffer buf = new StringBuffer ();
	for (Map.Entry<String, Integer> entry : editMap.entrySet ()) {
	    String title = entry.getKey ();
	    BasicAttributePanel bap = tabbedMap.get (title);
	    JTextField field = bap.pdmlinkProductPanel.text.getText ();
	    field.setText (text);
	    buf.append ("title为[" + title + "]的tab页中的产品容器信息已被更新为[" + text + "]");
	}
	return buf.toString ();
    }
    
    public String refreshFolderData(LinkedHashMap<String, Integer> editMap, String text) {
	if (logger.isDebugEnabled ()) {
	    logger.debug ("refreshFolderData is -> editMap=" + editMap + " text=[" + text + "]");
	}
   	StringBuffer buf = new StringBuffer ();
   	for (Map.Entry<String, Integer> entry : editMap.entrySet ()) {
   	    String title = entry.getKey ();
   	    BasicAttributePanel bap = tabbedMap.get (title);
   	    JTextField field = bap.subFolderPanel.text.getText ();
   	    field.setText (text);
   	    buf.append ("title为[" + title + "]的tab页中的文件夹信息已被更新为[" + text + "]");
   	}
   	return buf.toString ();
       }

}
