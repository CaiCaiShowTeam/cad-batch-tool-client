package com.bplead.cad.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.rmi.server.ExportException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.bplead.cad.bean.DataContent;
import com.bplead.cad.bean.SimpleDocument;
import com.bplead.cad.model.CustomPrompt;
import com.bplead.cad.util.ClientUtils;
import com.bplead.cad.util.ExportExcelUtil;
import com.bplead.cad.util.FTPUtils;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractDialog;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;
import priv.lee.cad.util.ClientAssert;

public class BomDetailDialog extends AbstractDialog implements ActionListener {

	private static LayoutManager rightLayout = new FlowLayout(FlowLayout.RIGHT);
	private static LayoutManager leftLayout = new FlowLayout(FlowLayout.LEFT);

	private static final Logger logger = Logger.getLogger(BomDetailDialog.class);
	private static final long serialVersionUID = 1336292047030719519L;
	private final String REDREMINDER = "redReminder";
	private final String BLACKREMINDER = "blackReminder";
	private final String BLUEREMINDER = "blueReminder";

	private ExportSettingPanel exportSettingPanel;
	private BomDetailResultPanel bomDetailResultPanel;

	public BomDetailDialog(Callback container) {
		super(BomDetailDialog.class, container);
	}

	private void download(File serverFile, File localFile) {
		FTPUtils utils = FTPUtils.newInstance();
		utils.download(serverFile, localFile);
		utils.delete(serverFile);
	}

	@Override
	public double getHorizontalProportion() {
		return 0.5;
	}

	@Override
	public double getVerticalProportion() {
		return 0.5;
	}

	@Override
	public void initialize() {
		logger.info("initialize " + getClass() + " layout...");
		setLayout(leftLayout);
		
		logger.info("initialize " + getClass() + " content...");
		JPanel panel = new JPanel();
		panel.setLayout(leftLayout);
		JLabel label1 = new JLabel(getResourceMap().getString(REDREMINDER));
		label1.setForeground(Color.RED);
		JLabel label2 = new JLabel(getResourceMap().getString(BLACKREMINDER));
		label2.setForeground(Color.BLACK);
		JLabel label3 = new JLabel(getResourceMap().getString(BLUEREMINDER));
		label3.setForeground(Color.BLUE);
		panel.add(label1);
		panel.add(label2);
		panel.add(label3);
		add(panel);

		logger.info("initialize " + getClass() + " content...");
		bomDetailResultPanel = new BomDetailResultPanel();
		add(bomDetailResultPanel);

		logger.info("initialize " + getClass() + " content...");
		exportSettingPanel = new ExportSettingPanel();
		add(exportSettingPanel);

		logger.info("initialize " + getClass() + "  completed...");
	}

	@Override
	public Object setCallbackObject() {
		String localPath = exportSettingPanel.setting.getText().getText();
		ClientAssert.hasText(localPath, CustomPrompt.LOCAL_REPOSITORY_NULL);

		List<SimpleDocument> documents = bomDetailResultPanel.table.getSelectedDocuments();
		ClientAssert.notEmpty(documents, CustomPrompt.SELECTED_ITEM_NULL);

		DataContent content = ClientUtils.checkoutAndDownload(documents);
		ClientAssert.notNull(content, CustomPrompt.FAILD_OPTION);

		logger.info("download " + getClass() + "  completed...");
		File localFile = new File(localPath + File.separator + content.getServerFile().getName());
		download(content.getServerFile(), localFile);

		logger.info("unzip file...");
		return ClientUtils.unzip(localFile);
	}

	class ExportSettingPanel extends AbstractPanel implements ActionListener {

		private static final long serialVersionUID = -6481481565984135229L;
		protected PromptTextField setting;

		@Override
		public void actionPerformed(ActionEvent e) {
			ExportExcelUtil util = new ExportExcelUtil(bomDetailResultPanel.table);
			util.export();
		}

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.15d;
		}

		@Override
		public void initialize() {
			setLayout(rightLayout);

			Container parent = getParent();
			while (!(parent instanceof Window)) {
				parent = parent.getParent();
			}
			Option export = new Option(Option.EXPORT_BUTTON, null, this);

			add(new OptionPanel(Arrays.asList(export, Option.newCancelOption((Window) parent))));
		}
	}


	class BomDetailResultPanel extends AbstractPanel {

		private static final long serialVersionUID = -7416585921364617464L;
		protected BomDetailTable table;
		private double TABLE_HEIGTH_PROPORTION = 0.85d;
		private double TABLE_WIDTH_PROPORTION = 0.98d;

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.7d;
		}

		@Override
		public void initialize() {
			table = new BomDetailTable(null);
			JScrollPane sp = new JScrollPane(table);
			sp.setPreferredSize(new Dimension((int) (getPreferredSize().width * TABLE_WIDTH_PROPORTION),
					(int) (getPreferredSize().height * TABLE_HEIGTH_PROPORTION)));
			table.setColumnWidth();
			add(sp);
		}

		public void initResultTable(List<SimpleDocument> documents) {
			table.refresh(documents);
		}
	}
}