package com.bplead.cad.ui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.bplead.cad.util.ClientUtils;
import com.bplead.cad.util.ReadToXml;

import priv.lee.cad.model.Callback;
import priv.lee.cad.ui.AbstractDialog;
import priv.lee.cad.ui.AbstractPanel;
import priv.lee.cad.ui.Option;
import priv.lee.cad.ui.OptionPanel;
import priv.lee.cad.ui.PromptTextField;
import priv.lee.cad.util.ClientAssert;
import priv.lee.cad.util.StringUtils;

public class ChooseDrawingDialog extends AbstractDialog implements ActionListener, Runnable {
	private final String DWG_ADD_ICON = "dwg.add.icon";
	private final String DWG_REMOVE_ICON = "dwg.remove.icon";

	private static LayoutManager rightLayout = new FlowLayout(FlowLayout.RIGHT);
	private static LayoutManager leftLayout = new FlowLayout(FlowLayout.LEFT);

	private static final Logger logger = Logger.getLogger(ChooseDrawingDialog.class);
	private static final long serialVersionUID = 1336292047030719519L;
	private ConfirmPanel confirmPanel;
	private SearchResultPanel searchResultPanel;
	private String source = "";

	public ChooseDrawingDialog(Callback container) {
		super(ChooseDrawingDialog.class, container);
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
		searchResultPanel = new SearchResultPanel();
		add(searchResultPanel);

		logger.info("initialize " + getClass() + " content...");
		confirmPanel = new ConfirmPanel();
		add(confirmPanel);

		logger.info("initialize " + getClass() + "  completed...");
	}

	@Override
	public Object setCallbackObject() {
		return null;
	}

	class ConfirmPanel extends AbstractPanel implements ActionListener {
		private static final long serialVersionUID = -6481481565984135229L;
		protected PromptTextField setting;

		@Override
		public void actionPerformed(ActionEvent e) {
			DefaultTableModel tableModel = (DefaultTableModel) searchResultPanel.table.getModel();
			int rowCount = tableModel.getRowCount();
			String filePath = "";
			for (int i = 0; i < rowCount; i++) {
				File file = new File((String) searchResultPanel.table.getValueAt(i, 3));
				if (!file.exists()) {
					continue;
				}
				String selectPath = "\"" + file.getParent() + File.separator + File.separator + file.getName() + "\"";
				if (StringUtils.isEmpty(filePath)) {
					filePath = selectPath;
				} else {
					filePath = filePath + selectPath;
				}
			}
			if (StringUtils.isEmpty(filePath)) {
				ClientAssert.isTrue(false, "please add DWG files!");
			} else {
				ReadToXml.readToXml(filePath);
				Container parent = getParent();
				while (!(parent instanceof Window)) {
					parent = parent.getParent();
				}
				EventQueue.invokeLater((Runnable) parent);
			}
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
			Option confirm = new Option(Option.CONFIRM_BUTTON, null, this);
			Option download = new Option(Option.DOWNLOAD_BUTTON, null, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					source = Option.DOWNLOAD_BUTTON;
					Container parent = getParent();
					while (!(parent instanceof Window)) {
						parent = parent.getParent();
					}
					EventQueue.invokeLater((Runnable) parent);
				}
			});

			add(new OptionPanel(Arrays.asList(download, confirm, Option.newCancelOption((Window) parent))));
		}
	}

	class SearchResultPanel extends AbstractPanel {

		private static final long serialVersionUID = -7416585921364617464L;
		protected ChooseDrawingTable table;
		private double TABLE_HEIGTH_PROPORTION = 0.75d;
		private double TABLE_WIDTH_PROPORTION = 0.97d;
		private final String TITLE = "title";

		@Override
		public double getHorizontalProportion() {
			return 0.95d;
		}

		@Override
		public double getVerticalProportion() {
			return 0.75d;
		}

		@Override
		public void initialize() {
			setLayout(leftLayout);
			setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					getResourceMap().getString(TITLE), TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, toolkit.getFont()));

			Option addOption = new Option(null, DWG_ADD_ICON, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new AddLocalFileChooser(JFileChooser.FILES_ONLY, table);
				}
			});
			addOption.setContentAreaFilled(false);
			addOption.setBorder(null);
			Option removeOption = new Option(null, DWG_REMOVE_ICON, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					new RemoveLocalFileChooser(table);
				}
			});
			removeOption.setContentAreaFilled(false);
			removeOption.setBorder(null);

			OptionPanel optionPanel = new OptionPanel(Arrays.asList(addOption, removeOption));
			add(optionPanel);
			optionPanel.setPreferredSize(new Dimension(60, 20));

			table = new ChooseDrawingTable();
			JScrollPane sp = new JScrollPane(table);
			sp.setPreferredSize(new Dimension((int) (getPreferredSize().width * TABLE_WIDTH_PROPORTION),
					(int) (getPreferredSize().height * TABLE_HEIGTH_PROPORTION)));
			table.setColumnWidth();
			add(sp);
		}
	}

	@Override
	public void run() {
		if (StringUtils.equalsIgnoreCase(Option.DOWNLOAD_BUTTON, source)) {
			ClientUtils.args.setType(Option.DOWNLOAD_BUTTON);
		}
		new LoginFrame().activate();
		dispose();
	}
}