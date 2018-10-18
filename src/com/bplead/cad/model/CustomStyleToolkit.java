package com.bplead.cad.model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.bplead.cad.bean.constant.RemoteMethod;

import priv.lee.cad.model.impl.DefaultStyleToolkit;

public class CustomStyleToolkit extends DefaultStyleToolkit {

    public class ManualListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    try {
		Runtime.getRuntime ().exec ("cmd /c start https://news.sina.com.cn/");
	    }
	    catch(IOException ee) {
		ee.printStackTrace ();
	    }
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
    private HashMap<String, ActionListener> listenerMap;

    public JMenu buildFileMenu() {
	JMenu file = new JMenu (resourceMap.getString (FILE_MENU));
	file.getPopupMenu ().setLightWeightPopupEnabled (false);

	JMenuItem quit = new JMenuItem (resourceMap.getString (QUIT_MENU_ITEM));
	quit.addActionListener (new QuitActionListenner ());
	file.add (quit);

	JMenuItem clearDetails = new JMenuItem (resourceMap.getString (CLEARDETAILS_MENU_ITEM));
	clearDetails.addActionListener (listenerMap.get (RemoteMethod.CLEAR_DETAIL_LISTENNER));
	file.addSeparator ();
	file.add (clearDetails);

	JMenuItem exportDetails = new JMenuItem (resourceMap.getString (EXPORTDETAILS_MENU_ITEM));
	exportDetails.addActionListener (listenerMap.get (RemoteMethod.EXPORT_DETAIL_LISTENNER));
	file.addSeparator ();
	file.add (exportDetails);
	return file;
    }

    public JMenu buildHelpMenu() {
	JMenu help = new JMenu (resourceMap.getString (HELP_MENU));
	help.getPopupMenu ().setLightWeightPopupEnabled (false);
	JMenuItem manual = new JMenuItem (resourceMap.getString (MANUAL_MENU_ITEM));
	manual.addActionListener (new ManualListener ());
	help.add (manual);
	return help;
    }

    public JMenu buildOptionMenu(ActionListener checkinListener, ActionListener checkoutAndDownloadListener) {
	JMenu option = new JMenu (resourceMap.getString (OPTION_MENU));
	option.getPopupMenu ().setLightWeightPopupEnabled (false);

	JMenuItem checkin = new JMenuItem (resourceMap.getString (CHECKIN_MENU_ITEM));
	if (checkinListener != null) {
	    checkin.addActionListener (checkinListener);
	}
	option.add (checkin);

	JMenuItem checkout = new JMenuItem (resourceMap.getString (CHECKOUT_MENU_ITEM));
	checkout.addActionListener (listenerMap.get (RemoteMethod.CHECKOUT_LISTENNER));
	option.addSeparator ();
	option.add (checkout);

	JMenuItem checkoutAndDownload = new JMenuItem (resourceMap.getString (CHECKOUT_DOWNLOAD_MENU_ITEM));
	if (checkoutAndDownloadListener != null) {
	    checkoutAndDownload.addActionListener (checkoutAndDownloadListener);
	}
	option.addSeparator ();
	option.add (checkoutAndDownload);

	JMenuItem cancleCheckout = new JMenuItem (resourceMap.getString (CANCLE_CHECKOUT_MENU_ITEM));
	cancleCheckout.addActionListener (listenerMap.get (RemoteMethod.UNDO_CHECKOUT_LISTENNER));
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

    public void setListenerMap(HashMap<String, ActionListener> listenerMap) {
	this.listenerMap = listenerMap;
    }
}
