package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JOptionPane;
import javax.swing.border.TitledBorder;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimpleFolder;
import com.bplead.cad.bean.SimplePdmLinkProduct;
import com.bplead.cad.util.ClientUtils;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;

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
	return 0.3d;
    }

    @Override
    public void initialize() {

	setBorder (BorderFactory.createTitledBorder (BorderFactory.createEtchedBorder (),
		getResourceMap ().getString (TITLE),TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,
		toolkit.getFont ()));

	// setBorder (BorderFactory.createLineBorder (Color.BLUE));

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
	    Option option = (Option) e.getSource ();
	    String containerName = this.text.getText ().getText ();
	    boolean flag = this.getProduct () != null;
	    if (flag) {
		containerName = this.getProduct ().getName ();
	    }
	    if (logger.isDebugEnabled ()) {
		logger.debug ("containerName is -> " + containerName);
	    }
	    flag = true;
	    if (StringUtils.equals (option.getText (),"设置全部")) {
		if (flag) {
		    setAllForContainer (containerName);
		} else {
		    JOptionPane.showMessageDialog (null,"尚未选择产品容器","提示",JOptionPane.INFORMATION_MESSAGE);
		}
	    } else if (StringUtils.equals (option.getText (),"设置选中")) {
		if (flag) {
		    setCheckForContainer (containerName);
		} else {
		    JOptionPane.showMessageDialog (null,"尚未选择产品容器","提示",JOptionPane.INFORMATION_MESSAGE);
		}
	    } else if (StringUtils.equals (option.getText (),"清空全部")) {
		if (flag) {
		    setClearForContainer (containerName);
		} else {
		    JOptionPane.showMessageDialog (null,"尚未选择产品容器","提示",JOptionPane.INFORMATION_MESSAGE);
		}
	    } else {
		new PdmLinkProductChooseDialog (this).activate ();
	    }
	}
	
	public void setAllForContainer (String containerName) {
	    JOptionPane.showMessageDialog (null,"您点击的是'设置全部'按钮 " + containerName,"提示",JOptionPane.INFORMATION_MESSAGE);
	    //refresh CadTablePanel data
	    WestPanel westPanel = ClientUtils.getParentContainer (this,WestPanel.class);
	    String result = westPanel.cadTablePanel.refreshContainerData (containerName,"all");
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置全部 refreshContainerData result is -> " + result);
	    }
	    //refresh TabAttributePanel data
	    CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
	    LinkedHashMap<String, Integer> editMap = westPanel.cadTablePanel.getAllEnableColumnValue ();
	    String resultTabbed = cadMainFrame.tabAttributePanel.refreshContainerData (editMap, containerName);
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置全部 refreshContainerData resultTabbed is -> " + resultTabbed);
	    }
	}
	
	public void setCheckForContainer (String containerName) {
	    JOptionPane.showMessageDialog (null,"您点击的是'设置选中'按钮 " + containerName,"提示",JOptionPane.INFORMATION_MESSAGE);
	    //refresh CadTablePanel data
	    WestPanel westPanel = (WestPanel) this.getParent ().getParent ();
	    String result = westPanel.cadTablePanel.refreshContainerData (containerName,"check");
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置选中 refreshContainerData result is -> " + result);
	    }
	    //refresh TabAttributePanel data
	    CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
	    LinkedHashMap<String, Integer> editMap = westPanel.cadTablePanel.getCheckEnableColumnValue ();
	    String resultTabbed = cadMainFrame.tabAttributePanel.refreshContainerData (editMap, containerName);
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置选中refreshContainerData resultTabbed is -> " + resultTabbed);
	    }
	}
	
	public void setClearForContainer (String containerName) {
	    JOptionPane.showMessageDialog (null,"您点击的是'清空全部'按钮 " + containerName,"提示",JOptionPane.INFORMATION_MESSAGE);
	    //refresh CadTablePanel data
	    WestPanel westPanel = (WestPanel) this.getParent ().getParent ();
	    String result = westPanel.cadTablePanel.refreshContainerData (containerName,"clear");
	    if (logger.isInfoEnabled ()) {
		logger.info ("清空全部 refreshContainerData result is -> " + result);
	    }
	    //refresh TabAttributePanel data
	    CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
	    LinkedHashMap<String, Integer> editMap = westPanel.cadTablePanel.getAllEnableColumnValue ();
	    String resultTabbed = cadMainFrame.tabAttributePanel.refreshContainerData (editMap, null);
	    if (logger.isInfoEnabled ()) {
		logger.info ("清空全部 refreshContainerData resultTabbed is -> " + resultTabbed);
	    }
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
	private final double LABEL_PROPORTION = 0.1d;
	private T object;
	public PromptTextField text;
	private final double TEXT_PROPORTION = 0.3d;
	private final double TXT_BTN_PROPOTION = 0.15d;

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
	    // setBorder (BorderFactory.createTitledBorder
	    // (BorderFactory.createEtchedBorder (),setTitle (),
	    // TitledBorder.DEFAULT_JUSTIFICATION,TitledBorder.DEFAULT_POSITION,toolkit.getFont
	    // ()));

	    setBorder (BorderFactory.createLineBorder (Color.GREEN));

	    PromptTextField.PromptTextFieldDimension dimension = PromptTextField.newDimension (getPreferredSize (),
		    LABEL_PROPORTION,TEXT_PROPORTION,HEIGHT_PROPORTION);
	    text = PromptTextField.newInstance (setPrompt (),setText (object),dimension);
	    //TODO 设置文本框不可编辑
	    text.setEditable (false);
	    add (text);

	    // all
	    Option setAll = new Option (setButtonTextAll (),null,this,getButtonPrerredSizeOther ());
	    // check point
	    Option setCheck = new Option (setButtonTextCheck (),null,this,getButtonPrerredSizeOther ());
	    // clear
	    Option setClear = new Option (setButtonTextClear (),null,this,getButtonPrerredSizeOther ());

	    // chose
	    Option option = new Option (null,BUTTON_ICON,this,getButtonPrerredSize ());
	    option.setContentAreaFilled (false);
	    option.setBorder (null);

	    add (new OptionPanel (Arrays.asList (option,setAll,setCheck,setClear)));
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
	    Option option = (Option) e.getSource ();
	    String folderName = this.text.getText ().getText ();
	    boolean flag = this.getFolder () != null;
	    if (flag) {
		folderName = this.getFolder ().getName ();
	    }
	    if (logger.isDebugEnabled ()) {
		logger.debug ("folderName is -> " + folderName);
	    }
	    flag = true;
	    if (StringUtils.equals (option.getText (),"设置全部")) {
		if (flag) {
		    setAllForFolder (folderName);
		} else {
		    JOptionPane.showMessageDialog (null,"尚未选择文件夹,不能执行该操作","提示",JOptionPane.INFORMATION_MESSAGE);
		}
	    } else if (StringUtils.equals (option.getText (),"设置选中")) {
		if (flag) {
		    setCheckForFolder (folderName);
		} else {
		    JOptionPane.showMessageDialog (null,"尚未选择文件夹,不能执行该操作","提示",JOptionPane.INFORMATION_MESSAGE);
		}
	    } else if (StringUtils.equals (option.getText (),"清空全部")) {
		if (flag) {
		   setClearForFolder (folderName);
		} else {
		    JOptionPane.showMessageDialog (null,"尚未选择文件夹,不能执行该操作","提示",JOptionPane.INFORMATION_MESSAGE);
		}
	    } else {
		new FolderChooseDialog (this,pdmlinkProductPanel.product).activate ();
	    }
	}
	
	public void setAllForFolder (String folderName) {
	    JOptionPane.showMessageDialog (null,"您点击的是'设置全部'按钮 " + folderName,"提示",JOptionPane.INFORMATION_MESSAGE);
	    //refresh CadTablePanel data
	    WestPanel westPanel = (WestPanel) this.getParent ().getParent ();
	    String result = westPanel.cadTablePanel.refreshFolderData (folderName,"all");
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置全部 refreshFolderData result is -> " + result);
	    }
	    //refresh TabAttributePanel data
	    CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
	    LinkedHashMap<String, Integer> editMap = westPanel.cadTablePanel.getAllEnableColumnValue ();
	    String resultTabbed = cadMainFrame.tabAttributePanel.refreshFolderData (editMap, folderName);
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置全部 refreshFolderData resultTabbed is -> " + resultTabbed);
	    }
	}
	
	public void setCheckForFolder (String folderName) {
	    JOptionPane.showMessageDialog (null,"您点击的是'设置选中'按钮 " + folderName,"提示",JOptionPane.INFORMATION_MESSAGE);
	    //refresh CadTablePanel data
	    WestPanel westPanel = (WestPanel) this.getParent ().getParent ();
	    String result = westPanel.cadTablePanel.refreshFolderData (folderName,"check");
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置选中 refreshFolderData result is -> " + result);
	    }
	    //refresh TabAttributePanel data
	    CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
	    LinkedHashMap<String, Integer> editMap = westPanel.cadTablePanel.getCheckEnableColumnValue ();
	    String resultTabbed = cadMainFrame.tabAttributePanel.refreshFolderData (editMap, folderName);
	    if (logger.isInfoEnabled ()) {
		logger.info ("设置选中 refreshFolderData resultTabbed is -> " + resultTabbed);
	    }
	}
	
	public void setClearForFolder (String folderName) {
	    JOptionPane.showMessageDialog (null,"您点击的是'清空全部'按钮 " + folderName,"提示",JOptionPane.INFORMATION_MESSAGE);
	    //refresh CadTablePanel data
	    WestPanel westPanel = (WestPanel) this.getParent ().getParent ();
	    String result = westPanel.cadTablePanel.refreshFolderData (folderName,"clear");
	    if (logger.isInfoEnabled ()) {
		logger.info ("清空全部 refreshFolderData result is -> " + result);
	    }
	    //refresh TabAttributePanel data
	    CADMainFrame cadMainFrame = ClientUtils.getParentContainer (this,CADMainFrame.class);
	    LinkedHashMap<String, Integer> editMap = westPanel.cadTablePanel.getAllEnableColumnValue ();
	    String resultTabbed = cadMainFrame.tabAttributePanel.refreshFolderData (editMap, null);
	    if (logger.isInfoEnabled ()) {
		logger.info ("清空全部 refreshFolderData resultTabbed is -> " + resultTabbed);
	    }
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
