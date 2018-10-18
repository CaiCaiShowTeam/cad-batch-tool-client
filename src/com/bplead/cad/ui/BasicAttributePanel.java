package com.bplead.cad.ui;

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
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.bplead.cad.annotation.IbaField;
import com.bplead.cad.bean.SimpleDocument;
import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.bean.io.CadDocument;
import com.bplead.cad.bean.io.Container;
import com.bplead.cad.bean.io.Document;

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
    private final double HEIGHT_PROPORTION = 0.1d;
    private final double HGAP_PROPORTION = 0.005d;
    private double labelProportion = 0.08d;
    private final Logger logger = Logger.getLogger (BasicAttributePanel.class);
    private Serializable serializable;// Document
    private CadDocument cadDocument;
    private double textProportion = 0.2d;
    private final String TITLE = "title";
    private final double VGAP_PROPORTION = 0.02d;
    private final String BUTTON_ICON = "folder.search.icon";
    private final String EMPTY_FOLDER = "folder.empty.prompt";
    private final String EMPTY_PDMLINKPRODUCT = "pdm.empty.prompt";
    private final String FOLDER_PROMPT = "folder.prompt";
    // private final String FOLDER_TITLE = "folder.title";
    private final String PDM_PROMPT = "pdm.prompt";
    // private final String PDM_TITLE = "pdm.title";
    private final String NUMBER_PROMPT = "number.prompt";
    private final String DETAIL_BUTTON_ICON = "object.details.icon";
    public PDMLinkProductPanel pdmlinkProductPanel;
    public SubFolderPanel subFolderPanel;
    public NumberPanel numberPanel;

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
	return 0.35d;
    }

    @Override
    public void initialize() {
	logger.info ("initialize content...");
	// ~ initialize content
	setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		getResourceMap ().getString (TITLE),TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,
		toolkit.getFont ()));
	// new productPanel , new subFolderPanel
	Container container = ( (Document) serializable ).getContainer ();
	pdmlinkProductPanel = new PDMLinkProductPanel (container == null ? null : container.getProduct ());
	subFolderPanel = new SubFolderPanel (container == null ? null : container.getFolder ());
	numberPanel = new NumberPanel ((Document) serializable);

	logger.info ("convert to PromptTextField...");
	List<PromptTextField> texts = conver2Texts ();

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
	private Document document;
	private CadDocument cadDocument;

	public NumberPanel(Document document) {
	    super (document);
	    this.document = document;
	    this.cadDocument = (CadDocument) document.getObject ();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // OPNE URL
	    try {
		Runtime.getRuntime ().exec ("cmd /c start http://plm.teg.cn/Windchill");
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
	protected String setButtonText() {
	    return null;
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

	@Override
	protected String setIcon() {
	    return DETAIL_BUTTON_ICON;
	}

    }

    class PDMLinkProductPanel extends SimpleButtonSetPanel<SimplePdmLinkProduct> {

	private static final long serialVersionUID = 5788762488066451045L;
	private SimplePdmLinkProduct product;

	public PDMLinkProductPanel(SimplePdmLinkProduct product) {
	    super (product);
	    this.product = product;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    new PdmLinkProductChooseDialog (this).activate ();
	}

	@Override
	public void call(Object object) {
	    ClientAssert.notNull (object,"Callback object is required");
	    ClientAssert.isInstanceOf (SimplePdmLinkProduct.class,object,
		    "Callback object must be a SimplePdmLinkProduct type");

	    product = (SimplePdmLinkProduct) object;

	    refresh (product.getName ());
	}

	public SimplePdmLinkProduct getProduct() {
	    return product;
	}

	@Override
	protected String setButtonText() {
	    return null;
	}

	@Override
	protected String setPrompt() {
	    return getResourceMap ().getString (PDM_PROMPT);
	}

	@Override
	protected String setText(SimplePdmLinkProduct product) {
	    if (product == null) {
		return getResourceMap ().getString (EMPTY_PDMLINKPRODUCT);
	    }
	    return product.getName ();
	}

	@Override
	protected String setTitle() {
	    return null;
	}

	@Override
	protected String setIcon() {
	    return BUTTON_ICON;
	}
    }

    abstract class SimpleButtonSetPanel<T> extends AbstractPanel implements ActionListener, Callback {

	private static final long serialVersionUID = -5690721799689305895L;
	// private final double BUTTON_PROPORTION = 0.3d;
	private final double BUTTON_PROPORTION = 0.1d;
	// private final double HEIGHT_PROPORTION = 0.3d;
	private final double HEIGHT_PROPORTION = 0.7d;
	// private final double LABEL_PROPORTION = 0.15d;
	private final double LABEL_PROPORTION = 0.08d;
	private T object;
	public PromptTextField text;
	// private final double TEXT_PROPORTION = 0.65d;
	private final double TEXT_PROPORTION = 0.2d;

	public SimpleButtonSetPanel(T object) {
	    this.object = object;
	}

	private Dimension getButtonPrerredSize() {
	    BigDecimal width = new BigDecimal (getPreferredSize ().height)
		    .multiply (new BigDecimal (BUTTON_PROPORTION));
	    return new Dimension (width.intValue (),width.intValue ());
	}

	@Override
	public double getHorizontalProportion() {
	    return 0.47d;
	}

	@Override
	public double getVerticalProportion() {
	    return 0.1d;
	}

	@Override
	public void initialize() {
	    logger.info ("modify " + getClass () + " to flow layout...");
	    setLayout (new FlowLayout (FlowLayout.LEFT));

	    logger.info ("initialize " + getClass () + "  content...");
	    // ~ initialize content
	    setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),setTitle (),
		    TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,toolkit.getFont ()));

	    PromptTextField.PromptTextFieldDimension dimension = PromptTextField.newDimension (getPreferredSize (),
		    LABEL_PROPORTION,TEXT_PROPORTION,HEIGHT_PROPORTION);
	    text = PromptTextField.newInstance (setPrompt (),setText (object),dimension);
	    add (text);

	    add (new OptionPanel (Arrays.asList (new Option (null,setIcon (),this,getButtonPrerredSize ()))));
	}

	protected void refresh(String text) {
	    this.text.getText ().setText (text);

	    validate ();
	}

	protected abstract String setButtonText();

	protected abstract String setPrompt();

	protected abstract String setText(T object);

	protected abstract String setTitle();

	protected abstract String setIcon();
    }

    class SubFolderPanel extends SimpleButtonSetPanel<SimpleFolder> {

	private static final long serialVersionUID = 5788762488066451045L;
	private SimpleFolder folder;

	public SubFolderPanel(SimpleFolder folder) {
	    super (folder);
	    this.folder = folder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    new FolderChooseDialog (this,pdmlinkProductPanel.product).activate ();
	}

	@Override
	public void call(Object object) {
	    ClientAssert.notNull (object,"Callback object is required");

	    Object [] nodes = (Object []) object;
	    folder = ( (FolderTree.FolderNode) nodes[nodes.length - 1] ).getFolder ();

	    refresh (folder.getName ());
	}

	public SimpleFolder getFolder() {
	    return folder;
	}

	@Override
	protected String setButtonText() {
	    return null;
	}

	@Override
	protected String setPrompt() {
	    return getResourceMap ().getString (FOLDER_PROMPT);
	}

	@Override
	protected String setText(SimpleFolder folder) {
	    if (folder == null) {
		return getResourceMap ().getString (EMPTY_FOLDER);
	    }
	    return folder.getName ();
	}

	@Override
	protected String setTitle() {
	    return null;
	}

	@Override
	protected String setIcon() {
	    return BUTTON_ICON;
	}
    }
}
