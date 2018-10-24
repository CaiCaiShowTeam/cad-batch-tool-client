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

    protected CadTablePanel cadTablePanel;

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
	    logger.debug ("merge plm data result is -> " + documents);
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
	setJMenuBar (
		toolkit.getStandardMenuBar (new CheckinActionListenner (),new CheckoutAndDownloadActionListenner ()));
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
	@Override
	public void actionPerformed(ActionEvent e) {

	    processorAttachments ();
	    logger.debug ("processorAttachments after documents is -> " + documents);

	    // checkin before validate basic data
	    ValidateUtils.validateCheckin (documents);

	    CheckinWorker worker = new CheckinWorker (documents);
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

    private class CheckinWorker extends SwingWorker<Boolean, PopProgress.PromptProgress> implements Callback {
	private Documents documents;
	private PopProgress progress;
	private final String PROMPT_0 = "checkin.prompt.0";
	private final String PROMPT_100 = "checkin.prompt.100";
	private final String PROMPT_50 = "checkin.prompt.50";
	private final String PROMPT_FAILED = "checkin.prompt.failed";
	private final String PROMPT_SUCCESSED = "checkin.prompt.successed";
	private final String PROMPT_TITLE = "checkin.prompt.title";

	public CheckinWorker(Documents documents) {
	    this.documents = documents;
	    this.progress = new PopProgress (this);
	    progress.activate ();
	}

	@Override
	public void call(Object object) {
	    logger.debug ("In CheckinWorker call ... " + object);
	}

	@Override
	protected Boolean doInBackground() throws Exception {
	    logger.info ("CheckinWorker start...");
	    // upload dwg file
	    List<Document> documentL = documents.getDocuments ();
	    if (documentL != null && !documentL.isEmpty ()) {
		for (int i = 0; i < documentL.size (); i++) {
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
	}

	@Override
	protected void process(List<PopProgress.PromptProgress> chunks) {
	    progress.setProgress (chunks.get (0));
	    for (PopProgress.PromptProgress progress : chunks) {
		detailTextAreaPanel.println (progress.getPrompt ());
	    }
	}
    }

    public class CheckoutActionListener implements ActionListener, Callback {

	private Callback callback;

	public CheckoutActionListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // processor checkout
	    ValidateUtils.validateCheckout (documents);

	    CheckoutWorker worker = new CheckoutWorker (documents);
	    worker.execute ();
	}

	@Override
	public void call(Object object) {
	    logger.debug ("in CheckoutActionListener call object is -> " + object + " callback is -> " + callback);
	}
    }

    public class CheckoutAndDownloadActionListenner implements ActionListener, Callback {

	@Override
	public void actionPerformed(ActionEvent e) {
	    // new SearchForDownloadDialog (this).activate ();
	}

	@Override
	public void call(Object object) {
	    ClientUtils.open ((File) object);
	}
    }

    private class CheckoutWorker extends SwingWorker<Boolean, PopProgress.PromptProgress> implements Callback {
	private Documents documents;
	private PopProgress progress;
	private final String PROMPT_FAILED = "checkout.prompt.failed";
	private final String PROMPT_SUCCESSED = "checkout.prompt.successed";
	private final String PROMPT_TITLE = "checkout.prompt.title";
	private List<SimpleDocument> resultL;

	public CheckoutWorker(Documents documents) {
	    this.documents = documents;
	    this.progress = new PopProgress (this);
	    progress.activate ();
	}

	@Override
	public void call(Object object) {
	    logger.debug ("in CheckoutWorker call ... " + object);
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

    public class UndoCheckoutActionListener implements ActionListener, Callback {

	private Callback callback;

	public UndoCheckoutActionListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // processor cancle checkout
	    ValidateUtils.validateUndoCheckout (documents);

	    UndoCheckoutWorker worker = new UndoCheckoutWorker (documents);
	    worker.execute ();
	}

	@Override
	public void call(Object object) {
	    logger.debug ("in UndoCheckoutActionListener object is -> " + object + " callback is -> " + callback);
	}
    }

    private class UndoCheckoutWorker extends SwingWorker<Boolean, PopProgress.PromptProgress> implements Callback {
	private Documents documents;
	private PopProgress progress;
	private final String PROMPT_FAILED = "undocheckout.prompt.failed";
	private final String PROMPT_SUCCESSED = "undocheckout.prompt.successed";
	private final String PROMPT_TITLE = "undocheckout.prompt.title";
	private List<SimpleDocument> resultL;

	public UndoCheckoutWorker(Documents documents) {
	    this.documents = documents;
	    this.progress = new PopProgress (this);
	    progress.activate ();
	}

	@Override
	public void call(Object object) {
	    logger.debug ("in UndoCheckoutWorker call ... " + object);
	}

	@Override
	protected Boolean doInBackground() throws Exception {
	    logger.info ("undoCheckoutWorker start...");

	    publish (new PopProgress.PromptProgress (getResourceMap ().getString ("start undocheckout ..."),0));
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
	    publish (new PopProgress.PromptProgress (getResourceMap ().getString ("completed undocheckout"),100));
	}

	@Override
	protected void process(List<PopProgress.PromptProgress> chunks) {
	    progress.setProgress (chunks.get (0));
	    for (PopProgress.PromptProgress progress : chunks) {
		detailTextAreaPanel.println (progress.getPrompt ());
	    }
	}
    }

    public Documents getDocuments() {
        return documents;
    }

    public void setDocuments(Documents documents) {
        this.documents = documents;
    }

}
