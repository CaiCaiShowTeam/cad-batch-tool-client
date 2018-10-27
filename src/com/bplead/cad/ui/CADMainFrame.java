package com.bplead.cad.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.SimpleDocument;
import com.bplead.cad.bean.constant.RemoteMethod;
import com.bplead.cad.bean.io.Attachment;
import com.bplead.cad.bean.io.CadDocuments;
import com.bplead.cad.bean.io.CadStatus;
import com.bplead.cad.bean.io.Document;
import com.bplead.cad.bean.io.Documents;
import com.bplead.cad.model.CustomStyleToolkit;
import com.bplead.cad.util.ClientUtils;
import com.bplead.cad.util.FTPUtils;
import com.bplead.cad.util.ValidateUtils;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractFrame;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.CollectionUtils;
import priv.lee.cad.util.PropertiesUtils;
import priv.lee.cad.util.XmlUtils;

public class CADMainFrame extends AbstractFrame implements Callback {

    private static final long serialVersionUID = -1719424691262349744L;

    private final String CAD_REPOSITORY = "cad.xml.repository";

    protected DetailTextAreaPanel detailTextAreaPanel;

    private Documents documents;

    private final Logger logger = Logger.getLogger (CADMainFrame.class);

    protected TabAttributePanel tabAttributePanel;

    protected CustomStyleToolkit toolkit = new CustomStyleToolkit ();

    protected WestPanel westPanel;

    public CADMainFrame() {
	super (CADMainFrame.class);
	setToolkit (toolkit);
    }

    @Override
    public void call(Object object) {
	reload ();
    }

    @Override
    public double getHorizontalProportion() {
	return 0.6d;
    }

    private File getRepository() {
	if (ClientUtils.temprary == null || !ClientUtils.getTemporaryFile ().exists ()) {
	    return null;
	}
	return new File (ClientUtils.getTemporaryDirectory () + PropertiesUtils.readProperty (CAD_REPOSITORY));
    }

    @Override
    public double getVerticalProportion() {
	return 0.99d;
    }

    /**
     * TODO
     * 
     * @author zjw checkin checkout undocheckout
     * @return 2018年10月24日下午9:55:28
     */
    public void mergeCommitParam() {
	CadTablePanel cadTablePanel = westPanel.cadTablePanel;
	documents = cadTablePanel.mergeCommitParam ();
    }

    private void initCAD() {
	if (getRepository () == null) {
	    // toolkit.startPreferencesDialog (this);
	    dispose ();
	} else {
	    File xml = getRepository ();
	    logger.debug ("xml file is -> " + xml);
	    ClientAssert.notNull (xml,"CAD tool initialize failed.Please check file/cad.xml"
		    + PropertiesUtils.readProperty (CAD_REPOSITORY) + " is exsits");
	    CadDocuments cadDocuments = XmlUtils.read (xml,CadDocuments.class);
	    logger.debug ("xml data object cadDocuments:" + cadDocuments);
	    this.documents = ClientUtils.initialize (cadDocuments);
	    logger.debug ("initialize plm data result is -> " + documents);
	}
    }

    @Override
    public void initialize() {
	logger.info ("initialize " + getClass () + " document...");
	initCAD ();
	if (documents == null) {
	    return;
	}
	logger.info ("initialize " + getClass () + " listenner...");
	// init manu action listenner
	HashMap<String, ActionListener> listenerMap = new HashMap<String, ActionListener> ();
	listenerMap.put (RemoteMethod.CLEAR_DETAIL_LISTENNER,new ClearDetailActionListenner ());
	listenerMap.put (RemoteMethod.EXPORT_DETAIL_LISTENNER,new ExportDetailActionListenner ());
	listenerMap.put (RemoteMethod.UNDO_CHECKOUT_LISTENNER,new UndoCheckoutActionListener (this));
	listenerMap.put (RemoteMethod.CHECKOUT_LISTENNER,new CheckoutActionListener (this));

	toolkit.setListenerMap (listenerMap);

	logger.info ("initialize " + getClass () + " menu bar...");
	setJMenuBar (toolkit.getStandardMenuBar (new CheckinActionListenner (this),
		new CheckoutAndDownloadActionListenner ()));
	logger.info ("initialize " + getClass () + " container panel...");

	// init layout borderLayout
	getContentPane ().setLayout (new BorderLayout (5,10));

	westPanel = new WestPanel (documents);
	getContentPane ().add (westPanel,BorderLayout.WEST);

	logger.info ("initialize " + getClass () + " TabAttributePanel panel ...");
	tabAttributePanel = new TabAttributePanel (documents);
	getContentPane ().add (tabAttributePanel,BorderLayout.EAST);

	logger.info ("initialize " + getClass () + " Detail jtextarea panel ...");
	detailTextAreaPanel = new DetailTextAreaPanel ();
	getContentPane ().add (detailTextAreaPanel,BorderLayout.SOUTH);
    }

    public class CheckinActionListenner implements ActionListener {

	private Callback callback;

	CheckinActionListenner(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    mergeCommitParam ();

	    processorAttachments ();
	    logger.debug ("processorAttachments after documents is -> " + documents);

	    // checkin before validate basic data
	    ValidateUtils.validateCheckin (documents);

	    CheckinWorker worker = new CheckinWorker (documents,callback);
	    worker.execute ();
	}

	private void processorAttachments() {
	    // build attachments add fileName,contentRole to Attachment
	    List<Document> documentL = documents.getDocuments ();
	    if (documentL != null && !documentL.isEmpty ()) {
		for (Document document : documentL) {
		    ClientUtils.buildAttachments (document.getObject (),ClientUtils.cadPrimarySuffix);
		}
	    }
	}
    }

    private class CheckinWorker extends SwingWorker<Boolean, PopProgress.PromptProgress> {
	private Documents documents;
	private Callback callback;
	private PopProgress progress;
	private final String PROMPT_0 = "checkin.prompt.0";
	private final String PROMPT_100 = "checkin.prompt.100";
	private final String PROMPT_50 = "checkin.prompt.50";
	private final String PROMPT_FAILED = "checkin.prompt.failed";
	private final String PROMPT_SUCCESSED = "checkin.prompt.successed";
	private final String PROMPT_TITLE = "checkin.prompt.title";

	public CheckinWorker(Documents documents, Callback callback) {
	    this.documents = documents;
	    this.callback = callback;
	    this.progress = new PopProgress (callback);
	    progress.activate ();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
	    logger.info ("CheckinWorker start...");
	    // upload dwg file
	    List<Document> documentL = documents.getDocuments ();
	    List<Integer> checkRows = documents.getCheckRows ();
	    if (documentL != null && !documentL.isEmpty ()) {
		for (int i = 0; i < documentL.size (); i++) {
		    if (!checkRows.contains (i)) {
			continue;
		    }
		    Document document = documentL.get (i);
		    List<Attachment> attachments = document.getObject ().getAttachments ();
		    publish (new PopProgress.PromptProgress (getResourceMap ().getString (PROMPT_0) + ( i + 1 ),0));
		    for (Attachment attachment : attachments) {
			File file = new File (attachment.getAbsolutePath ());
			FTPUtils.newInstance ().upload (file);
		    }
		}
	    }

	    publish (new PopProgress.PromptProgress (getResourceMap ().getString (PROMPT_50),50));
	    boolean successed = ClientUtils.checkin (documents);
	    if (successed) {
		JOptionPane.showMessageDialog (null,getResourceMap ().getString (PROMPT_SUCCESSED),
			getResourceMap ().getString (PROMPT_TITLE),JOptionPane.INFORMATION_MESSAGE);
	    } else {
		JOptionPane.showMessageDialog (null,getResourceMap ().getString (PROMPT_FAILED),
			getResourceMap ().getString (PROMPT_TITLE),JOptionPane.OK_OPTION);
	    }

	    logger.info ("CheckinWorker complete...");

	    return successed;
	}

	@Override
	protected void done() {
	    publish (new PopProgress.PromptProgress (getResourceMap ().getString (PROMPT_100),100));
	    if (callback instanceof CADMainFrame) {
		CADMainFrame cadMainFrame = (CADMainFrame) callback;
		cadMainFrame.westPanel.cadTablePanel.refreshCheckRowStatus (CadStatus.CHECK_IN);
	    }
	}

	@Override
	protected void process(List<PopProgress.PromptProgress> chunks) {
	    progress.setProgress (chunks.get (0));
	    for (PopProgress.PromptProgress progress : chunks) {
		detailTextAreaPanel.println (progress.getPrompt ());
	    }
	}
    }

    public class CheckoutActionListener implements ActionListener {

	private Callback callback;

	public CheckoutActionListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    mergeCommitParam ();

	    if (logger.isDebugEnabled ()) {
		logger.debug ("documents getCheckRows is -> " + documents.getCheckRows ());
	    }

	    // processor checkout
	    ValidateUtils.validateCheckout (documents);

	    CheckoutWorker worker = new CheckoutWorker (documents,callback);
	    worker.execute ();

	}

    }

    public class CheckoutAndDownloadActionListenner implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    // new SearchForDownloadDialog (this).activate ();
	}

    }

    private class CheckoutWorker extends SwingWorker<Boolean, PopProgress.PromptProgress> {
	private Documents documents;
	private Callback callback;
	private PopProgress progress;
	private final String PROMPT_FAILED = "checkout.prompt.failed";
	private final String PROMPT_SUCCESSED = "checkout.prompt.successed";
	private final String PROMPT_TITLE = "checkout.prompt.title";
	private List<SimpleDocument> resultL;

	public CheckoutWorker(Documents documents, Callback callback) {
	    this.documents = documents;
	    this.callback = callback;
	    this.progress = new PopProgress (callback);
	    progress.activate ();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
	    logger.info ("CheckoutWorker start...");

	    publish (new PopProgress.PromptProgress (getResourceMap ().getString ("start checkout ..."),0));
	    resultL = (List<SimpleDocument>) ClientUtils.checkout (documents);
	    if (!CollectionUtils.isEmpty (resultL)) {
		JOptionPane.showMessageDialog (null,getResourceMap ().getString (PROMPT_SUCCESSED),
			getResourceMap ().getString (PROMPT_TITLE),JOptionPane.INFORMATION_MESSAGE);
	    } else {
		JOptionPane.showMessageDialog (null,getResourceMap ().getString (PROMPT_FAILED),
			getResourceMap ().getString (PROMPT_TITLE),JOptionPane.OK_OPTION);
		return false;
	    }

	    logger.info ("CheckoutWorker completed...");
	    return true;
	}

	@Override
	protected void done() {
	    // TODO 1.设置操作可见性 2.刷新面板
	    publish (new PopProgress.PromptProgress (getResourceMap ().getString ("completed checkout ..."),100));
	    if (logger.isDebugEnabled ()) {
		logger.debug ("callback is -> " + ( callback == null ? "callback is null..." : callback.getClass () ));
	    }
	    if (callback instanceof CADMainFrame) {
		CADMainFrame cadMainFrame = (CADMainFrame) callback;
		cadMainFrame.westPanel.cadTablePanel.refreshCheckRowStatus (CadStatus.CHECK_OUT);
	    }
	}

	@Override
	protected void process(List<PopProgress.PromptProgress> chunks) {
	    progress.setProgress (chunks.get (0));
	    for (PopProgress.PromptProgress progress : chunks) {
		detailTextAreaPanel.println (progress.getPrompt ());
	    }
	}
    }

    public class ClearDetailActionListenner implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    detailTextAreaPanel.clear ();
	}
    }

    public class ExportDetailActionListenner implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String> () {
		@Override
		protected Boolean doInBackground() throws Exception {
		    int i = 0;
		    while (i < 50) {
			publish ("测试输入详细信息..." + i);
			i++;
		    }
		    return true;
		}

		@Override
		protected void process(List<String> chunks) {
		    for (String s : chunks) {
			detailTextAreaPanel.println (s);
		    }
		}
	    };
	    worker.execute ();
	}
    }

    public class UndoCheckoutActionListener implements ActionListener {

	private Callback callback;

	public UndoCheckoutActionListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    mergeCommitParam ();

	    // processor cancle checkout
	    ValidateUtils.validateUndoCheckout (documents);

	    UndoCheckoutWorker worker = new UndoCheckoutWorker (documents,callback);
	    worker.execute ();
	}

    }

    private class UndoCheckoutWorker extends SwingWorker<Boolean, PopProgress.PromptProgress> {
	private Documents documents;
	private Callback callback;
	private PopProgress progress;
	private final String PROMPT_FAILED = "undocheckout.prompt.failed";
	private final String PROMPT_SUCCESSED = "undocheckout.prompt.successed";
	private final String PROMPT_TITLE = "undocheckout.prompt.title";
	private List<SimpleDocument> resultL;

	public UndoCheckoutWorker(Documents documents, Callback callback) {
	    this.documents = documents;
	    this.callback = callback;
	    this.progress = new PopProgress (callback);
	    progress.activate ();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
	    logger.info ("undoCheckoutWorker start...");

	    publish (new PopProgress.PromptProgress (getResourceMap ().getString ("start undocheckout ..."),0));
	    resultL = (List<SimpleDocument>) ClientUtils.undoCheckout (documents);
	    if (!CollectionUtils.isEmpty (resultL)) {
		JOptionPane.showMessageDialog (null,getResourceMap ().getString (PROMPT_SUCCESSED),
			getResourceMap ().getString (PROMPT_TITLE),JOptionPane.INFORMATION_MESSAGE);
	    } else {
		JOptionPane.showMessageDialog (null,getResourceMap ().getString (PROMPT_FAILED),
			getResourceMap ().getString (PROMPT_TITLE),JOptionPane.OK_OPTION);
		return false;
	    }

	    logger.info ("CheckoutWorker completed...");
	    return true;
	}

	@Override
	protected void done() {
	    // TODO 1.设置操作可见性 2.刷新面板
	    publish (new PopProgress.PromptProgress (getResourceMap ().getString ("completed undocheckout"),100));
	    if (logger.isDebugEnabled ()) {
		logger.debug ("callback is -> " + ( callback == null ? "callback is null..." : callback.getClass () ));
	    }
	    if (callback instanceof CADMainFrame) {
		CADMainFrame cadMainFrame = (CADMainFrame) callback;
		cadMainFrame.westPanel.cadTablePanel.refreshCheckRowStatus (CadStatus.CHECK_IN);
	    }
	}

	@Override
	protected void process(List<PopProgress.PromptProgress> chunks) {
	    progress.setProgress (chunks.get (0));
	    for (PopProgress.PromptProgress progress : chunks) {
		detailTextAreaPanel.println (progress.getPrompt ());
	    }
	}
    }

}
