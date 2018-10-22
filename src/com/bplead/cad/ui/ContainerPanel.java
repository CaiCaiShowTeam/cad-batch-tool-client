package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;
import priv.lee.cad.util.ClientAssert;

public class ContainerPanel extends AbstractPanel {

    private static final long serialVersionUID = 1442969218942586007L;

    private final String BUTTON_ICON = "folder.search.icon";

    private final String EMPTY_FOLDER = "folder.empty.prompt";

    private final String EMPTY_PDMLINKPRODUCT = "pdm.empty.prompt";
    private final String FOLDER_ALL = "folder.all";
    private final String FOLDER_CHECK = "folder.check";
    private final String FOLDER_CLEAR = "folder.clear";
    private final String FOLDER_PROMPT = "folder.prompt";
    private final String FOLDER_TITLE = "folder.title";
    private final Logger logger = Logger.getLogger (ContainerPanel.class);
    private final String PDM_ALL = "pdm.all";
    private final String PDM_CHECK = "pdm.check";
    private final String PDM_CLEAR = "pdm.clear";
    private final String PDM_PROMPT = "pdm.prompt";
    private final String PDM_TITLE = "pdm.title";
    public PDMLinkProductPanel pdmlinkProductPanel;
    public SubFolderPanel subFolderPanel;
    private final String TITLE = "title";
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

	setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),getResourceMap ().getString (TITLE),
		TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,toolkit.getFont ()));
	
//	setBorder (BorderFactory.createLineBorder (Color.BLUE));
	

	logger.info ("initialize " + PDMLinkProductPanel.class + "...");
	pdmlinkProductPanel = new PDMLinkProductPanel (null);
	add (pdmlinkProductPanel);

	add (Box.createHorizontalStrut (1));

	logger.info ("initialize " + SubFolderPanel.class + "...");
	subFolderPanel = new SubFolderPanel (null);
	add (subFolderPanel);
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
	protected String setButtonTextAll() {
	    return PDM_ALL;
	}

	@Override
	protected String setButtonTextCheck() {
	    return PDM_CHECK;
	}

	@Override
	protected String setButtonTextClear() {
	    return PDM_CLEAR;
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
	    return getResourceMap ().getString (PDM_TITLE);
	}
    }

    abstract class SimpleButtonSetPanel<T> extends AbstractPanel implements ActionListener, Callback {

	private static final long serialVersionUID = -5690721799689305895L;
	private final double BUTTON_PROPORTION = 0.5d;
	private final double HEIGHT_PROPORTION = 0.47d;
	private final double LABEL_PROPORTION = 0.075d;
	private T object;
	public PromptTextField text;
	private final double TEXT_PROPORTION = 0.45d;
	private final double TXT_BTN_PROPOTION = 0.12d;

	public SimpleButtonSetPanel(T object) {
	    this.object = object;
	}

	private Dimension getButtonPrerredSize() {
	    BigDecimal width = new BigDecimal (getPreferredSize ().height)
		    .multiply (new BigDecimal (BUTTON_PROPORTION));
	    return new Dimension (width.intValue (),width.intValue ());
	}

	private Dimension getButtonPrerredSizeOther() {
	    Dimension dimension = getButtonPrerredSize ();

	    BigDecimal width = new BigDecimal (getPreferredSize ().width).multiply (new BigDecimal (TXT_BTN_PROPOTION));
	    return new Dimension (width.intValue (),dimension.height);
	}

	@Override
	public double getHorizontalProportion() {
	    return 0.99d;
	}

	@Override
	public double getVerticalProportion() {
	    return 0.38d;
	}

	@Override
	public void initialize() {
	    logger.info ("initialize " + getClass () + "  content...");
	    // ~ initialize content
//	    setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),setTitle (),
//		    TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,toolkit.getFont ()));

	    setBorder (BorderFactory.createLineBorder (Color.GREEN));

	    PromptTextField.PromptTextFieldDimension dimension = PromptTextField.newDimension (getPreferredSize (),
		    LABEL_PROPORTION,TEXT_PROPORTION,HEIGHT_PROPORTION);
	    text = PromptTextField.newInstance (setPrompt (),setText (object),dimension);
	    add (text);

	    // all
	    Option setAll = new Option (setButtonTextAll (),null,this,getButtonPrerredSizeOther ());
	    // check point
	    Option setCheck = new Option (setButtonTextCheck (),null,this,getButtonPrerredSizeOther ());
	    // clear
	    Option setClear = new Option (setButtonTextClear (),null,this,getButtonPrerredSizeOther ());

	    add (new OptionPanel (Arrays.asList (new Option (null,BUTTON_ICON,this,getButtonPrerredSize ()),setAll,
		    setCheck,setClear)));
	}

	protected void refresh(String text) {
	    this.text.getText ().setText (text);

	    validate ();
	}

	protected abstract String setButtonTextAll();

	protected abstract String setButtonTextCheck();

	protected abstract String setButtonTextClear();

	protected abstract String setPrompt();

	protected abstract String setText(T object);

	protected abstract String setTitle();
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
	protected String setButtonTextAll() {
	    return FOLDER_ALL;
	}

	@Override
	protected String setButtonTextCheck() {
	    return FOLDER_CHECK;
	}

	@Override
	protected String setButtonTextClear() {
	    return FOLDER_CLEAR;
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
	    return getResourceMap ().getString (FOLDER_TITLE);
	}
    }
}