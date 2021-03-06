package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.bplead.cad.annotation.IbaField;
import com.bplead.cad.bean.SimpleDocument;
import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.bean.constant.RemoteMethod;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.CadStatus;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.layout.DefaultGroupLayout;
import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;

public class BasicAttributePanel extends AbstractPanel {

    private static final long serialVersionUID = 5723039852386303330L;
    private final String FOLDER_BUTTON_ICON = "folder.search.icon";
    private final String PDM_SEARCH_ICON = "pdm.search.icon";
    private final String DETAIL_BUTTON_ICON = "object.details.icon";
    private CadDocument cadDocument;
    private final String EMPTY_FOLDER = "folder.empty.prompt";
    private final String EMPTY_PDMLINKPRODUCT = "pdm.empty.prompt";
    private final String FOLDER_PROMPT = "folder.prompt";
    private final String FOLDER_SEARCH_TEXT = "folder.search.text";
    private final double HEIGHT_PROPORTION = 0.15d;
    private final double HGAP_PROPORTION = 0.02d;
    private double labelProportion = 0.13d;
    private final Logger logger = Logger.getLogger (BasicAttributePanel.class);
    private final String NUMBER_PROMPT = "number.prompt";
    public NumberPanel numberPanel;
    private final String OBJECT_DETAILS_TEXT = "object.details.text";
    private final String PDM_PROMPT = "pdm.prompt";
    private final String PDM_SEARCH_TEXT = "pdm.search.text";
    public PDMLinkProductPanel pdmlinkProductPanel;
    private Serializable serializable;// Document
    public SubFolderPanel subFolderPanel;
    private double textProportion = 0.28d;
    private final String TITLE = "title";
    private final double VGAP_PROPORTION = 0.02d;

    public BasicAttributePanel(Serializable serializable) {
	this.serializable = serializable;
	if (serializable instanceof Document) {
	    this.cadDocument = (CadDocument) ( (Document) serializable ).getObject ();
	}
    }

    private List<PromptTextField> conver2Texts() {
	// ~ reflect String type fields and convert to PromptTextField type
	List<PromptTextField> texts = new ArrayList<PromptTextField> ();

	PromptTextField.PromptTextFieldDimension dimension = PromptTextField.newDimension (getPreferredSize (),
		labelProportion,textProportion,HEIGHT_PROPORTION);

	String value = "";
	Field [] fields = cadDocument.getClass ().getDeclaredFields ();
	for (Field field : fields) {
	    field.setAccessible (true);
	    value = "";
	    try {
		IbaField ibaField = field.getAnnotation (IbaField.class);
		if (ibaField != null) {
		    if (!ibaField.panelAttr ()) {
			continue;
		    }
		} else {
		    continue;
		}

		if (this.serializable != null) {
		    Object object = field.get (cadDocument);
		    if (!( object instanceof String )) {
			continue;
		    }
		    value = (String) object;
		}
	    }
	    catch(Exception e) {
		e.printStackTrace ();
	    }
	    PromptTextField text = PromptTextField.newInstance (getResourceMap ().getString (field.getName ()),value,
		    dimension);
	    text.setEditable (false);
	    texts.add (text);
	}
	return texts;
    }

    public Serializable getCad() {
	return serializable;
    }

    @Override
    public double getHorizontalProportion() {
	return 0.95d;
    }

    public double getLabelProportion() {
	return labelProportion;
    }

    public double getTextProportion() {
	return textProportion;
    }

    @Override
    public double getVerticalProportion() {
	return 0.4d;
    }

    @Override
    public void initialize() {
	logger.info ("initialize content...");
	// ~ initialize content
	setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		getResourceMap ().getString (TITLE),TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,
		toolkit.getFont ()));

	// new productPanel , new subFolderPanel
	pdmlinkProductPanel = new PDMLinkProductPanel ((Document) serializable);
	subFolderPanel = new SubFolderPanel ((Document) serializable);
	numberPanel = new NumberPanel ((Document) serializable);

	logger.info ("convert to PromptTextField...");
	List<PromptTextField> texts = conver2Texts ();
	
	if (RemoteMethod.VERBOSE) {
	    setBorder (BorderFactory.createLineBorder (Color.ORANGE));
	}

	// ~ performance hGap and vGap
	int hGap = ( (Double) ( getPreferredSize ().width * HGAP_PROPORTION ) ).intValue ();
	int vGap = ( (Double) ( getPreferredSize ().height * VGAP_PROPORTION ) ).intValue ();
	logger.debug ("hGap:" + hGap + ",vGap:" + vGap);

	logger.info ("use default group layout...");
	DefaultGroupLayout layout = new DefaultGroupLayout (this,hGap,vGap);
	layout.addComponent (pdmlinkProductPanel);
	layout.addComponent (subFolderPanel);
	layout.addComponent (numberPanel);
	layout.addComponent (texts).layout (2);
    }

    public void setCad(Serializable cad) {
	this.serializable = cad;
    }

    public void setLabelProportion(double labelProportion) {
	this.labelProportion = labelProportion;
    }

    public void setTextProportion(double textProportion) {
	this.textProportion = textProportion;
    }

    class NumberPanel extends SimpleButtonSetPanel<Document> {

	private static final long serialVersionUID = 5788762488066451045L;
	private CadDocument cadDocument;
	private Document document;

	public NumberPanel(Document document) {
	    super (document);
	    this.document = document;
	    this.cadDocument = (CadDocument) document.getObject ();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // OPNE URL
	    try {
		Runtime.getRuntime ().exec ("cmd /c start http://www.baidu.com");
	    }
	    catch(IOException ee) {
		ee.printStackTrace ();
	    }
	}

	@Override
	public void call(Object object) {
	    ClientAssert.notNull (object,"Callback object is required");
	    ClientAssert.isInstanceOf (Document.class,object,"Callback object must be a SimplePdmLinkProduct type");

	    document = (Document) object;
	    cadDocument = (CadDocument) document.getObject ();

	    String docNumber = StringUtils.isEmpty (document.getOid ()) ? cadDocument.getNumber ()
		    : document.getNumber ();
	    refresh (docNumber);
	}

	public SimpleDocument getDocument() {
	    return document;
	}

	@Override
	protected String setBottonText() {
	    return OBJECT_DETAILS_TEXT;
	}

	@Override
	protected String setButtonText() {
	    return null;
	}

	@Override
	protected String setIcon() {
	    return DETAIL_BUTTON_ICON;
	}

	@Override
	protected String setPrompt() {
	    return getResourceMap ().getString (NUMBER_PROMPT);
	}

	@Override
	protected String setText(Document document) {
	    return StringUtils.isEmpty (document.getNumber ()) ? cadDocument.getNumber () : document.getNumber ();
	}

	@Override
	protected String setTitle() {
	    return null;
	}

    }

    class PDMLinkProductPanel extends SimpleButtonSetPanel<Document> {

	private static final long serialVersionUID = 5788762488066451045L;
	private SimplePdmLinkProduct product;
	private Document document;

	public PDMLinkProductPanel(Document document) {
	    super (document);
	    this.document = document;
	    this.product = document.getContainer () == null ? null : document.getContainer ().getProduct ();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    CadStatus cadStatus = document.getCadStatus ();
	    if (cadStatus == CadStatus.NOT_EXIST) {
		new SearchPDMLinkProductDialog (this).activate ();
	    } else {
		JOptionPane.showMessageDialog (null,"对象已在系统中存在,不能执行更改产品容器操作.","提示",JOptionPane.INFORMATION_MESSAGE);
	    }
//	    call (new SimplePdmLinkProduct ("wt.pdmlink.PDMLinkProduct:123456","测试产品容器"));
	}

	@Override
	public void call(Object object) {
	    ClientAssert.notNull (object,"Callback object is required");
	    ClientAssert.isInstanceOf (SimplePdmLinkProduct.class,object,
		    "Callback object must be a SimplePdmLinkProduct type");

	    product = (SimplePdmLinkProduct) object;

	    refresh (product.getName ());

	    // refresh cadTablePanel
	    boolean enable = ClientUtils.enableObject (document);
	    // enable = true;
	    if (enable) {
		String number = ( (CadDocument) document.getObject () ).getNumber ();
		String containerName = product.getName ();
		if (logger.isDebugEnabled ()) {
		    logger.debug (
			    "refresh cadTablePanel number is -> " + number + " containerName is -> " + containerName);
		}
		CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
		String result = cadMainFrame.westPanel.cadTablePanel.refreshContainerByNumber (number,containerName);
		if (logger.isDebugEnabled ()) {
		    logger.debug ("选择产品容器后回调函数中处理更新table中产品容器信息的结果 result is -> " + result);
		}
	    }
	}

	public SimplePdmLinkProduct getProduct() {
	    return product;
	}

	@Override
	protected String setBottonText() {
	    return PDM_SEARCH_TEXT;
	}

	@Override
	protected String setButtonText() {
	    return null;
	}

	@Override
	protected String setIcon() {
	    return PDM_SEARCH_ICON;
	}

	@Override
	protected String setPrompt() {
	    return getResourceMap ().getString (PDM_PROMPT);
	}

	@Override
	protected String setText(Document document) {
	    if (product == null) {
		return getResourceMap ().getString (EMPTY_PDMLINKPRODUCT);
	    }
	    return product.getName ();
	}

	@Override
	protected String setTitle() {
	    return null;
	}
    }

    abstract class SimpleButtonSetPanel<T> extends AbstractPanel implements ActionListener, Callback {

	private static final long serialVersionUID = -5690721799689305895L;
	private final double BUTTON_PROPORTION = 1d;
	private final double HEIGHT_PROPORTION = 0.9d;
	private final double LABEL_PROPORTION = 0.23d;
	private T object;
	public PromptTextField text;
	private final double TEXT_PROPORTION = 0.6d;

	public SimpleButtonSetPanel(T object) {
	    this.object = object;
	}

	private Dimension getButtonPrerredSize() {
	    BigDecimal width = new BigDecimal (getPreferredSize ().height)
		    .multiply (new BigDecimal (BUTTON_PROPORTION));
	    return new Dimension (width.intValue (),( (Double) ( getPreferredSize ().height * 0.8d ) ).intValue ());
	}

	@Override
	public double getHorizontalProportion() {
	    return 0.47d;
	}

	@Override
	public double getVerticalProportion() {
	    return 0.15d;
	}

	@Override
	public void initialize() {
	    logger.info ("modify " + getClass () + " to flow layout...");
	    setLayout (new FlowLayout (FlowLayout.LEFT,5,5));

	    logger.info ("initialize " + getClass () + "  content...");
	    // ~ initialize content
	    // setBorder (BorderFactory.createTitledBorder
	    // (BorderFactory.createEtchedBorder (),setTitle (),
	    // TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,toolkit.getFont
	    // ()));

	    PromptTextField.PromptTextFieldDimension dimension = PromptTextField.newDimension (getPreferredSize (),
		    LABEL_PROPORTION,TEXT_PROPORTION,HEIGHT_PROPORTION);
	    text = PromptTextField.newInstance (setPrompt (),setText (object),dimension);
	    text.setEditable (false);

	    add (text);

	    Option option = new Option (null,setIcon (),this,getButtonPrerredSize ());
	    option.setContentAreaFilled (false);
	    option.setBorder (null);
	    add (new OptionPanel (Arrays.asList (option)));
	}

	protected void refresh(String text) {
	    this.text.getText ().setText (text);

	    validate ();
	}

	protected abstract String setBottonText();

	protected abstract String setButtonText();

	protected abstract String setIcon();

	protected abstract String setPrompt();

	protected abstract String setText(T object);

	protected abstract String setTitle();
    }

    class SubFolderPanel extends SimpleButtonSetPanel<Document> {

	private static final long serialVersionUID = 5788762488066451045L;
	private SimpleFolder folder;
	private Document document;

	public SubFolderPanel(Document document) {
	    super (document);
	    this.document = document;
	    this.folder = document.getContainer () == null ? null : document.getContainer ().getFolder ();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    CadStatus cadStatus = document.getCadStatus ();
	    if (cadStatus == CadStatus.NOT_EXIST) {
		new FolderChooseDialog (this,pdmlinkProductPanel.product).activate ();
	    } else {
		JOptionPane.showMessageDialog (null,"对象已在系统中存在,不能执行更改文件夹操作.","提示",JOptionPane.INFORMATION_MESSAGE);
	    }
	}

	@Override
	public void call(Object object) {
	    ClientAssert.notNull (object,"Callback object is required");

	    Object [] nodes = (Object []) object;
	    String forderPath = "/";
	    for (Object obj : nodes) {
		FolderTree.FolderNode node = null;
		if (obj instanceof FolderTree.FolderNode) {
		    node = (FolderTree.FolderNode) obj;
		    forderPath = forderPath + node.getFolder ().getName () + "/";
		}
	    }
	    folder = ( (FolderTree.FolderNode) nodes[nodes.length - 1] ).getFolder ();
	    folder.setFolderPath (forderPath);
	    refresh (forderPath);

	    // refresh cadTablePanel
	    boolean enable = ClientUtils.enableObject (document);
	    if (enable) {
		String number = ( (CadDocument) document.getObject () ).getNumber ();
		String folderName = folder.getFolderPath ();
		if (logger.isDebugEnabled ()) {
		    logger.debug ("refresh cadTablePanel number is -> " + number + " folderName is -> " + folderName);
		}
		CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
		String result = cadMainFrame.westPanel.cadTablePanel.refreshFolderByNumber (number,folderName);
		if (logger.isDebugEnabled ()) {
		    logger.debug ("选择文件夹后回调函数中处理更新table中文件夹信息的结果 result is -> " + result);
		}
	    }
	}

	public SimpleFolder getFolder() {
	    return folder;
	}

	@Override
	protected String setBottonText() {
	    return FOLDER_SEARCH_TEXT;
	}

	@Override
	protected String setButtonText() {
	    return null;
	}

	@Override
	protected String setIcon() {
	    return FOLDER_BUTTON_ICON;
	}

	@Override
	protected String setPrompt() {
	    return getResourceMap ().getString (FOLDER_PROMPT);
	}

	@Override
	protected String setText(Document document) {
	    if (folder == null) {
		return getResourceMap ().getString (EMPTY_FOLDER);
	    }
	    return folder.getName ();
	}

	@Override
	protected String setTitle() {
	    return null;
	}
    }
}
