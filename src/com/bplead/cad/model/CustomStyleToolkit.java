package com.bplead.cad.model;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import priv.lee.cad.model.Callback;
import priv.lee.cad.model.impl.DefaultStyleToolkit;
import priv.lee.cad.util.ClientAssert;

public class CustomStyleToolkit extends DefaultStyleToolkit {

    public class CancleCheckoutListener implements ActionListener, Callback {

	private Callback callback;

	public CancleCheckoutListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // processor cancle checkout
	    ClientAssert.isTrue (false,"CancleCheckoutListener callback required");
	}

	@Override
	public void call(Object object) {

	}
    }

    public class CheckoutAndDownloadListener implements ActionListener, Callback {

	private Callback callback;

	public CheckoutAndDownloadListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // processor checkout and docwnload
	    ClientAssert.isTrue (false,"CheckoutAndDownloadListener callback required");
	}

	@Override
	public void call(Object object) {

	}
    }

    public class ClearActionListener implements ActionListener {

	private Callback callback;

	public ClearActionListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // clear details info TODO
	    ClientAssert.isTrue (false,"ClearActionListener callback required");
	}
    }

    public class ExportActionListener implements ActionListener, Callback {

	private Callback callback;

	public ExportActionListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    startExportDetailsDialog (callback);
	}

	@Override
	public void call(Object object) {

	}
    }

    public class ManualListener implements ActionListener, Callback {

	private Callback callback;

	public ManualListener(Callback callback) {
	    this.callback = callback;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	    // processor help
	    ClientAssert.isTrue (false,"ManualListener callback required");
	}

	@Override
	public void call(Object object) {

	}
    }

    public class QuitActionListenner implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    System.exit (0);
	}
    }

    private static final String CANCLE_CHECKOUT_MENU_ITEM = "menu.option.item4";
    private static final String CHECKIN_MENU_ITEM = "menu.option.item1";
    private static final String CHECKOUT_DOWNLOAD_MENU_ITEM = "menu.option.item3";
    private static final String CHECKOUT_MENU_ITEM = "menu.option.item2";
    private static final String CLEARDETAILS_MENU_ITEM = "menu.file.item2";
    private static final String EXPORTDETAILS_MENU_ITEM = "menu.file.item3";
    private static final String FILE_MENU = "menu.file";
    private static final String HELP_MENU = "menu.help";
    private static final String MANUAL_MENU_ITEM = "munu.help.item1";
    private static final String OPTION_MENU = "menu.option";
    private static final String QUIT_MENU_ITEM = "menu.file.item1";
    private Callback callback;

    public CustomStyleToolkit(Callback callback) {
	this.callback = callback;
    }

    public JMenu buildFileMenu() {
	JMenu file = new JMenu (resourceMap.getString (FILE_MENU));
	file.getPopupMenu ().setLightWeightPopupEnabled (false);

	JMenuItem quit = new JMenuItem (resourceMap.getString (QUIT_MENU_ITEM));
	quit.addActionListener (new QuitActionListenner ());
	file.add (quit);

	JMenuItem clearDetails = new JMenuItem (resourceMap.getString (CLEARDETAILS_MENU_ITEM));
	clearDetails.addActionListener (new ClearActionListener (callback));
	file.addSeparator ();
	file.add (clearDetails);

	JMenuItem exportDetails = new JMenuItem (resourceMap.getString (EXPORTDETAILS_MENU_ITEM));
	exportDetails.addActionListener (new ExportActionListener (callback));
	file.addSeparator ();
	file.add (exportDetails);
	return file;
    }

    public JMenu buildHelpMenu() {
	JMenu help = new JMenu (resourceMap.getString (HELP_MENU));
	help.getPopupMenu ().setLightWeightPopupEnabled (false);
	JMenuItem manual = new JMenuItem (resourceMap.getString (MANUAL_MENU_ITEM));
	manual.addActionListener (new ManualListener (callback));
	help.add (manual);
	return help;
    }

    public JMenu buildOptionMenu(ActionListener checkinListener, ActionListener checkoutListener) {
	JMenu option = new JMenu (resourceMap.getString (OPTION_MENU));
	option.getPopupMenu ().setLightWeightPopupEnabled (false);

	JMenuItem checkin = new JMenuItem (resourceMap.getString (CHECKIN_MENU_ITEM));
	if (checkinListener != null) {
	    checkin.addActionListener (checkinListener);
	}
	option.add (checkin);

	JMenuItem checkout = new JMenuItem (resourceMap.getString (CHECKOUT_MENU_ITEM));
	if (checkoutListener != null) {
	    checkout.addActionListener (checkoutListener);
	}
	option.addSeparator ();
	option.add (checkout);

	JMenuItem checkoutAndDownload = new JMenuItem (resourceMap.getString (CHECKOUT_DOWNLOAD_MENU_ITEM));
	checkoutAndDownload.addActionListener (new CheckoutAndDownloadListener (callback));
	option.addSeparator ();
	option.add (checkoutAndDownload);

	JMenuItem cancleCheckout = new JMenuItem (resourceMap.getString (CANCLE_CHECKOUT_MENU_ITEM));
	cancleCheckout.addActionListener (new CancleCheckoutListener (callback));
	option.addSeparator ();
	option.add (cancleCheckout);
	return option;
    }

    @Override
    public JMenuBar getStandardMenuBar(ActionListener checkinListener, ActionListener checkoutListener) {
	// ~ add menu bar
	JMenuBar menuBar = new JMenuBar ();
	JMenu file = buildFileMenu ();
	menuBar.add (file);
	JMenu option = buildOptionMenu (checkinListener,checkoutListener);
	menuBar.add (option);
	JMenu help = buildHelpMenu ();
	menuBar.add (help);
	return menuBar;
    }

    public void startExportDetailsDialog(Callback callback) {
	EventQueue.invokeLater (new Runnable () {
	    public void run() {
		// export details TODO
		// PreferencesDialog dialog = new PreferencesDialog(container);
		// dialog.activate();
		ClientAssert.isTrue (false,"startExportDetailsDialog callback required");
	    }
	});
    }
}
